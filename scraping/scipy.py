import requests as rq
from bs4 import BeautifulSoup
import re
from typing import Dict, List, Any
import json

def extrair_links_toctree_l1(html):
    base_url = "https://scikit-learn.org/stable/api/"
    soup = BeautifulSoup(html, "html.parser")
    
    links = []
    for li in soup.select("li.toctree-l1 > a.reference.internal"):
        href = li.get("href")
        if href:
            links.append(base_url + href)
    
    return links[1:]

def get_names(link):
    link = link.split("/")[-1]
    link = link.replace(".html", "")
    link = link.replace("sklearn.", "")
    return link
    
def extrair_info_modulo(link):
    req = rq.get(link)
    soup = BeautifulSoup(req.text, "html.parser")
    result = {
        "params": "não encontrado",
        "notes": "não encontrado",
        "description": "não encontrado",
        "atributes": "não encontrado",
        "metodos": []
    }

    # Encontra a seção principal do módulo
    article = soup.find("article", class_="bd-article")
    if not article:
        return result

    # Descrição: primeiro <p> depois do <h1>
    h1 = article.find("h1")
    if h1:
        next_p = h1.find_next("p")
        if next_p:
            result["description"] = next_p.get_text(strip=True)

    # Tabela de métodos/classes
    tables = article.find_all("table", class_="autosummary")
    for table in tables:
        links = table.select("td p a.reference.internal")
        for link in links:
            nome = link.get_text(strip=True)
            href = link.get("href", "").strip()
            result["metodos"].append(nome)
            
    return result

def extrair_info_modulo_json(links):
    data = {}
    names = [get_names(link) for link in links]
    for i ,link in enumerate(links):
        data[names[i]] = extrair_info_modulo(link)
    
    with open("scikit_namespace.json", "w") as f:
        json.dump(data, f, indent=4, ensure_ascii=False)

def extract_docs_from_html(html_content: str) -> Dict[str, Any]:
    soup = BeautifulSoup(html_content, 'html.parser')
    result = {
        "class_name": "",
        "class_signature": "",
        "class_description": "",
        "class_parameters": [],
        "methods": [],
        "notes": "",
        "examples": "",
        "see_also": ""
    }
    
    # Extrair nome e assinatura da classe
    class_header = soup.find('dt', class_='sig sig-object py')
    if class_header:
        result["class_name"] = class_header.get('id', '')
        signature = class_header.find('em', class_='property')
        if signature:
            result["class_signature"] = signature.next_sibling.strip()
    
    # Extrair descrição principal
    description = soup.find('dd', class_='field-even')
    if description:
        result["class_description"] = ' '.join(description.stripped_strings)
    
    # Extrair parâmetros da classe
    parameters_section = soup.find('dt', string='Parameters')
    if parameters_section:
        params_dd = parameters_section.find_next('dd')
        if params_dd:
            param_items = params_dd.find_all('li', class_='field')
            for item in param_items:
                param_name = item.find('span', class_='sig-name').get_text() if item.find('span', class_='sig-name') else ''
                param_type = item.find('span', class_='sig-prename').get_text() if item.find('span', class_='sig-prename') else ''
                param_default = item.find('span', class_='sig-paren').get_text() if item.find('span', class_='sig-paren') else ''
                param_desc = item.find('span', class_='sig-paren').next_sibling.strip() if item.find('span', class_='sig-paren') else ''
                
                result["class_parameters"].append({
                    "name": param_name,
                    "type": param_type,
                    "default": param_default,
                    "description": param_desc
                })
    
    # Extrair métodos
    method_defs = soup.find_all('dl', class_='py method')
    for method in method_defs:
        method_name = method.find('dt').get('id', '') if method.find('dt') else ''
        method_sig = method.find('em', class_='property').next_sibling.strip() if method.find('em', class_='property') else ''
        
        method_info = {
            "name": method_name,
            "signature": method_sig,
            "description": "",
            "parameters": [],
            "returns": ""
        }
        
        # Descrição do método
        method_desc = method.find('dd')
        if method_desc:
            method_info["description"] = ' '.join(method_desc.stripped_strings)
        
        # Parâmetros do método
        method_params = method.find('dt', string='Parameters')
        if method_params:
            params_dd = method_params.find_next('dd')
            if params_dd:
                param_items = params_dd.find_all('li', class_='field')
                for item in param_items:
                    param_name = item.find('span', class_='sig-name').get_text() if item.find('span', class_='sig-name') else ''
                    param_desc = item.find('span', class_='sig-prename').next_sibling.strip() if item.find('span', class_='sig-prename') else ''
                    
                    method_info["parameters"].append({
                        "name": param_name,
                        "description": param_desc
                    })
        
        # Valor de retorno
        returns = method.find('dt', string='Returns')
        if returns:
            returns_dd = returns.find_next('dd')
            if returns_dd:
                method_info["returns"] = ' '.join(returns_dd.stripped_strings)
        
        result["methods"].append(method_info)
    
    # Extrair seções adicionais
    notes_section = soup.find('dt', string='Notes')
    if notes_section:
        result["notes"] = ' '.join(notes_section.find_next('dd').stripped_strings)
    
    examples_section = soup.find('dt', string='Examples')
    if examples_section:
        result["examples"] = ' '.join(examples_section.find_next('dd').stripped_strings)
    
    see_also_section = soup.find('dt', string='See also')
    if see_also_section:
        result["see_also"] = ' '.join(see_also_section.find_next('dd').stripped_strings)
    
    return result

import urllib.parse

from urllib.parse import urlparse

def extract_original_links(html_content, base_url=None):
    """
    Extrai links exatamente como estão no HTML, identificando se são relativos ou absolutos.
    
    Parâmetros:
    html_content (str): Conteúdo HTML contendo a tabela
    base_url (str): Opcional - URL base para referência
    
    Retorna:
    list: Lista de dicionários com informações sobre cada link
    """
    soup = BeautifulSoup(html_content, 'html.parser')
    links_info = []
    
    for table in soup.find_all('table'):
        for link in table.find_all('a', href=True):
            href = link['href'].strip()
            if not href or href.startswith('#'):
                continue
                
            # Analisa o link para determinar seu tipo
            parsed = urlparse(href)
            is_absolute = bool(parsed.netloc)  # True se contém domínio
            
            link_info = {
                'original': href,
                'is_absolute': is_absolute,
                'text': link.get_text(strip=True)
            }
            
            # Se fornecido base_url e o link é relativo, mostra como ficaria transformado
            if base_url and not is_absolute:
                link_info['transformed'] = urlparse(base_url)._replace(path=href).geturl()
            
            links_info.append(link_info)
    
    return links_info

def extrair_info_function(links, name):
    for link in links:
        print(extract_original_links(rq.get(link).text))
    pass


def main():
    url = "https://scikit-learn.org/stable/api/index.html"
    response = rq.get(url)
    links = extrair_links_toctree_l1(response.text)
    #extrair_info_modulo_json(links)
    names = [get_names(link) for link in links]
    extrair_info_function(links, names)

 

    


if __name__ == "__main__":
    main()
