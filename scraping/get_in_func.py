import get_namespace_functions as gnf
import namespace
import requests
import json
from bs4 import BeautifulSoup

def get_section(url):
    val = url.split("/")
    points = val[-1].split(".")
    sec_name = ""
    sec_name = "-".join(points[0:len(points)-1])
    return sec_name

def get_section_2(sec_name):
    return sec_name.lower()

def get_section_3(sec_name):
    return sec_name.replace("_", "-")

def parse_numpy_function_doc(url):
    response = requests.get(url)
    if response.status_code != 200:
        print(f"Erro ao acessar a página. Código de status: {response.status_code}")
        return None

    soup = BeautifulSoup(response.text, "html.parser")
    section_id = get_section(url)
    sec = soup.find("section", id=section_id)
    
    if not sec:
        section_id = get_section_2(section_id)
        sec = soup.find("section", id=section_id)
        if not sec:
            section_id = get_section_3(section_id)
            sec = soup.find("section", id=section_id)
            if not sec:
                print(f"section: {section_id} not find")
                return None
    result = {
        "description": "",
        "params": [],
        "return": "",
        "notes": "",
        "examples": ""
    }

    # DESCRIPTION
    first_p = sec.find("p")
    if first_p:
        result["description"] = first_p.get_text(strip=True)

    # PARAMETERS, RETURNS, NOTES, RAISES
    fields = sec.find_all("dl", class_="field-list")
    for field in fields:
        for dt in field.find_all("dt"):
            title = dt.get_text(strip=True).lower()
            dd = dt.find_next_sibling("dd")
            if "parameter" in title:
                param_dl = dd.find("dl")
                if param_dl:
                    for p_dt in param_dl.find_all("dt"):
                        name = p_dt.get_text(strip=True)
                        desc = p_dt.find_next_sibling("dd").get_text(strip=True)
                        result["params"].append({name: desc})
            elif "return" in title:
                result["return"] = dd.get_text(strip=True)
            elif "note" in title:
                result["notes"] = dd.get_text(strip=True)

    # EXAMPLES
    example_divs = sec.find_all("div", class_="highlight-python notranslate")
    if not example_divs:  # fallback para outros formatos
        example_divs = sec.find_all("div", class_="highlight-default notranslate")
    examples_text = "\n\n".join(div.get_text(strip=False) for div in example_divs)
    result["examples"] = examples_text.strip()

    return result
    
def url_name(url):
    return url.split("/")[-1].replace(".html", "").replace("routines.", "")

def func_name(url):
    return url.split("/")[-1].split(".")[-2]
if __name__ == "__main__":
    namespaces = namespace.return_namespace()
    
    for url in namespaces:
        all_data = {}
        print("\n\n\n")
        namespace_url = url_name(url)

        links = gnf.get_links_namespace(url)
        
        
        for l in links:         
            all_data[func_name(l)] = parse_numpy_function_doc(l)
                    
        with open(f"content_{namespace_url}.json", "w", encoding="utf-8") as f:
            json.dump(all_data, f, ensure_ascii=False, indent=4)
   
