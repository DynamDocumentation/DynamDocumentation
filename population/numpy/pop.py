import inspect
import numpy as np
from typing import Dict, List, Set
import warnings
import importlib

# Configuração para suprimir warnings
warnings.filterwarnings("ignore")

# Lista dos namespaces oficiais
TARGET_MODULES = {
    'numpy',
    'numpy.exceptions',
    'numpy.fft',
    'numpy.linalg',
    'numpy.polynomial',
    'numpy.random',
    'numpy.strings',
    'numpy.testing',
    'numpy.typing',
    'numpy.ctypeslib',
    'numpy.dtypes',
    'numpy.emath',
    'numpy.lib',
    'numpy.rec',
    'numpy.version',
    'numpy.char',
    'numpy.ma'
}



def get_module_contents(module) -> List[str]:
    """Captura TODOS os atributos públicos do módulo, incluindo ufuncs e funções C."""
    contents = []
    for name in dir(module):
        if name.startswith('_'):
            continue
        obj = getattr(module, name)
        # Verifica se é callable (função, método, ufunc, etc.)
        if callable(obj):
            contents.append(name)
    return sorted(contents)

def extract_module_api(module_name: str) -> Dict:
    try:
        module = importlib.import_module(module_name)
    except ImportError as e:
        return {
            "description": f"Error loading module: {str(e)}",
            "functions": [],
            "classes": {}
        }

    return {
        "description": inspect.getdoc(module) or "No description available",
        "functions": get_module_contents(module),
        "classes":  [name for name, obj in inspect.getmembers(module, inspect.isclass) if not name.startswith('_')] 
    }


def build_module_structure() -> Dict[str, Dict]:
    """Constrói a estrutura do módulo numpy e seus submódulos."""
    numpy_api = {}
    
    for module_name in TARGET_MODULES:
        try:
            module = importlib.import_module(module_name)
            numpy_api[module_name] = extract_module_api(module_name)
            
        except ImportError as e:
            numpy_api[module_name] = {"description": str(e), "functions": []}
    return numpy_api

if __name__ == "__main__":
    numpy_api = build_module_structure()
    
    # Exemplo: mostrar estrutura do numpy.linalg
    print("\n🔍 Conteúdo de numpy.linalg:")
    print(numpy_api['numpy.linalg'])
    
    # Exemplo: mostrar algumas funções do namespace principal
    print("\n📌 Algumas funções do namespace principal:")
    print(numpy_api['numpy.linalg'])

    import json
    with open('numpy_api.json', 'w') as f:
        json.dump(numpy_api, f, indent=2)
    
    names = []

    with open('numpy_api.json', 'r') as f:
        for namespace, data in numpy_api.items():
            names.append(namespace)
    with open('../libs.json', 'r') as f:    
        val = json.load(f)
    val['numpy'] = names
    with open('../libs.json', 'w') as f:
        json.dump(val, f, indent=2)


        
    
    
