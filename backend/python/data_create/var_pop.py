import os
import json
import mariadb
from enum import Enum

# Define VariableType enum to match the Kotlin enum
class VariableType(Enum):
    PARAMETER = "PARAMETER"
    FIELD = "FIELD"
    PROPERTY = "PROPERTY"

# Database connection configuration
config = {
    "user": "dynam",
    "password": "1234",
    "host": "localhost",
    "port": 3306,
    "database": "dynam"
}

def populate_variables(output_dir="../output", specific_library=None):
    """
    Populate Variables table with parameters from classes and functions.
    
    Parameters:
    -----------
    output_dir : str
        Path to the output directory containing the JSON files
    specific_library : str, optional
        If specified, only process variables for this library
    """
    try:
        conn = mariadb.connect(**config)
        cur = conn.cursor()
        print("Successfully connected to MariaDB database")
        
        # Get classes and their namespaces, filtered by library if specified
        if specific_library:
            cur.execute("""
                SELECT c.id, c.name, n.name 
                FROM Classes c 
                JOIN Namespaces n ON c.namespace_id = n.id
                WHERE n.name LIKE ?
            """, (f"{specific_library}%",))
        else:
            cur.execute("""
                SELECT c.id, c.name, n.name 
                FROM Classes c 
                JOIN Namespaces n ON c.namespace_id = n.id
            """)
        classes = cur.fetchall()
        
        # Get functions and their parent info (either class or namespace), filtered by library if specified
        if specific_library:
            cur.execute("""
                SELECT f.id, f.name, f.parent_class_id, c.name, n.name, f.parent_namespace_id, n2.name
                FROM Functions f
                LEFT JOIN Classes c ON f.parent_class_id = c.id
                LEFT JOIN Namespaces n ON c.namespace_id = n.id
                LEFT JOIN Namespaces n2 ON f.parent_namespace_id = n2.id
                WHERE (n.name LIKE ? OR n2.name LIKE ?)
            """, (f"{specific_library}%", f"{specific_library}%"))
        else:
            cur.execute("""
                SELECT f.id, f.name, f.parent_class_id, c.name, n.name, f.parent_namespace_id, n2.name
                FROM Functions f
                LEFT JOIN Classes c ON f.parent_class_id = c.id
                LEFT JOIN Namespaces n ON c.namespace_id = n.id
                LEFT JOIN Namespaces n2 ON f.parent_namespace_id = n2.id
            """)
        functions = cur.fetchall()
        
        # Process class parameters
        for class_id, class_name, namespace_name in classes:
            process_class_parameters(cur, output_dir, class_id, class_name, namespace_name)
        
        # Process function parameters
        for func_id, func_name, parent_class_id, parent_class_name, parent_class_namespace, parent_namespace_id, parent_namespace_name in functions:
            process_function_parameters(cur, output_dir, func_id, func_name, parent_class_id, parent_class_name, parent_class_namespace, parent_namespace_id, parent_namespace_name)
        
        conn.commit()
        cur.close()
        conn.close()
        print("Variables table populated successfully")
        
    except mariadb.Error as e:
        print(f"Error connecting to MariaDB: {e}")
        try:
            if conn:
                conn.close()
        except:
            pass

def process_class_parameters(cur, output_dir, class_id, class_name, namespace_name):
    """Process parameters for a class and insert them into the Variables table"""
    parts = namespace_name.split('.')
    if len(parts) < 2:
        return
        
    library = parts[0]
    module = parts[-1]
    
    # Build path to JSON file
    json_path = os.path.join(output_dir, library, f"{module}.json")
    
    if not os.path.isfile(json_path):
        print(f"JSON file not found for namespace {namespace_name}: {json_path}")
        return
        
    with open(json_path, "r", encoding="utf-8") as f:
        data = json.load(f)
        
        if "classes" not in data:
            return
            
        # Find the matching class
        for class_data in data["classes"]:
            if "name" in class_data and class_data["name"] == class_name:
                # Extract parameters
                parameters = class_data.get("documentation", {}).get("parameters", {})
                
                # Insert each parameter
                for param_name, param_info in parameters.items():
                    if isinstance(param_info, dict):
                        param_type = param_info.get("type", "")
                        param_desc = param_info.get("description", "")
                        
                        # Extract default value if present in type
                        default_value = None
                        if "default=" in param_type:
                            default_parts = param_type.split("default=", 1)
                            if len(default_parts) > 1:
                                default_value = default_parts[1].split(",", 1)[0].strip()
                        
                        cur.execute(
                            "INSERT INTO Variables (class_id, function_id, type, name, data_type, description, default_value) VALUES (?, ?, ?, ?, ?, ?, ?)",
                            (class_id, None, "PARAMETER", param_name, param_type, param_desc, default_value)
                        )
                        # print(f"  Added parameter '{param_name}' to class '{class_name}'")
                break

def process_function_parameters(cur, output_dir, func_id, func_name, parent_class_id, parent_class_name, parent_class_namespace, parent_namespace_id, parent_namespace_name):
    """Process parameters for a function and insert them into the Variables table"""
    # Determine JSON file location
    if parent_class_id is not None:
        # This is a method within a class
        parts = parent_class_namespace.split('.') if parent_class_namespace else []
    else:
        # This is a standalone function
        parts = parent_namespace_name.split('.') if parent_namespace_name else []
    
    if len(parts) < 2:
        return
        
    library = parts[0]
    module = parts[-1]
    
    # Build path to JSON file
    json_path = os.path.join(output_dir, library, f"{module}.json")
    
    if not os.path.isfile(json_path):
        print(f"JSON file not found: {json_path}")
        return
        
    with open(json_path, "r", encoding="utf-8") as f:
        data = json.load(f)
        
        if parent_class_id is not None:
            # Find the method within a class
            if "classes" in data:
                for class_data in data["classes"]:
                    if class_data.get("name") == parent_class_name:
                        if "methods" in class_data:
                            for method_data in class_data["methods"]:
                                if method_data.get("name") == func_name:
                                    parameters = method_data.get("documentation", {}).get("parameters", {})
                                    add_parameters_to_db(cur, parameters, None, func_id, func_name)
        else:
            # Find the standalone function
            if "functions" in data:
                for function_data in data["functions"]:
                    if function_data.get("name") == func_name:
                        parameters = function_data.get("documentation", {}).get("parameters", {})
                        add_parameters_to_db(cur, parameters, None, func_id, func_name)

def add_parameters_to_db(cur, parameters, class_id, func_id, entity_name):
    """Helper function to add parameters to the database"""
    for param_name, param_info in parameters.items():
        if isinstance(param_info, dict):
            param_type = param_info.get("type", "")
            param_desc = param_info.get("description", "")
            
            # Extract default value if present in type
            default_value = None
            if "default=" in param_type:
                default_parts = param_type.split("default=", 1)
                if len(default_parts) > 1:
                    default_value = default_parts[1].split(",", 1)[0].strip()
            
            cur.execute(
                "INSERT INTO Variables (class_id, function_id, type, name, data_type, description, default_value) VALUES (?, ?, ?, ?, ?, ?, ?)",
                (class_id, func_id, "PARAMETER", param_name, param_type, param_desc, default_value)
            )
            # print(f"  Added parameter '{param_name}' to {'class' if class_id else 'function'} '{entity_name}'")

if __name__ == "__main__":
    populate_variables()