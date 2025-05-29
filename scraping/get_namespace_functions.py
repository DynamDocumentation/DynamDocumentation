#namespace: classes e funções e outros namespaces
#classe:
#funções:
#modulos

import requests
from bs4 import BeautifulSoup
import json

import namespace

# URL da página de referência do NumPy
#url = "https://numpy.org/doc/stable/reference/routines.html"



def get_parent_url(url):
    return url.rsplit('/', 1)[0] + '/'

def get_func_name(url):
    return url.rsplit('/', 1)[1]

def get_links_namespace(url):
    # Fazendo a requisição HTTP
    response = requests.get(url)
    links_ = []
    # Verificando se a requisição foi bem-sucedida
    if response.status_code == 200:
        # Parseando o HTML com BeautifulSoup
        soup = BeautifulSoup(response.text, "html.parser")
        url_usage = get_parent_url(url)       
        # Encontrando todos os links dentro da seção principal
        links = soup.select("li a")  # Seleciona links dentro de listas
        for link in links:
            href = link.get("href")
            if href and "generated/" in href:
                full_url = url_usage + href
                links_.append(full_url)
    else:
        print(f"Erro ao acessar a página. Código de status: {response.status_code}")
    return links_

from bs4 import BeautifulSoup
import os
    
    
    

def get_name(url):
    return os.path.basename(url).replace(".html", "").replace("-package", "").split(".")[-1]

def get_keyword_from_filename(filename):
    filename = os.path.basename(filename).replace(".html", "")
    parts = filename.split(".")
    if len(parts) > 1:
        keyword = parts[1].replace("-package", "").replace("_", "")
    else:
        keyword = parts[0].replace("-package", "").replace("_", "")
    if keyword.lower() == "polynomials":
        return "polynomial"
    if keyword.lower() == "emath":
        return "mathematical-functions-with-automatic-domain"
    return keyword.lower()

def get_namespace_description(section):
    if section is None:
        return ""
    paragraphs = section.find_all("p", recursive=False)
    return "\n".join(p.get_text(strip=True) for p in paragraphs)

def get_namespace_notes(section):
    notes = section.find_all("div", class_="admonition note")
    note_texts = []
    for note in notes:
        title = note.find("p", class_="admonition-title")
        if title:
            title.decompose()
        note_texts.append(note.get_text(strip=True))
    return "\n\n".join(note_texts)

def section_remove_inner(section, id):
    main_section = section.find('section', {'id': id})
    if main_section is None:
        return section
    for inner in main_section.find_all('section'):
        inner.decompose()
    return section

def find_similar_section(html_path):
    f = requests.get(html_path)
    soup = BeautifulSoup(f.text, "html.parser")
    keyword = get_keyword_from_filename(html_path)

    for section in soup.find_all("section"):
        section_id = section.get("id", "").lower()
        if keyword in section_id:
            return section, section_id
    return None, None

def process_namespace(url):
    name = get_name(url)
    section, section_id = find_similar_section(url)
    section = section_remove_inner(section, section_id)

    description_text = get_namespace_description(section)
    notes_text = get_namespace_notes(section)
    metodos = []
    links = get_links_namespace(url)
    for l in links:
            metodos.append(get_name(l))
    return {
        name: {
            "params": "sim",
            "notes": notes_text or "não encontrado",
            "description": description_text or "não encontrado",
            "atributes": "sim",
            "metodos": metodos
        }
    }


if __name__ == "__main__":
    #get_links("https://numpy.org/doc/stable/reference/routines.html")
    namespaces = namespace.return_namespace()  # Ex: lista com URLs de namespaces
    all_data = {}
           

    for url in namespaces:
        if "index.html" not in url:
            print(f"Processando: {url}")
            result = process_namespace(url)
            
            if result:
                all_data.update(result)
                print(f"✅ Processado: {get_name(url)}")

    with open("todos_namespaces.json", "w", encoding="utf-8") as f:
        json.dump(all_data, f, ensure_ascii=False, indent=4)
        
    print("\n🎉 Arquivo 'todos_namespaces.json' criado com sucesso!")
