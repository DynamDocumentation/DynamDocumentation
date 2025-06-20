import inspect
import importlib
import json
from typing import Dict, List

def get_function_signature(func) -> str:
    """Extrai a assinatura da função no formato 'nome(arg1, arg2, ...)'."""
    try:
        sig = inspect.signature(func)
        return f"{func.__name__}{sig}"
    except (ValueError, TypeError):
        return f"{func.__name__}(...)"  # Fallback para funções em C

def parse_docstring(doc: str) -> dict:
    """
    Parse a estilo NumPy/Sphinx no docstring, segmentando em seções
    como 'Parameters', 'Returns', etc.
    """
    import re

    if not doc:
        return {}

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
    param_desc = []

    def finalize_param():
        nonlocal param_name, param_desc
        if param_name:
            if param_name not in result["parameters"]:
                result["parameters"][param_name] = {
                    "type": "",
                    "description": ""
                }
            current = result["parameters"][param_name]["description"]
            joined = (current + "\n" + "\n".join(param_desc)).strip()
            result["parameters"][param_name]["description"] = joined
        param_name = None
        param_desc = []

    i = 0
    while i < len(lines):
        line = lines[i].rstrip()

        # Se seção reconhecida
        if line in known_sections:
            if current_section == "parameters":
                finalize_param()
            current_section = line.lower()
            if i + 1 < len(lines) and re.match(r'^-+\s*$', lines[i+1]):
                i += 2
                continue
            else:
                i += 1
                continue

        # Processa a seção atual
        if current_section == "description":
            result["description"] += line + "\n"

        elif current_section == "parameters":
            param_match = re.match(r'^(\S+)\s*:\s*(.*)', line)
            if param_match:
                finalize_param()
                param_name = param_match.group(1)
                param_type = param_match.group(2)
                result["parameters"][param_name] = {
                    "type": param_type.strip(),
                    "description": ""
                }
            else:
                if param_name:
                    param_desc.append(line)

        elif current_section == "returns":
            result["returns"] += line + "\n"

        elif current_section == "raises":
            result["raises"] += line + "\n"

        elif current_section == "see also":
            result["see_also"] += line + "\n"

        elif current_section == "notes":
            result["notes"] += line + "\n"

        elif current_section == "examples":
            result["examples"] += line + "\n"

        i += 1

    if current_section == "parameters":
        finalize_param()

    for key, value in result.items():
        if isinstance(value, str):
            result[key] = value.strip()

    return result

def extract_functions(module) -> List[Dict]:
    """Extrai todas as funções públicas do módulo com docstrings parseados."""
    functions = []
    for name in dir(module):
        if name.startswith('_'):
            continue

        obj = getattr(module, name)
        if callable(obj):
            doc = inspect.getdoc(obj) or ""
            functions.append({
                "name": name,
                "signature": get_function_signature(obj),
                "docstring": parse_docstring(doc)
            })
    return sorted(functions, key=lambda x: x["name"])

def extract_classes(module) -> List[Dict]:
    """Extrai todas as classes públicas do módulo e seus métodos."""
    classes = []
    for name, obj in inspect.getmembers(module, inspect.isclass):
        if name.startswith('_'):
            continue

        class_info = {
            "name": name,
            "docstring": parse_docstring(inspect.getdoc(obj) or ""),
            "methods": []
        }
        # Extrai métodos
        for meth_name, meth_obj in inspect.getmembers(obj, callable):
            if meth_name.startswith('_'):
                continue
            method_doc = inspect.getdoc(meth_obj) or ""
            class_info["methods"].append({
                "name": meth_name,
                "signature": get_function_signature(meth_obj),
                "docstring": parse_docstring(method_doc)
            })
        classes.append(class_info)

    return sorted(classes, key=lambda x: x["name"])

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

    return {
        "description": inspect.getdoc(module) or "",
        "functions": extract_functions(module),
        "classes": extract_classes(module),
        "constants": extract_constants(module)
    }

if __name__ == "__main__":
    # Altere esta lista de módulos conforme necessário:
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

    # Extrai e salva para cada módulo
    for targ in TARGET_MODULES:
        sklearn_api = extract_module_api(targ)
        with open(f'{targ.replace(".","_")}.json', 'w') as f:
            json.dump(sklearn_api, f, indent=2)

    print("\n✅ JSONs de cada submódulo do sklearn foram gerados com sucesso!")
