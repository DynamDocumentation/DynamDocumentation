import inspect
import importlib
import json
import os
import warnings
from typing import Dict, List

# Import the improved functions from pop_general
from pop_general import (get_function_signature, parse_numpy_tensorflow_style, 
                        extract_functions, extract_classes, extract_methods)

# Suprimir avisos para evitar ruído na saída
warnings.filterwarnings("ignore")

# Criar diretório de saída se não existir
OUTPUT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "output"))
os.makedirs(OUTPUT_DIR, exist_ok=True)

def parse_docstring(doc: str) -> dict:
    """
    Parse a estilo NumPy/Sphinx no docstring, segmentando em seções
    como 'Parameters', 'Returns', etc.
    
    Esta função agora utiliza a implementação mais robusta de
    parse_numpy_tensorflow_style da pop_general.py
    """
    # Como estamos trabalhando com scikit-learn, usamos o parser de NumPy/TensorFlow 
    # que funciona bem para documentação no estilo scikit-learn
    return parse_numpy_tensorflow_style(doc)

# These functions are now imported from pop_general and use "docstring" consistently

def extract_constants(module) -> List[Dict]:
    """Extrai variáveis públicas que sejam maiúsculas (possivelmente 'constantes')."""
    constants = []
    for name in dir(module):
        if name.startswith('_'):
            continue
        obj = getattr(module, name)
        if not callable(obj) and not inspect.isclass(obj) and name.isupper():
            constants.append({
                "name": name,
                "value": str(obj),
                "docstring": parse_docstring(inspect.getdoc(obj) or "")
            })
    return constants

def extract_module_api(module_name: str) -> Dict:
    """
    Retorna a estrutura detalhada de todo o módulo:
      - Descrição do módulo
      - Lista de funções (nome, assinatura, docstring)
      - Lista de classes (nome, docstring, métodos)
      - Lista de 'constantes' (nome, valor, docstring)
    """
    try:
        module = importlib.import_module(module_name)
    except ImportError as e:
        return {"error": str(e)}

    # Usando as funções importadas de pop_general que já usam o campo "docstring"
    functions = extract_functions(module, module_name)
    classes = extract_classes(module, module_name)
    
    return {
        "description": inspect.getdoc(module) or "",
        "functions": functions,
        "classes": classes,
        "constants": extract_constants(module)
    }

if __name__ == "__main__":
    # Altere esta lista de módulos conforme necessário:
    TARGET_MODULES = {
        'seaborn',
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

    # Lista para rastrear todos os módulos processados
    processed_modules = []
    
    # Extrai e salva para cada módulo
    for targ in TARGET_MODULES:
        sklearn_api = extract_module_api(targ)
        
        # Cria diretório específico para a biblioteca, se necessário
        lib_output_dir = os.path.join(OUTPUT_DIR, targ.split('.')[0])
        os.makedirs(lib_output_dir, exist_ok=True)
        
        # Salva o arquivo JSON no diretório de saída
        filename = f'{targ.split(".")[-1]}.json'
        output_path = os.path.join(lib_output_dir, filename)
        
        with open(output_path, 'w') as f:
            json.dump(sklearn_api, f, indent=2)
            
        # Adicionar ao registro de módulos processados
        processed_modules.append({
            "name": targ,
            "file": filename
        })
    
    # Criar arquivo index.json
    # O arquivo modules precisa conter uma lista de strings com os nomes dos módulos
    # para ser compatível com namespace_pop.py
    module_names = [module["name"] for module in processed_modules]
    
    index = {
        "library": "sklearn",
        "modules": module_names
    }
    
    index_path = os.path.join(lib_output_dir, "index.json")
    with open(index_path, 'w') as f:
        json.dump(index, f, indent=2)

    print(f"\n✅ JSONs de cada submódulo do sklearn foram gerados com sucesso em {lib_output_dir}!")
    print(f"✅ Arquivo index.json criado em {index_path}")
