import mariadb

def clean_table(table_name):
    """
    Delete all rows from a table and reset AUTO_INCREMENT to 1.
    Skips tables that don't exist.
    """
    config = {
        "user": "dynam",
        "password": "1234",
        "host": "localhost",
        "port": 3306,
        "database": "dynam"
    }
    try:
        conn = mariadb.connect(**config)
        cur = conn.cursor()
        
        # Check if the table exists first
        cur.execute(f"SHOW TABLES LIKE '{table_name}'")
        if not cur.fetchone():
            print(f"Table '{table_name}' doesn't exist, skipping clean operation.")
            conn.close()
            return
        
        # If we get here, the table exists, so we can clean it
        cur.execute(f"DELETE FROM {table_name};")
        cur.execute(f"ALTER TABLE {table_name} AUTO_INCREMENT = 1;")
        conn.commit()
        cur.close()
        conn.close()
        print(f"Table {table_name} cleaned and AUTO_INCREMENT reset.")
    except mariadb.Error as e:
        print(f"Error cleaning table {table_name}: {e}")