# filepath: /home/espala/Exercicios/EPs/DynamDocumentation/population/pop_general.py
import inspect
import importlib
import json
import os
import sys
import re
import warnings
from typing import Dict, List, Optional, Union, Any, Tuple

def is_torch_module(module_name: str) -> bool:
    """Verifica se estamos processando o PyTorch."""
    return module_name == "torch" or module_name.startswith("torch.")

def get_function_signature(func, is_torch: bool = False) -> str:
    """Extrai a assinatura da função com suporte especial para PyTorch."""
    try:
        sig = inspect.signature(func)
        return f"{func.__name__}{sig}"
    except (ValueError, TypeError):
        # Para funções em C/Cython, tenta extrair da docstring
        doc = inspect.getdoc(func) or ""
        
        # Estratégia 1: Tentar encontrar a assinatura na primeira linha (comum em NumPy e PyTorch)
        first_line = doc.split('\n')[0].strip()
        match = re.match(r'^([a-zA-Z0-9_]+)\((.*?)\)', first_line)
        if match:
            return f"{func.__name__}({match.group(2)})"
            
        # Estratégia 2 (PyTorch): Procurar por uma assinatura após o nome da função
        if is_torch:
            torch_sig_pattern = fr'{func.__name__}\s*\((.*?)\)\s*->'
            torch_match = re.search(torch_sig_pattern, doc)
            if torch_match:
                return f"{func.__name__}({torch_match.group(1)})"
            
            # Estratégia 3 (PyTorch): Procurar linha "Args:" e extrair parâmetros
            args_match = re.search(r'Args:\s*\n(.*?)(?:\n\n|\n[A-Z]|\Z)', doc, re.DOTALL)
            if args_match:
                args_text = args_match.group(1).strip()
                params = []
                for line in args_text.split('\n'):
                    param_match = re.match(r'^\s*([a-zA-Z0-9_]+)[\s:]', line)
                    if param_match:
                        params.append(param_match.group(1))
                if params:
                    return f"{func.__name__}({', '.join(params)})"
        
        # Fallback
        return f"{func.__name__}(...)"

def parse_torch_docstring(doc: str) -> Dict[str, Any]:
    """Parser especializado para docstrings do PyTorch."""
    if not doc:
        return {
            "description": "",
            "parameters": {},
            "returns": "",
            "raises": "",
            "see_also": "",
            "notes": "",
            "examples": ""
        }
    
    result = {
        "description": "",
        "parameters": {},
        "returns": "",
        "raises": "",
        "see_also": "",
        "notes": "",
        "examples": ""
    }
    
    # Extrai a descrição (tudo antes de Args:, Returns:, etc.)
    desc_match = re.search(r'^(.*?)(?:\n\s*(?:Args|Arguments|Parameters|Returns|Raises|Examples|Note|Warning):|$)', doc, re.DOTALL)
    if desc_match:
        result["description"] = desc_match.group(1).strip()
    
    # Extrai parâmetros
    args_match = re.search(r'(?:Args|Arguments|Parameters):\s*\n(.*?)(?:\n\s*(?:Returns|Raises|Examples|Note|Warning):|$)', doc, re.DOTALL)
    if args_match:
        args_text = args_match.group(1)
        current_param = None
        current_type = ""
        current_desc = []
        
        for line in args_text.split('\n'):
            line = line.strip()
            if not line:
                continue
                
            # Detecta novo parâmetro (formatos diversos do PyTorch)
            param_match = re.match(r'^([a-zA-Z0-9_]+)(?:\s*\(([^)]*)\))?(?:\s*:)?\s*(.*)$', line)
            
            if param_match:
                # Salva parâmetro anterior
                if current_param:
                    result["parameters"][current_param] = {
                        "type": current_type.strip(),
                        "description": '\n'.join(current_desc).strip()
                    }
                
                # Novo parâmetro
                current_param = param_match.group(1)
                current_type = param_match.group(2) or ""
                current_desc = [param_match.group(3) or ""]
            elif current_param:
                # Continua descrição do parâmetro atual
                current_desc.append(line)
        
        # Salva o último parâmetro
        if current_param:
            result["parameters"][current_param] = {
                "type": current_type.strip(),
                "description": '\n'.join(current_desc).strip()
            }
    
    # Extrai returns
    returns_match = re.search(r'Returns:\s*\n(.*?)(?:\n\s*(?:Raises|Examples|Note|Warning):|$)', doc, re.DOTALL)
    if returns_match:
        result["returns"] = returns_match.group(1).strip()
    
    # Extrai raises
    raises_match = re.search(r'Raises:\s*\n(.*?)(?:\n\s*(?:Examples|Note|Warning):|$)', doc, re.DOTALL)
    if raises_match:
        result["raises"] = raises_match.group(1).strip()
    
    # Extrai exemplos
    examples_match = re.search(r'(?:Example|Examples):\s*\n(.*?)(?:\n\s*(?:Note|Warning):|$)', doc, re.DOTALL)
    if examples_match:
        result["examples"] = examples_match.group(1).strip()
    
    # Extrai notas
    notes_match = re.search(r'(?:Note|Notes):\s*\n(.*?)(?:\n\s*(?:Warning|Example|Examples):|$)', doc, re.DOTALL)
    if notes_match:
        result["notes"] = notes_match.group(1).strip()
    
    return result

