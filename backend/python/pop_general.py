import inspect
import importlib
import json
import os
import sys
import re
import warnings
from typing import Dict, List, Optional, Union, Any, Tuple

# Library detection functions
def is_torch_module(module_name: str) -> bool:
    """Verifica se estamos processando o PyTorch."""
    return module_name == "torch" or module_name.startswith("torch.")

def is_jax_module(module_name: str) -> bool:
    """Verifica se estamos processando o JAX."""
    return module_name == "jax" or module_name.startswith("jax.")

def is_tensorflow_module(module_name: str) -> bool:
    """Verifica se estamos processando o TensorFlow."""
    return module_name == "tensorflow" or module_name.startswith("tensorflow.") or \
           module_name == "tf" or module_name.startswith("tf.")

def is_numpy_module(module_name: str) -> bool:
    """Verifica se estamos processando o NumPy."""
    return module_name == "numpy" or module_name.startswith("numpy.")

def is_requests_module(module_name: str) -> bool:
    """Verifica se estamos processando o Requests."""
    return module_name == "requests" or module_name.startswith("requests.")

def is_matplotlib_module(module_name: str) -> bool:
    """Verifica se estamos processando o Matplotlib."""
    return module_name == "matplotlib" or module_name.startswith("matplotlib.")

def is_seaborn_module(module_name: str) -> bool:
    """Verifica se estamos processando o Seaborn."""
    return module_name == "seaborn" or module_name.startswith("seaborn.")

def get_function_signature(func, library_type: str = "") -> str:
    """
    Tenta extrair assinatura, mesmo para funções geradas em C/Cython.
    Se não encontrar, retorna um fallback.
    """
    try:
        sig = inspect.signature(func)
        return f"{func.__name__}{sig}"
    except (ValueError, TypeError):
        # Para wrappers C/C++, tentamos extrair da docstring (às vezes há algo ali)
        doc = getattr(func, "__doc__", "") or ""
        first_line = doc.split('\n', 1)[0].strip()
        
        # Pattern melhorado para capturar várias formas de assinaturas em docstrings
        patterns = [
            r'^([a-zA-Z0-9_\.]+)\((.*?)\)',  # func(args)
            r'^([a-zA-Z0-9_\.]+)\s*\((.*?)\)',  # func (args)
            r'^(?:[a-zA-Z0-9_\.]+\.)?([a-zA-Z0-9_]+)\((.*?)\)',  # module.func(args)
            r'^(?:[a-zA-Z0-9_\.]+\.)?([a-zA-Z0-9_]+)\s*\((.*?)\)'  # module.func (args)
        ]
        
        for pattern in patterns:
            match = re.search(pattern, first_line)
            if match:
                return f"{func.__name__}({match.group(2)})"
        
        # Fallback específico para bibliotecas com formatos conhecidos
        if is_tensorflow_module(library_type):
            match = re.search(r'tf\.(?:[a-zA-Z0-9_\.]+\.)?([a-zA-Z0-9_]+)\((.*?)\)', doc)
            if match:
                return f"{func.__name__}({match.group(2)})"
        
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
    
    desc_match = re.search(
        r'^(.*?)(?:\n\s*(?:Args|Arguments|Parameters|Returns|Raises|Examples|Note|Warning):|$)',
        doc, re.DOTALL)
    if desc_match:
        result["description"] = desc_match.group(1).strip()
    
    # Args/Parameters
    args_match = re.search(
        r'(?:Args|Arguments|Parameters):\s*\n(.*?)(?:\n\s*(?:Returns|Raises|Examples|Note|Warning):|$)',
        doc, re.DOTALL)
    if args_match:
        args_text = args_match.group(1)
        current_param = None
        current_type = ""
        current_desc = []
        
        for line in args_text.split('\n'):
            line = line.strip()
            if not line:
                continue
            
            param_match = re.match(r'^([a-zA-Z0-9_]+)(?:\s*\(([^)]*)\))?(?:\s*:)?\s*(.*)$', line)
            if param_match:
                if current_param:
                    result["parameters"][current_param] = {
                        "type": current_type.strip(),
                        "description": "\n".join(current_desc).strip()
                    }
                current_param = param_match.group(1)
                current_type = param_match.group(2) or ""
                current_desc = [param_match.group(3) or ""]
            elif current_param:
                current_desc.append(line)
        
        if current_param:
            result["parameters"][current_param] = {
                "type": current_type.strip(),
                "description": "\n".join(current_desc).strip()
            }
    
    # Returns
    returns_match = re.search(r'Returns:\s*\n(.*?)(?:\n\s*(?:Raises|Examples|Note|Warning):|$)', doc, re.DOTALL)
    if returns_match:
        result["returns"] = returns_match.group(1).strip()
    
    # Raises
    raises_match = re.search(r'Raises:\s*\n(.*?)(?:\n\s*(?:Examples|Note|Warning):|$)', doc, re.DOTALL)
    if raises_match:
        result["raises"] = raises_match.group(1).strip()
    
    # Examples
    examples_match = re.search(r'(?:Example|Examples):\s*\n(.*?)(?:\n\s*(?:Note|Warning):|$)', doc, re.DOTALL)
    if examples_match:
        result["examples"] = examples_match.group(1).strip()
    
    # Notes
    notes_match = re.search(r'(?:Note|Notes):\s*\n(.*?)(?:\n\s*(?:Warning|Example|Examples):|$)', doc, re.DOTALL)
    if notes_match:
        result["notes"] = notes_match.group(1).strip()
    
    return result

