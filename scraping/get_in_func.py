import get_namespace_functions as gnf
import namespace
import requests
from bs4 import BeautifulSoup

def get_section(url):
    val = url.split("/")
    points = val[-1].split(".")
    sec_name = ""
    sec_name = "-".join(points[0:len(points)-1])
    return sec_name

def get_body(url):
    response = requests.get(url)

# Verificando se a requisição foi bem-sucedida
    if response.status_code == 200:
        # Parseando o HTML
        soup = BeautifulSoup(response.text, "html.parser")
        section = get_section(url)
        sec = soup.find("section", id=section)
               
        # Encontrando a seção específica pelo identificador (ID ou classe)
        #print(sec)            
        divs = soup.find_all("div", class_="doctest highlight-default notranslate")
        for d in divs:
            print(d.get_text(strip=False))
        if sec:
            dt_tags = sec.find_all("dt")
            for dt in dt_tags:
                print(dt.get_text(strip=True))
            p_tags = sec.find_all("p")
            for p in p_tags:
                print(p.get_text(strip=True))
        else:
            print("seção nao encontrada")
    else:
        print(f"Erro ao acessar a página. Código de status: {response.status_code}")


    
if __name__ == "__main__":
    namespaces = namespace.return_namespace()
    for url in namespaces:
        print("\n\n\n")
        print(url)
        links = gnf.get_links_namespace(url)
        for l in links:         
            print(get_body(l))
            print(get_section(l))


    print(1)

