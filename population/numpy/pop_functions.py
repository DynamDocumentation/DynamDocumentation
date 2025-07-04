import inspect
import numpy as np
import re
from typing import Dict, List, Tuple
import importlib
import json

def get_function_signature(func) -> str:
    """Extrai a assinatura da função no formato 'nome(arg1, arg2, ...)'"""
    try:
        sig = inspect.signature(func)
        return f"{func.__name__}{sig}"
    except (ValueError, TypeError):
        return f"{func.__name__}(...)"  # Fallback para funções em C

import re

def parse_docstring(doc: str) -> dict:
    """
    Parse a NumPy/Sphinx-style docstring into structured sections.
    Safely handle multiline parameters and avoid AttributeError
    for dictionary fields.
    """
    if not doc:
        return {}

    # Known sections in NumPy docstrings
    known_sections = [
        "Parameters",
        "Returns",
        "Raises",
        "See Also",
        "Notes",
        "Examples"
    ]

    # Prepare result structure with default strings or dicts
    result = {
        "description": "",
        "parameters": {},   # stored as a dict per parameter
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
            # Append description
            current = result["parameters"][param_name]["description"]
            joined = (current + "\n" + "\n".join(param_desc)).strip()
            result["parameters"][param_name]["description"] = joined
        param_name = None
        param_desc = []

    i = 0
    while i < len(lines):
        line = lines[i].rstrip()

        # Look for a section header (e.g. "Parameters\n----------")
        if line in known_sections:
            # If leaving 'Parameters', finalize the pending parameter
            if current_section == "parameters":
                finalize_param()
            current_section = line.lower()
            # Next line may be just dashes ("----------"), skip it
            if i + 1 < len(lines) and re.match(r'^-+\s*$', lines[i+1]):
                i += 2
                continue
            else:
                i += 1
                continue

        # Process current section
        if current_section == "description":
            result["description"] += line + "\n"

        elif current_section == "parameters":
            # Parameter lines typically look like "name : type"
            param_match = re.match(r'^(\S+)\s*:\s*(.*)', line)
            if param_match:
                # Found a new parameter; finalize previous
                finalize_param()
                param_name = param_match.group(1)
                param_type = param_match.group(2)
                result["parameters"][param_name] = {
                    "type": param_type.strip(),
                    "description": ""
                }
            else:
                # Possibly a continuation of the description for the current parameter
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

    # Finalize last parameter if any
    if current_section == "parameters":
        finalize_param()

    # Strip trailing newlines from string fields
    for key, value in result.items():
        if isinstance(value, str):
            result[key] = value.strip()

    return result

def extract_functions(module) -> List[Dict]:
    """Extrai todas as funções públicas do módulo"""
    functions = []
    for name in dir(module):
        if name.startswith('_'):
            continue
            
        obj = getattr(module, name)
        if callable(obj):
            try:
                doc = inspect.getdoc(obj) or ""
                functions.append({
                    "name": name,
                    "signature": get_function_signature(obj),
                    "docstring": parse_docstring(doc)
                })
            except Exception as e:
                print(f"⚠️ Error processing function {name}: {str(e)}")
                functions.append({
                    "name": name,
                    "signature": f"{name}(...)",
                    "docstring": {}
                })
    
    return sorted(functions, key=lambda x: x["name"])

def extract_classes(module) -> List[Dict]:
    """Extrai todas as classes públicas do módulo"""
    classes = []
    for name, obj in inspect.getmembers(module, inspect.isclass):
        if name.startswith('_'):
            continue
            
        class_info = {
            "name": name,
            "docstring": parse_docstring(inspect.getdoc(obj) or ""),
            "methods": []
        }
        
        # Extrai métodos da classe
        for meth_name, meth_obj in inspect.getmembers(obj, callable):
            if meth_name.startswith('_'):
                continue
                
            try:
                class_info["methods"].append({
                    "name": meth_name,
                    "signature": get_function_signature(meth_obj),
                    "docstring": parse_docstring(inspect.getdoc(meth_obj) or "")
                })
            except Exception as e:
                print(f"⚠️ Error processing method {name}.{meth_name}: {str(e)}")
        
        classes.append(class_info)
    
    return sorted(classes, key=lambda x: x["name"])

def extract_constants(module) -> List[Dict]:
    """Extrai constantes especiais do módulo"""
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
    """Extrai toda a API de um módulo"""
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

# Exemplo de uso para numpy.linalg

if __name__ == "__main__":
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

    for targ in TARGET_MODULES:
        numpy_api = extract_module_api(targ)
        
        # Exemplo: mostrar estrutura do numpy.linalg
        

        import json
        with open(f'{targ}.json', 'w') as f:
            json.dump(numpy_api, f, indent=2)
        
        