import textwrap

def dedent_doc(text: str) -> str:
    """
    Remove all leading indentation from each line in a multi-line string.
    
    This is more aggressive than textwrap.dedent - it removes ALL leading
    whitespace from every line, ensuring no line has leading spaces or tabs.
    """
    if not text:
        return text
        
    lines = text.split('\n')
    result = []
    
    for line in lines:
        # Keep empty lines as-is
        if not line.strip():
            result.append('')
            continue
            
        # Remove ALL leading whitespace from each non-empty line
        result.append(line.strip())
    
    return '\n'.join(result)

def parse_numpy_tensorflow_style(doc: str) -> Dict[str, Any]:
    """
    Parser for NumPy/TensorFlow/scikit-learn style docstrings.
    
    This function specifically targets scikit-learn's format with 
    indentation-based parameter blocks and section headers with dashes.
    
    Removes common leading indentation from all text fields for better
    display in frontend interfaces.
    """
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
    
    # First step: extract the description part (everything before Parameters section)
    # Look for the first section header
    params_match = re.search(r'\n\s*Parameters\s*\n\s*[-]+\s*\n', doc)
    
    if params_match:
        # Description is everything before the Parameters section
        result["description"] = dedent_doc(doc[:params_match.start()].strip())
        
        # Now let's extract all section blocks
        sections = re.split(r'\n\s*([A-Za-z][A-Za-z\s]*)\s*\n\s*[-]+\s*\n', doc[params_match.start():])
        
        # Process the extracted sections
        current_section = None
        for i, section in enumerate(sections):
            if i % 2 == 0 and current_section:
                # This is section content
                if current_section == "Parameters":
                    # Parse parameters - this is special handling for scikit-learn style
                    lines = section.split('\n')
                    current_param = None
                    current_type = ""
                    current_desc = []
                    
                    for line in lines:
                        # Check for parameter definition (not indented, has colon)
                        param_match = re.match(r'^([a-zA-Z0-9_]+)\s*:\s*(.*)$', line.strip())
                        if param_match:
                            # If we were processing a previous parameter, save it
                            if current_param:
                                result["parameters"][current_param] = {
                                    "type": current_type,
                                    "description": dedent_doc('\n'.join(current_desc).strip())
                                }
                            
                            # Start a new parameter
                            current_param = param_match.group(1)
                            current_type = param_match.group(2)
                            current_desc = []
                        elif line.strip() and current_param:
                            # This is a continuation of the description (indented)
                            if line.startswith('    '):  # Indented line
                                current_desc.append(line.strip())
                    
                    # Don't forget the last parameter
                    if current_param:
                        result["parameters"][current_param] = {
                            "type": current_type,
                            "description": dedent_doc('\n'.join(current_desc).strip())
                        }
                else:
                    # For non-parameter sections, just store the whole text
                    result_key = current_section.lower().replace(' ', '_')
                    result[result_key] = dedent_doc(section.strip())
            else:
                # This is a section header
                current_section = section
    else:
        # No Parameters section found, the entire text is the description
        result["description"] = dedent_doc(doc.strip())
        
        # Try to find other sections with different format (Google style)
        for section_name, pattern in [
            ("parameters", r'Parameters\s*:(.*?)(?=\n\s*[A-Z][a-z]+\s*:|\Z)'),
            ("returns", r'Returns\s*:(.*?)(?=\n\s*[A-Z][a-z]+\s*:|\Z)'),
            ("raises", r'Raises\s*:(.*?)(?=\n\s*[A-Z][a-z]+\s*:|\Z)'),
            ("see_also", r'See Also\s*:(.*?)(?=\n\s*[A-Z][a-z]+\s*:|\Z)'),
            ("notes", r'Notes\s*:(.*?)(?=\n\s*[A-Z][a-z]+\s*:|\Z)'),
            ("examples", r'Examples\s*:(.*?)(?=\n\s*[A-Z][a-z]+\s*:|\Z)')
        ]:
            match = re.search(pattern, doc, re.DOTALL)
            if match:
                if section_name == "parameters":
                    # Process parameters
                    param_text = match.group(1)
                    lines = param_text.split('\n')
                    current_param = None
                    current_desc = []
                    
                    for line in lines:
                        if not line.strip():
                            continue
                            
                        if line.lstrip() == line:  # Not indented - new parameter
                            if current_param:
                                result["parameters"][current_param] = {
                                    "type": "",
                                    "description": dedent_doc('\n'.join(current_desc).strip())
                                }
                            
                            parts = line.split(':', 1)
                            if len(parts) > 1:
                                current_param = parts[0].strip()
                                current_desc = [parts[1].strip()]
                            else:
                                current_param = line.strip()
                                current_desc = []
                        else:  # Indented - continuation of description
                            current_desc.append(line.strip())
                    
                    # Don't forget the last parameter
                    if current_param:
                        result["parameters"][current_param] = {
                            "type": "",
                            "description": dedent_doc('\n'.join(current_desc).strip())
                        }
                else:
                    # For other sections, just store the text
                    result[section_name] = dedent_doc(match.group(1).strip())
    
    # Final cleanup: if description section contains a Parameters section, truncate it
    if result["description"]:
        param_start = re.search(r'\n\s*Parameters\s*\n\s*[-]+\s*\n', result["description"])
        if param_start:
            result["description"] = dedent_doc(result["description"][:param_start.start()].strip())
    
    # If we detected Parameters in the description but failed to parse them properly, try one more time
    if not result["parameters"]:
        params_match = re.search(r'Parameters\s*\n\s*[-]+\s*\n(.*?)(?:\n\s*[A-Z][a-z]+\s*\n\s*[-]+\s*\n|\Z)', doc, re.DOTALL)
        if params_match:
            param_section = params_match.group(1)
            param_blocks = re.split(r'\n(?=\S)', param_section)
            
            for block in param_blocks:
                lines = block.split('\n')
                if not lines:
                    continue
                    
                # First line contains the parameter name and type
                first_line = lines[0].strip()
                param_match = re.match(r'^([a-zA-Z0-9_]+)\s*:\s*(.*)$', first_line)
                
                if param_match:
                    param_name = param_match.group(1)
                    param_type = param_match.group(2)
                    
                    # Remaining lines are the description
                    if len(lines) > 1:
                        param_desc = '\n'.join(line.strip() for line in lines[1:] if line.strip())
                    else:
                        param_desc = ""
                    
                    result["parameters"][param_name] = {
                        "type": param_type,
                        "description": dedent_doc(param_desc)
                    }
    
    return result
    
    # Extract other sections
    for section_name, section_key in [
        ("Returns", "returns"),
        ("Raises", "raises"),
        ("See Also", "see_also"),
        ("Notes", "notes"),
        ("Examples", "examples")
    ]:
        for pattern in [
            fr'{section_name}\s*\n\s*[-]+\s*\n(.*?)(?=\n\s*[A-Z][a-z]+\s*\n\s*[-]+\s*\n|\Z)',
            fr'{section_name}\s*:(.*?)(?=\n\s*[A-Z][a-z]+\s*:|\Z)'
        ]:
            match = re.search(pattern, doc, re.DOTALL)
            if match:
                result[section_key] = match.group(1).strip()
                break
    
    # If we still don't have parameters, try Google-style parsing as fallback
    if not result["parameters"]:
        args_match = re.search(r'(?:Args|Arguments|Parameters):\s*\n(.*?)(?:\n\s*(?:Returns|Raises|Examples|Notes):|$)', doc, re.DOTALL)
        if args_match:
            args_text = args_match.group(1)
            current_param = None
            current_type = ""
            current_desc = []
            
            for line in args_text.split('\n'):
                line = line.strip()
                if not line:
                    continue
                
                # Match parameter definition lines
                param_match = re.match(r'^([a-zA-Z0-9_]+)(?:\s*\(([^)]*)\))?(?:\s*:)?\s*(.*)$', line)
                if param_match:
                    # Save previous parameter if exists
                    if current_param:
                        result["parameters"][current_param] = {
                            "type": current_type.strip(),
                            "description": "\n".join(current_desc).strip()
                        }
                    
                    # Start new parameter
                    current_param = param_match.group(1)
                    current_type = param_match.group(2) or ""
                    current_desc = [param_match.group(3) or ""]
                elif current_param:
                    # Continue with previous parameter description
                    current_desc.append(line)
            
            # Save last parameter
            if current_param:
                result["parameters"][current_param] = {
                    "type": current_type.strip(),
                    "description": "\n".join(current_desc).strip()
                }
    
    return result

