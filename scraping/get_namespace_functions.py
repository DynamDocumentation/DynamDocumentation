import requests
from bs4 import BeautifulSoup

import namespace

# URL da página de referência do NumPy
#url = "https://numpy.org/doc/stable/reference/routines.html"



def get_parent_url(url):
    return url.rsplit('/', 1)[0] + '/'

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

if __name__ == "__main__":
    #get_links("https://numpy.org/doc/stable/reference/routines.html")
    namespaces = namespace.return_namespace()
    for url in namespaces:
        print("\n\n\n")
        print(url)
        links = get_links_namespace(url)
        for l in links: print(l)
        print("\n\n\n")