def parse_docstring(doc: Optional[str], is_torch: bool = False) -> dict:
    """
    Parse uma docstring no estilo NumPy/Sphinx ou PyTorch.
    
    Args:
        doc: A docstring para analisar
        is_torch: Se True, usa um parser especializado para o formato PyTorch
    """
    if not doc:
        # Docstring vazia ou None
        return {
            "description": "",
            "parameters": {},
            "returns": "",
            "raises": "",
            "see_also": "",
            "notes": "",
            "examples": ""
        }
    
    # Para PyTorch, use o parser especializado
    if is_torch:
        return parse_torch_docstring(doc)

    # Código existente para outras bibliotecas
    known_sections = [
        "Parameters",
        "Returns",
        "Raises",
        "See Also",
        "Notes",
        "Examples"
    ]

    result = {
        "description": "",
        "parameters": {},
        "returns": "",
        "raises": "",
        "see_also": "",
        "notes": "",
        "examples": ""
    }

    lines = doc.split('\n')
    current_section = "description"
    param_name = None
    param_type = ""
    param_desc: List[str] = []

    def finalize_param():
        """Guarda o texto coletado para o parâmetro atual e reinicia."""
        nonlocal param_name, param_type, param_desc
        if param_name:
            result["parameters"][param_name] = {
                "type": param_type.strip(),
                "description": "\n".join(param_desc).strip()
            }
        # Reset
        param_name = None
        param_type = ""
        param_desc = []

    i = 0
    while i < len(lines):
        line = lines[i].strip()
        
        # Verificar se é início de seção
        is_section = False
        for section in known_sections:
            if line.startswith(section) and (len(line) == len(section) or line[len(section)] in [':', ' ', '\t', '-']):
                # Se estávamos processando um parâmetro, finalize-o
                if current_section == "parameters" and param_name:
                    finalize_param()
                
                current_section = section.lower().replace(' ', '_')
                is_section = True
                break
                
        if is_section:
            i += 1
            continue
        
        # Processamento específico para a seção Parameters
        if current_section == "parameters":
            # Tenta identificar um novo parâmetro
            # Formato 1: param_name : param_type
            param_match = re.match(r'^([a-zA-Z0-9_\.\[\]]+)\s*:\s*(.*?)$', line)
            # Formato 2: param_name -- descrição
            if not param_match:
                param_match = re.match(r'^([a-zA-Z0-9_\.\[\]]+)\s+--\s+(.*?)$', line)
            
            if param_match:
                # Se já estávamos processando um parâmetro, finalize-o
                if param_name:
                    finalize_param()
                
                param_name = param_match.group(1)
                if param_match.group(2):
                    # Se capturamos tipo ou descrição
                    if current_section == "parameters" and ":" in line:
                        param_type = param_match.group(2)
                    else:
                        param_desc.append(param_match.group(2))
            elif line and param_name:
                # Continuação da descrição de um parâmetro existente
                # Tenta identificar se é uma linha de tipo
                type_match = re.match(r'^\s*([a-zA-Z0-9_\,\s\[\]\(\)\{\}\<\>\|\.\"\'\*]+)$', line)
                if not param_type and type_match and not param_desc:
                    param_type = type_match.group(1)
                else:
                    param_desc.append(line)
        else:
            # Adiciona a linha à seção atual
            if line:  # Ignora linhas vazias
                current_text = result[current_section]
                if current_text:
                    result[current_section] = current_text + "\n" + line
                else:
                    result[current_section] = line
        
        i += 1
    
    # Finaliza o último parâmetro, se houver
    if current_section == "parameters" and param_name:
        finalize_param()
    
    return result