def parse_jax_docstring(doc: str) -> Dict[str, Any]:
    """
    Parser específico para docstrings do JAX.
    """
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
    
    # Extrai descrição (até a primeira seção)
    sections = re.split(r'\n\s*(?:Args|Arguments|Parameters|Returns|Raises|Examples|Notes|See Also):', doc)
    if sections:
        result["description"] = sections[0].strip()
    
    # Parâmetros no formato JAX
    params_match = re.search(r'(?:Args|Arguments|Parameters):(.*?)(?:\n\s*(?:Returns|Raises|Examples|Notes|See Also):|$)', doc, re.DOTALL)
    if params_match:
        params_text = params_match.group(1)
        # JAX usa diversos formatos para parâmetros
        current_param = None
        current_type = ""
        current_desc = []
        
        for line in params_text.split('\n'):
            stripped_line = line.strip()
            if not stripped_line:
                continue
            
            # Verifica se é um novo parâmetro
            indent = len(line) - len(line.lstrip())
            
            # Pattern mais comum no JAX: parameter_name: parameter_type
            param_match = re.match(r'^([a-zA-Z0-9_]+)\s*:\s*([^-\s][^-]*?)(?:\s*-\s*(.*))?$', stripped_line)
            
            # Alternativa: parameter_name (param_type): description
            if not param_match:
                param_match = re.match(r'^([a-zA-Z0-9_]+)\s*\(([^)]*)\)(?:\s*:)?\s*(.*)$', stripped_line)
            
            # Alternativa: parameter_name -- description
            if not param_match:
                param_match = re.match(r'^([a-zA-Z0-9_]+)\s*--\s*(.*)$', stripped_line)
                if param_match:
                    param_match = (param_match.group(1), "", param_match.group(2))
            
            if param_match and (indent == 0 or current_param is None):
                # Finalize o parâmetro anterior
                if current_param:
                    result["parameters"][current_param] = {
                        "type": current_type.strip(),
                        "description": "\n".join(current_desc).strip()
                    }
                
                current_param = param_match.group(1)
                if len(param_match.groups()) > 1:
                    current_type = param_match.group(2) or ""
                else:
                    current_type = ""
                
                if len(param_match.groups()) > 2 and param_match.group(3):
                    current_desc = [param_match.group(3)]
                else:
                    current_desc = []
            elif current_param and indent > 0:
                # Linha de continuação para a descrição
                current_desc.append(stripped_line)
        
        # Finaliza o último parâmetro
        if current_param:
            result["parameters"][current_param] = {
                "type": current_type.strip(),
                "description": "\n".join(current_desc).strip()
            }
    
    # Returns
    returns_match = re.search(r'Returns:(.*?)(?:\n\s*(?:Raises|Examples|Notes|See Also):|$)', doc, re.DOTALL)
    if returns_match:
        result["returns"] = returns_match.group(1).strip()
    
    # Raises
    raises_match = re.search(r'Raises:(.*?)(?:\n\s*(?:Examples|Notes|See Also):|$)', doc, re.DOTALL)
    if raises_match:
        result["raises"] = raises_match.group(1).strip()
    
    # Examples
    examples_match = re.search(r'Examples:(.*?)(?:\n\s*(?:Notes|See Also):|$)', doc, re.DOTALL)
    if examples_match:
        result["examples"] = examples_match.group(1).strip()
    
    # Notes
    notes_match = re.search(r'Notes:(.*?)(?:\n\s*(?:Examples|See Also):|$)', doc, re.DOTALL)
    if notes_match:
        result["notes"] = notes_match.group(1).strip()
    
    # See Also
    see_also_match = re.search(r'See Also:(.*?)(?:\n\s*(?:Examples|Notes):|$)', doc, re.DOTALL)
    if see_also_match:
        result["see_also"] = see_also_match.group(1).strip()
    
    return result

