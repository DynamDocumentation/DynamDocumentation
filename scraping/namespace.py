def process_files_string(files_str):
    # Divide cada linha e depois cada elemento separado por espaços/tabs
    lines = files_str.split('\n')
    items = []
    for line in lines:
        # Remove espaços extras e divide por espaços/tabs
        parts = line.split()
        items.extend(parts)  # Adiciona todos os elementos da linha à lista
    return items

links = """https://numpy.org/doc/stable/reference/routines.exceptions.html                                                                                                              
https://numpy.org/doc/stable/reference/routines.fft.html                                                                                                                     
https://numpy.org/doc/stable/reference/routines.linalg.html                                                                                                                  
https://numpy.org/doc/stable/reference/routines.polynomials-package.html                                                                                                     
https://numpy.org/doc/stable/reference/random/index.html                                                                                                                     
https://numpy.org/doc/stable/reference/routines.strings.html                                                                                                                 
https://numpy.org/doc/stable/reference/routines.testing.html                                                                                                                 
https://numpy.org/doc/stable/reference/typing.html                                                                                                                           
https://numpy.org/doc/stable/reference/routines.ctypeslib.html                                                                                                               
https://numpy.org/doc/stable/reference/routines.dtypes.html                                                                                                                  
https://numpy.org/doc/stable/reference/routines.emath.html                                                                                                                   
https://numpy.org/doc/stable/reference/routines.lib.html                                                                                                                     
https://numpy.org/doc/stable/reference/routines.rec.html                                                                                                                     
https://numpy.org/doc/stable/reference/routines.version.html                                                                                                                 
https://numpy.org/doc/stable/reference/routines.char.html                                                                                                                    
https://numpy.org/doc/stable/reference/distutils.html                                                                                                                        
https://numpy.org/doc/stable/reference/routines.ma.html                                                                                                                      
https://numpy.org/doc/stable/reference/routines.matlib.html 
"""

def return_namespace():
    return process_files_string(links)

if __name__ == "__main__":
    return_namespace()




