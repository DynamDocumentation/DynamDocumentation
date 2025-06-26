import inspect
import warnings
import importlib
import json
import os
from typing import Dict, List

# Configuração para suprimir warnings
warnings.filterwarnings("ignore")

# Criar diretório de saída se não existir
OUTPUT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "output"))
os.makedirs(OUTPUT_DIR, exist_ok=True)

# Exemplo de submódulos para o scikit-learn (use ou ajuste conforme necessário)
TARGET_MODULES = {
    'sklearn',
    'sklearn.linear_model',
    'sklearn.tree',
    'sklearn.svm',
    'sklearn.cluster',
    'sklearn.naive_bayes',
    'sklearn.neighbors',
    'sklearn.preprocessing',
    'sklearn.model_selection',
    'sklearn.feature_selection',
    'sklearn.pipeline'
}

def get_module_contents(module) -> List[str]:
    """Captura TODOS os atributos públicos do módulo, incluindo funções, métodos e itens callables."""
    contents = []
    for name in dir(module):
        if name.startswith('_'):
            continue
        obj = getattr(module, name)
        if callable(obj):
            contents.append(name)
    return sorted(contents)

def extract_module_api(module_name: str) -> Dict:
    """
    Retorna as informações básicas de um módulo:
      - Descrição (docstring do módulo)
      - Lista de funções públicas (nomes)
      - Lista de classes públicas (nomes)
    """
    try:
        module = importlib.import_module(module_name)
    except ImportError as e:
        return {
            "description": f"Error loading module: {str(e)}",
            "functions": [],
            "classes": []
        }

    description = inspect.getdoc(module) or "No description available"
    functions_list = get_module_contents(module)
    classes_list = [
        name for name, obj in inspect.getmembers(module, inspect.isclass) if not name.startswith('_')
    ]

    return {
        "description": description,
        "functions": functions_list,
        "classes": classes_list
    }

def build_module_structure() -> Dict[str, Dict]:
    """Constrói a estrutura do sklearn e seus submódulos."""
    sklearn_api = {}

    for module_name in TARGET_MODULES:
        try:
            sklearn_api[module_name] = extract_module_api(module_name)
        except ImportError as e:
            sklearn_api[module_name] = {"description": str(e), "functions": [], "classes": []}

    return sklearn_api

if __name__ == "__main__":
    sklearn_api = build_module_structure()

    # Exemplo simples de impressão para um submódulo específico
    print("\n🔍 Conteúdo de sklearn.tree:")
    print(sklearn_api['sklearn.tree'])

    # Cria diretório específico para a biblioteca
    lib_output_dir = os.path.join(OUTPUT_DIR, 'sklearn')
    os.makedirs(lib_output_dir, exist_ok=True)
    
    # Salva todo o resultado em um JSON no diretório de saída
    output_path = os.path.join(lib_output_dir, 'sklearn_api.json')
    
    with open(output_path, 'w') as f:
        json.dump(sklearn_api, f, indent=2)

    print(f"\n✅ Arquivo 'sklearn_api.json' gerado com sucesso em {output_path}!")