def parse_requests_docstring(doc: str) -> Dict[str, Any]:
    """
    Parser especializado para docstrings do Requests.
    """
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
    
    # Requests usa muitos docstrings no formato :param param_name: description
    result["description"] = re.sub(r':(?:param|return|returns|raises|rtype).*$', '', doc, flags=re.MULTILINE).strip()
    
    # Extrai parâmetros no formato :param name: description
    param_matches = re.finditer(r':param\s+([^:]+):\s*(.*?)(?=\n\s*:(?:param|return|returns|raises|rtype)|$)', doc, re.DOTALL)
    for match in param_matches:
        param_name = match.group(1).strip()
        param_desc = match.group(2).strip()
        
        # Tenta extrair tipo se estiver presente no nome do parâmetro
        type_match = re.match(r'^([^\s]+)\s+(.+)$', param_name)
        if type_match:
            param_type = type_match.group(1)
            param_name = type_match.group(2)
        else:
            param_type = ""
        
        result["parameters"][param_name] = {
            "type": param_type,
            "description": param_desc
        }
    
    # Return
    return_match = re.search(r':(?:return|returns):\s*(.*?)(?=\n\s*:(?:param|raises|rtype)|$)', doc, re.DOTALL)
    if return_match:
        result["returns"] = return_match.group(1).strip()
    
    # Return type
    rtype_match = re.search(r':rtype:\s*(.*?)(?=\n\s*:(?:param|return|returns|raises)|$)', doc, re.DOTALL)
    if rtype_match and "returns" in result:
        result["returns"] = f"{result['returns']} (tipo: {rtype_match.group(1).strip()})"
    
    # Raises
    raises_match = re.search(r':raises:\s*(.*?)(?=\n\s*:(?:param|return|returns|rtype)|$)', doc, re.DOTALL)
    if raises_match:
        result["raises"] = raises_match.group(1).strip()
    
    return result

