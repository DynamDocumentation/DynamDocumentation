import mariadb
import os
import json

# Configurações de conexão
config = {
    "user": "dynam",
    "password": "1234",
    "host": "localhost",
    "port": 3306,
    "database": "dynam"
}

def populate_namespaces_from_output(output_dir="output", specific_library=None):
    try:
        conn = mariadb.connect(**config)
        cur = conn.cursor()
        print("Conexão bem-sucedida com o banco de dados MariaDB!")

        # Caminho absoluto para a pasta output
        base_path = os.path.abspath(output_dir)
        if not os.path.isdir(base_path):
            print(f"Pasta '{base_path}' não encontrada.")
            return

        # If a specific library is provided, only process that library
        folders = [specific_library] if specific_library else os.listdir(base_path)
        
        for folder in folders:
            folder_path = os.path.join(base_path, folder)
            index_path = os.path.join(folder_path, "index.json")
            if os.path.isdir(folder_path) and os.path.isfile(index_path):
                print(f"Lendo arquivo: {index_path}")
                with open(index_path, "r", encoding="utf-8") as f:
                    data = json.load(f)
                    print(data)  # Exibe o conteúdo do JSON para depuração
                    # Se for dict com chave "modules", insere cada módulo
                    if isinstance(data, dict) and "modules" in data:
                        for module in data["modules"]:
                            cur.execute("INSERT INTO Namespaces (name) VALUES (?)", (module,))
                            print(f"Adicionado namespace: {module}")
                    # (mantém os outros casos se quiser)
                    elif isinstance(data, dict) and "name" in data:
                        name = data["name"]
                        cur.execute("INSERT INTO Namespaces (name) VALUES (?)", (name,))
                        print(f"Adicionado namespace: {name}")
                    elif isinstance(data, list):
                        for item in data:
                            if isinstance(item, dict) and "name" in item:
                                name = item["name"]
                                cur.execute("INSERT INTO Namespaces (name) VALUES (?)", (name,))
                                print(f"Adicionado namespace: {name}")
                            elif isinstance(item, str):
                                cur.execute("INSERT INTO Namespaces (name) VALUES (?)", (item,))
                                print(f"Adicionado namespace: {item}")
                    elif isinstance(data, str):
                        cur.execute("INSERT INTO Namespaces (name) VALUES (?)", (data,))
                        print(f"Adicionado namespace: {data}")
        conn.commit()
        cur.close()
        conn.close()
    except mariadb.Error as e:
        print(f"Erro ao conectar ao MariaDB: {e}")

def main():
    populate_namespaces_from_output("../output")

if __name__ == "__main__":
    main()