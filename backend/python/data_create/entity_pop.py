import os
import json
import mariadb

# Database connection configuration
config = {
    "user": "dynam",
    "password": "1234",
    "host": "localhost",
    "port": 3306,
    "database": "dynam"
}

def populate_entities_from_namespaces(output_dir="../output"):
    """
    Populate Classes and Functions tables based on namespace entries.
    
    1. Reads all namespaces from the database
    2. For each namespace, finds the corresponding JSON file
    3. Extracts classes and functions
    4. Inserts them into the database with proper relationships
    """
    try:
        conn = mariadb.connect(**config)
        cur = conn.cursor()
        print("Successfully connected to MariaDB database")
        
        # Get all namespaces from the database
        cur.execute("SELECT id, name FROM Namespaces")
        namespaces = cur.fetchall()
        
        # Process each namespace
        for namespace_id, namespace_name in namespaces:
            # Split namespace to determine path to JSON file
            parts = namespace_name.split('.')
            if len(parts) < 2:
                # Skip namespaces that don't have module structure
                continue
                
            library = parts[0]
            module = parts[-1]  # Get last part as module name
            
            # Build path to JSON file
            json_path = os.path.join(output_dir, library, f"{module}.json")
            
            if os.path.isfile(json_path):
                print(f"Processing {json_path} for namespace {namespace_name} (ID: {namespace_id})")
                
                with open(json_path, "r", encoding="utf-8") as f:
                    data = json.load(f)
                    
                    # Process classes
                    if "classes" in data:
                        for class_data in data["classes"]:
                            if "name" in class_data:
                                class_name = class_data["name"]
                                documentation = class_data.get("documentation", {})
                                class_description = documentation.get("description", None)
                                class_example = documentation.get("examples", None)
                                
                                # Insert class with example field
                                cur.execute(
                                    "INSERT INTO Classes (namespace_id, name, description, example) VALUES (?, ?, ?, ?)",
                                    (namespace_id, class_name, class_description, class_example)
                                )
                                class_id = cur.lastrowid
                                # print(f"  Added class: {class_name} (ID: {class_id})")
                                
                                # Process methods (functions that belong to a class)
                                if "methods" in class_data:
                                    for method_data in class_data["methods"]:
                                        if "name" in method_data:
                                            method_name = method_data["name"]
                                            method_signature = method_data.get("signature", None)
                                            method_doc = method_data.get("documentation", {})
                                            method_description = method_doc.get("description", None)
                                            method_returns = method_doc.get("returns", None)
                                            method_example = method_doc.get("examples", None)
                                            
                                            # Insert method with return_type and example fields
                                            cur.execute(
                                                "INSERT INTO Functions (parent_class_id, name, signature, description, return_type, example) VALUES (?, ?, ?, ?, ?, ?)",
                                                (class_id, method_name, method_signature, method_description, method_returns, method_example)
                                            )
                                            # print(f"    Added method: {method_name}")
                    
                    # Process top-level functions
                    if "functions" in data:
                        for function_data in data["functions"]:
                            if "name" in function_data:
                                function_name = function_data["name"]
                                function_signature = function_data.get("signature", None)
                                function_doc = function_data.get("documentation", {})
                                function_description = function_doc.get("description", None)
                                function_returns = function_doc.get("returns", None)
                                function_example = function_doc.get("examples", None)
                                
                                # Insert function with return_type and example fields
                                cur.execute(
                                    "INSERT INTO Functions (parent_namespace_id, name, signature, description, return_type, example) VALUES (?, ?, ?, ?, ?, ?)",
                                    (namespace_id, function_name, function_signature, function_description, function_returns, function_example)
                                )
                                # print(f"  Added function: {function_name}")
            else:
                print(f"JSON file not found for namespace {namespace_name}: {json_path}")
                
        conn.commit()
        cur.close()
        conn.close()
        print("Database population completed successfully")
        
    except mariadb.Error as e:
        print(f"Error connecting to MariaDB: {e}")
        try:
            if conn:
                conn.close()
        except:
            pass

if __name__ == "__main__":
    populate_entities_from_namespaces()