def parse_generic_docstring(doc: str) -> Dict[str, Any]:
    """
    Parser genérico que tenta lidar com vários estilos de docstrings
    """
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
    
    # Primeiro, tenta encontrar seções principais
    known_sections = ["Parameters", "Args", "Arguments", "Returns", "Return", "Raises", "Exceptions", 
                      "See Also", "Notes", "Note", "Examples", "Example"]
    
    # Divide a docstring em seções
    current_section = "description"
    result["description"] = doc.strip()
    
    for section in known_sections:
        # Diferentes formatos de seções
        patterns = [
            fr'\n\s*{section}:\s*\n',  # NumPy style: Section:\n
            fr'\n\s*{section}\s*\n\s*-+\s*\n',  # NumPy style: Section\n------\n
            fr'\n\s*{section}:',  # Google style: Section:
            fr'\n\s*{section}\s+'  # Section seguido de espaço
        ]
        
        for pattern in patterns:
            section_parts = re.split(pattern, doc, flags=re.IGNORECASE)
            if len(section_parts) > 1:
                # Atualiza a descrição para ser apenas a parte antes da primeira seção
                if current_section == "description":
                    result["description"] = section_parts[0].strip()
                
                # Trata o conteúdo desta seção
                section_content = section_parts[1]
                next_section_match = None
                
                # Encontra onde começa a próxima seção
                for next_section in known_sections:
                    for next_pattern in patterns:
                        match = re.search(next_pattern, section_content, re.IGNORECASE)
                        if match and (next_section_match is None or match.start() < next_section_match.start()):
                            next_section_match = match
                
                # Extrai o conteúdo até a próxima seção
                if next_section_match:
                    section_content = section_content[:next_section_match.start()]
                
                section_key = section.lower().replace(' ', '_')
                
                # Processa parâmetros
                if section.lower() in ["parameters", "args", "arguments"]:
                    # Tenta extrair parâmetros em vários formatos
                    param_lines = section_content.split('\n')
                    current_param = None
                    current_type = ""
                    current_desc = []
                    
                    for line in param_lines:
                        line = line.strip()
                        if not line:
                            continue
                        
                        # Diversos padrões para definição de parâmetros
                        param_patterns = [
                            r'^([a-zA-Z0-9_]+)\s*:\s*([^-]*)(?:\s*-\s*(.*))?$',  # name: type - desc
                            r'^([a-zA-Z0-9_]+)\s*\(([^)]*)\)(?:\s*:)?\s*(.*)$',  # name(type): desc
                            r'^([a-zA-Z0-9_]+)\s+--\s+(.*)$',  # name -- desc
                            r'^([a-zA-Z0-9_]+)\s+(.*)$'  # name desc
                        ]
                        
                        matched = False
                        for param_pattern in param_patterns:
                            param_match = re.match(param_pattern, line)
                            if param_match:
                                if current_param:  # Finaliza o parâmetro anterior
                                    result["parameters"][current_param] = {
                                        "type": current_type.strip(),
                                        "description": "\n".join(current_desc).strip()
                                    }
                                
                                current_param = param_match.group(1)
                                if len(param_match.groups()) > 1:
                                    if param_pattern == r'^([a-zA-Z0-9_]+)\s+--\s+(.*)$':
                                        current_type = ""
                                        current_desc = [param_match.group(2)]
                                    else:
                                        current_type = param_match.group(2) or ""
                                        if len(param_match.groups()) > 2:
                                            current_desc = [param_match.group(3) or ""]
                                        else:
                                            current_desc = []
                                else:
                                    current_type = ""
                                    current_desc = []
                                
                                matched = True
                                break
                        
                        if not matched and current_param:
                            # Linha de continuação
                            current_desc.append(line)
                    
                    # Finaliza o último parâmetro
                    if current_param:
                        result["parameters"][current_param] = {
                            "type": current_type.strip(),
                            "description": "\n".join(current_desc).strip()
                        }
                else:
                    # Outras seções
                    if section_key in ["returns", "return", "raises", "exceptions", "see_also", "notes", "note", "examples", "example"]:
                        if section_key.startswith("return"):
                            section_key = "returns"
                        elif section_key.startswith("raise") or section_key.startswith("exception"):
                            section_key = "raises"
                        elif section_key.startswith("see"):
                            section_key = "see_also"
                        elif section_key.startswith("note"):
                            section_key = "notes"
                        elif section_key.startswith("example"):
                            section_key = "examples"
                        
                        result[section_key] = section_content.strip()
    
    return result