def safe_extract(module, name):
    """Extrai um objeto do módulo de forma segura, lidando com erros."""
    try:
        return getattr(module, name)
    except (AttributeError, TypeError):
        return None
    except Exception as e:
        warnings.warn(f"Erro ao acessar {name} em {module.__name__}: {str(e)}")
        return None

def extract_functions(module, module_name: str) -> List[Dict]:
    """Extrai todas as funções públicas do módulo com docstrings parseados."""
    functions = []
    is_torch = is_torch_module(module_name)
    
    for name in dir(module):
        # Ignora funções internas e privadas
        if name.startswith('_'):
            continue
        
        try:
            obj = safe_extract(module, name)
            if obj is None:
                continue
                
            # Verifica se é uma função ou builtin
            if inspect.isfunction(obj) or inspect.isbuiltin(obj) or callable(obj):
                # Ignora se for uma classe
                if inspect.isclass(obj):
                    continue
                    
                doc = inspect.getdoc(obj) or ""
                signature = get_function_signature(obj, is_torch=is_torch)
                parsed_doc = parse_docstring(doc, is_torch=is_torch)
                
                functions.append({
                    "name": name,
                    "signature": signature,
                    "documentation": parsed_doc
                })
        except Exception as e:
            warnings.warn(f"Erro ao processar função {name}: {str(e)}")
    
    return sorted(functions, key=lambda x: x["name"])

def extract_methods(cls, module_name: str) -> List[Dict]:
    """Extrai todos os métodos de uma classe."""
    methods = []
    is_torch = is_torch_module(module_name)
    
    for name in dir(cls):
        if name.startswith('_'):
            continue
            
        try:
            method = safe_extract(cls, name)
            if method is None:
                continue
                
            # Checa se é um método, função ou callable
            if inspect.isfunction(method) or inspect.ismethod(method) or inspect.isbuiltin(method) or callable(method):
                doc = inspect.getdoc(method) or ""
                signature = get_function_signature(method, is_torch=is_torch)
                parsed_doc = parse_docstring(doc, is_torch=is_torch)
                
                methods.append({
                    "name": name,
                    "signature": signature,
                    "documentation": parsed_doc
                })
        except Exception as e:
            warnings.warn(f"Erro ao processar método {name}: {str(e)}")
    
    return sorted(methods, key=lambda x: x["name"])

def extract_classes(module, module_name: str) -> List[Dict]:
    """Extrai todas as classes públicas do módulo e seus métodos."""
    classes = []
    is_torch = is_torch_module(module_name)
    
    for name in dir(module):
        if name.startswith('_'):
            continue
            
        try:
            obj = safe_extract(module, name)
            if obj is None:
                continue
                
            if inspect.isclass(obj):
                doc = inspect.getdoc(obj) or ""
                parsed_doc = parse_docstring(doc, is_torch=is_torch)
                methods = extract_methods(obj, module_name)
                
                classes.append({
                    "name": name,
                    "documentation": parsed_doc,
                    "methods": methods
                })
        except Exception as e:
            warnings.warn(f"Erro ao processar classe {name}: {str(e)}")
    
    return sorted(classes, key=lambda x: x["name"])

