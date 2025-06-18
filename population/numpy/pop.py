import inspect
import numpy as np
from typing import Dict, List, Set
import warnings
import importlib

# Configuração para suprimir warnings
warnings.filterwarnings("ignore")

# Lista dos namespaces oficiais
TARGET_NAMESPACES = {
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

def is_numpy_callable(obj) -> bool:
    """Verifica se um objeto é uma função/método callable do NumPy."""
    if callable(obj):
        numpy_types = {
            'numpy.ufunc',
            'numpy._ArrayFunctionDispatcher',
            'numpy._DTypeMeta',
            'numpy.generic',
            'builtin_function_or_method',
            'method'
        }
        return any(t in str(type(obj)) for t in numpy_types)
    return False

def get_class_methods(cls) -> List[str]:
    """Extrai todos os métodos públicos de uma classe."""
    methods = []
    for name, obj in inspect.getmembers(cls, predicate=is_numpy_callable):
        if not name.startswith('_'):
            methods.append(name)
    return methods

def extract_special_module(module_name: str) -> Dict[str, List[str]]:
    """Extrai API de módulos especiais (linalg, random, fft)."""
    try:
        module = importlib.import_module(module_name)
    except ImportError as e:
        print(f"⚠️ Erro ao importar {module_name}: {str(e)}")
        return {'functions': [], 'classes': {}}
    
    api = {'functions': [], 'classes': {}}
    
    # Listas de funções conhecidas para cada módulo especial
    known_functions = {
        'numpy.linalg': ['cholesky', 'cond', 'det', 'eig', 'eigh', 'eigvals', 'eigvalsh',
                        'inv', 'lstsq', 'matrix_power', 'matrix_rank', 'norm', 'pinv',
                        'qr', 'slogdet', 'solve', 'svd', 'tensorinv', 'tensorsolve'],
        'numpy.random': ['beta', 'binomial', 'chisquare', 'choice', 'dirichlet',
                        'exponential', 'f', 'gamma', 'geometric', 'gumbel',
                        'hypergeometric', 'laplace', 'logistic', 'lognormal',
                        'logseries', 'multinomial', 'multivariate_normal',
                        'negative_binomial', 'noncentral_chisquare', 'noncentral_f',
                        'normal', 'pareto', 'permutation', 'poisson', 'power',
                        'rand', 'randint', 'randn', 'random', 'rayleigh',
                        'standard_cauchy', 'standard_exponential', 'standard_gamma',
                        'standard_normal', 'standard_t', 'triangular', 'uniform',
                        'vonmises', 'wald', 'weibull', 'zipf'],
        'numpy.fft': ['fft', 'ifft', 'fft2', 'ifft2', 'fftn', 'ifftn',
                     'rfft', 'irfft', 'rfft2', 'irfft2', 'rfftn', 'irfftn',
                     'hfft', 'ihfft', 'fftfreq', 'rfftfreq', 'fftshift', 'ifftshift']
    }
    
    # Adiciona funções conhecidas
    if module_name in known_functions:
        api['functions'] = [f for f in known_functions[module_name] if hasattr(module, f)]
    
    # Adiciona funções detectadas dinamicamente
    for name in dir(module):
        if name.startswith('_'):
            continue
        obj = getattr(module, name)
        if is_numpy_callable(obj) and name not in api['functions']:
            api['functions'].append(name)
    
    # Extrai classes e métodos
    for name, obj in inspect.getmembers(module, inspect.isclass):
        if not name.startswith('_'):
            api['classes'][name] = get_class_methods(obj)
    
    return api

def extract_namespace_api(module_name: str) -> Dict[str, List[str]]:
    """Extrai API de um namespace específico."""
    if module_name in ['numpy.linalg', 'numpy.random', 'numpy.fft']:
        return extract_special_module(module_name)
    
    try:
        module = importlib.import_module(module_name)
    except ImportError as e:
        print(f"⚠️ Erro ao importar {module_name}: {str(e)}")
        return {'functions': [], 'classes': {}}
    
    api = {'functions': [], 'classes': {}}
    
    for name in dir(module):
        if name.startswith('_'):
            continue
        
        try:
            obj = getattr(module, name)
            if is_numpy_callable(obj):
                api['functions'].append(name)
            elif inspect.isclass(obj):
                api['classes'][name] = get_class_methods(obj)
        except Exception as e:
            print(f"⚠️ Erro ao processar {name} em {module_name}: {str(e)}")
            continue
    
    return api

def get_full_numpy_api() -> Dict[str, Dict[str, List[str]]]:
    """Coleta API completa dos namespaces oficiais do NumPy."""
    results = {}
    
    print("🔍 Iniciando extração da API do NumPy...")
    for namespace in sorted(TARGET_NAMESPACES):
        print(f"⏳ Processando {namespace}...", end='\r')
        api_data = extract_namespace_api(namespace)
        
        if api_data['functions'] or api_data['classes']:
            results[namespace] = api_data
            print(f"✅ {namespace.ljust(20)} {len(api_data['functions'])} funções, {len(api_data['classes'])} classes")
        else:
            print(f"⚠️  {namespace.ljust(20)} vazio ou não carregado")
    
    return results
if __name__ == "__main__":
    numpy_api = get_full_numpy_api()
    
    # Exemplo: mostrar estrutura do numpy.linalg
    print("\n🔍 Conteúdo de numpy.linalg:")
    print("Funções:", numpy_api['numpy.linalg']['functions'])
    print("Classes:", numpy_api['numpy.linalg']['classes'])
    
    # Exemplo: mostrar algumas funções do namespace principal
    print("\n📌 Algumas funções do namespace principal:")
    print(numpy_api['numpy']['functions'][:20])

    import json
    with open('numpy_api.json', 'w') as f:
        json.dump(numpy_api, f, indent=2)

    with open('numpy_api.txt', 'w') as f:
        for namespace, data in numpy_api.items():
            print(f"Namespace: {namespace}\n")
            print(f"Funções: {data}\n")
            