def is_sklearn_module(module_name: str) -> bool:
    """Verifica se estamos processando o scikit-learn."""
    return module_name == "sklearn" or module_name.startswith("sklearn.")

def parse_docstring(doc: Optional[str], module_name: str) -> dict:
    """
    Decide o parser com base na biblioteca, com fallback para parser genérico.
    """
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
    
    # Escolhe o parser especializado com base no módulo
    if is_torch_module(module_name):
        return parse_torch_docstring(doc)
    elif is_jax_module(module_name):
        return parse_jax_docstring(doc)
    elif is_requests_module(module_name):
        return parse_requests_docstring(doc)
    elif is_numpy_module(module_name) or is_tensorflow_module(module_name) or is_sklearn_module(module_name):
        # Use the improved NumPy style parser for scikit-learn too since they use the same docstring format
        return parse_numpy_tensorflow_style(doc)
    else:
        # Parser genérico para outras bibliotecas
        return parse_generic_docstring(doc)

def safe_extract(module, name):
    """Tenta acessar o atributo de forma segura."""
    try:
        return getattr(module, name)
    except (AttributeError, TypeError):
        return None
    except Exception as e:
        warnings.warn(f"Erro ao acessar {name} em {module.__name__}: {str(e)}")
        return None

def extract_functions(module, module_name: str) -> List[Dict]:
    """Extrai todas as funções públicas do módulo com docstrings parseadas."""
    import inspect  # Import local para não quebrar se não for usado
    functions = []
    for name in dir(module):
        if name.startswith('_'):
            continue
        try:
            obj = safe_extract(module, name)
            if obj is None:
                continue
            if inspect.isfunction(obj) or inspect.isbuiltin(obj) or callable(obj):
                if inspect.isclass(obj):
                    continue
                doc = getattr(obj, "__doc__", "") or ""
                signature = get_function_signature(obj, module_name)
                parsed_doc = parse_docstring(doc, module_name)
                functions.append({
                    "name": name,
                    "signature": signature,
                    "documentation": parsed_doc
                })
        except Exception as e:
            warnings.warn(f"Erro ao processar função {name} no módulo {module_name}: {str(e)}")
    return sorted(functions, key=lambda x: x["name"])