def extract_module_api(module_name: str) -> Dict:
    """
    Retorna as informações de API de um módulo.
    """
    print(f"Extraindo API de {module_name}...")
    try:
        module = importlib.import_module(module_name)
    except ImportError as e:
        return {
            "error": f"Could not import module {module_name}: {str(e)}"
        }

    description = inspect.getdoc(module) or "No description available"
    is_torch = is_torch_module(module_name)
    
    # Extrair funções e classes
    functions = extract_functions(module, module_name)
    classes = extract_classes(module, module_name)
    
    print(f"  Encontradas {len(functions)} funções e {len(classes)} classes")
    
    # Para o PyTorch, tentamos extrair atributos importantes 
    # que podem não ser detectados automaticamente
    if is_torch:
        print(f"  Modo PyTorch: processando APIs específicas...")
        try:
            # O PyTorch tem muitas funções que são acessadas diretamente
            # como atributos do módulo principal torch._C
            if module_name == "torch" and hasattr(module, "_C"):
                for name in dir(module._C):
                    if not name.startswith('_'):
                        try:
                            obj = getattr(module._C, name)
                            if callable(obj) and not inspect.isclass(obj):
                                doc = inspect.getdoc(obj) or ""
                                if doc and name not in [f["name"] for f in functions]:
                                    functions.append({
                                        "name": name,
                                        "signature": f"{name}(...)",
                                        "documentation": parse_docstring(doc, is_torch=True)
                                    })
                        except:
                            pass
        except Exception as e:
            warnings.warn(f"Erro ao processar APIs específicas do PyTorch: {str(e)}")
    
    return {
        "description": description,
        "functions": functions,
        "classes": classes
    }

def build_module_structure(library_name: str) -> Dict[str, Dict]:
    """Constrói a estrutura completa de módulos de uma biblioteca."""
    result = {}
    
    print(f"Processando biblioteca: {library_name}")
    
    # Tenta importar o módulo principal
    try:
        main_module = importlib.import_module(library_name)
    except ImportError as e:
        return {"error": f"Could not import library {library_name}: {str(e)}"}
    
    # Cria o diretório de saída para JSON, se não existir
    output_dir = os.path.join("output", library_name)
    os.makedirs(output_dir, exist_ok=True)
    
    # Documenta o módulo principal
    main_structure = extract_module_api(library_name)
    result[library_name] = main_structure
    
    # Salva o JSON do módulo principal
    with open(os.path.join(output_dir, f"{library_name}.json"), "w", encoding='utf-8') as f:
        json.dump(main_structure, f, indent=2, ensure_ascii=False)
    
    # Tenta descobrir submódulos (se a biblioteca for um pacote com __path__)
    try:
        import pkgutil
        if hasattr(main_module, '__path__'):
            package_path = main_module.__path__
            for _, name, is_pkg in pkgutil.iter_modules(package_path):
                if not name.startswith('_'):  # ignora submódulos "privados"
                    submodule_name = f"{library_name}.{name}"
                    try:
                        submodule_structure = extract_module_api(submodule_name)
                        result[submodule_name] = submodule_structure

                        # Salva o JSON do submódulo
                        with open(os.path.join(output_dir, f"{name}.json"), "w", encoding='utf-8') as f:
                            json.dump(submodule_structure, f, indent=2, ensure_ascii=False)

                        print(f"  Processado submódulo: {submodule_name}")
                    except Exception as e:
                        print(f"  Erro ao processar submódulo {submodule_name}: {str(e)}")
    
            # Para PyTorch, processamos alguns módulos especiais
            if is_torch_module(library_name):
                special_modules = ["nn", "optim", "autograd", "cuda", "linalg"]
                for name in special_modules:
                    submodule_name = f"{library_name}.{name}"
                    if submodule_name not in result:
                        try:
                            print(f"  Processando módulo especial do PyTorch: {submodule_name}")
                            submodule_structure = extract_module_api(submodule_name)
                            result[submodule_name] = submodule_structure

                            # Salva o JSON do submódulo
                            with open(os.path.join(output_dir, f"{name}.json"), "w", encoding='utf-8') as f:
                                json.dump(submodule_structure, f, indent=2, ensure_ascii=False)
                        except Exception as e:
                            print(f"  Erro ao processar módulo especial {submodule_name}: {str(e)}")
    
    except (AttributeError, ImportError) as e:
        print(f"  Aviso: não foi possível descobrir submódulos: {str(e)}")
    
    # Gera índice global
    index = {
        "library": library_name,
        "modules": list(result.keys())
    }
    with open(os.path.join(output_dir, "index.json"), "w", encoding='utf-8') as f:
        json.dump(index, f, indent=2, ensure_ascii=False)

    return result

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python pop_general.py <library_name>")
        sys.exit(1)
    
    library_name = sys.argv[1]
    
    try:
        structure = build_module_structure(library_name)
        print(f"Documentação gerada com sucesso para {library_name}!")
    except Exception as e:
        print(f"Erro ao gerar documentação: {str(e)}")
        sys.exit(1)