def extract_methods(cls, module_name: str) -> List[Dict]:
    import inspect
    methods = []
    for name in dir(cls):
        if name.startswith('_'):
            continue
        try:
            method = safe_extract(cls, name)
            if method is None:
                continue
            if inspect.isfunction(method) or inspect.ismethod(method) or inspect.isbuiltin(method) or callable(method):
                doc = getattr(method, "__doc__", "") or ""
                signature = get_function_signature(method, module_name)
                parsed_doc = parse_docstring(doc, module_name)
                methods.append({
                    "name": name,
                    "signature": signature,
                    "documentation": parsed_doc
                })
        except Exception as e:
            warnings.warn(f"Erro ao processar método {name} em {cls}: {str(e)}")
    return sorted(methods, key=lambda x: x["name"])

def extract_classes(module, module_name: str) -> List[Dict]:
    import inspect
    classes = []
    for name in dir(module):
        if name.startswith('_'):
            continue
        try:
            obj = safe_extract(module, name)
            if obj is None:
                continue
            if inspect.isclass(obj):
                doc = getattr(obj, "__doc__", "") or ""
                parsed_doc = parse_docstring(doc, module_name)
                methods = extract_methods(obj, module_name)
                classes.append({
                    "name": name,
                    "documentation": parsed_doc,
                    "methods": methods
                })
        except Exception as e:
            warnings.warn(f"Erro ao processar classe {name} em {module_name}: {str(e)}")
    return sorted(classes, key=lambda x: x["name"])

def extract_module_api(module_name: str) -> Dict:
    """
    Retorna as informações de API de um módulo, 
    tentando lidar com codegen e wrappers em C++.
    """
    print(f"Extraindo API de {module_name}...")
    try:
        module = importlib.import_module(module_name)
    except ImportError as e:
        return {"error": f"Não foi possível importar {module_name}: {str(e)}"}
    
    description = getattr(module, "__doc__", "") or "No description available"
    functions = extract_functions(module, module_name)
    classes = extract_classes(module, module_name)
    return {
        "description": description.strip(),
        "functions": functions,
        "classes": classes
    }

def build_module_structure(library_name: str) -> Dict[str, Dict]:
    import pkgutil
    
    result = {}
    print(f"Processando biblioteca: {library_name}")
    
    try:
        main_module = importlib.import_module(library_name)
    except ImportError as e:
        return {"error": f"Não foi possível importar {library_name}: {str(e)}"}
    
    output_dir = os.path.join("output", library_name)
    os.makedirs(output_dir, exist_ok=True)
    
    main_structure = extract_module_api(library_name)
    result[library_name] = main_structure
    with open(os.path.join(output_dir, f"{library_name}.json"), "w", encoding='utf-8') as f:
        json.dump(main_structure, f, indent=2, ensure_ascii=False)
    
    # Descobre submódulos
    if hasattr(main_module, '__path__'):
        package_path = main_module.__path__
        for _, name, is_pkg in pkgutil.iter_modules(package_path):
            if name.startswith('_'):
                continue
            submodule_name = f"{library_name}.{name}"
            try:
                submodule_data = extract_module_api(submodule_name)
                result[submodule_name] = submodule_data
                with open(os.path.join(output_dir, f"{name}.json"), "w", encoding='utf-8') as f:
                    json.dump(submodule_data, f, indent=2, ensure_ascii=False)
            except Exception as e:
                print(f"Erro ao processar submódulo {submodule_name}: {str(e)}")
    
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
        print("Uso: python pop_general.py <library_name>")
        sys.exit(1)
    library_name = sys.argv[1]
    try:
        structure = build_module_structure(library_name)
        print(f"Documentação gerada com sucesso para {library_name}!")
    except Exception as e:
        print(f"Erro ao gerar documentação para {library_name}: {str(e)}")
        sys.exit(1)