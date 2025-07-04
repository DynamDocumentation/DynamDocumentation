import React, { useState } from 'react';
import { 
    Dialog, DialogTitle, DialogContent, Table, TableBody, TableCell, 
    TableContainer, TableHead, TableRow, Paper, Button, Box, Typography,
    IconButton, CircularProgress, Collapse
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import DataObjectIcon from '@mui/icons-material/DataObject';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';
import JsonDocViewer from './JsonDocViewer';

const FileExplorer = ({ open, onClose, folder, files }) => {
    const [expandedFile, setExpandedFile] = useState(null);
    const [fileContent, setFileContent] = useState(null);
    const [loadingFile, setLoadingFile] = useState(false);
    const [selectedFunction, setSelectedFunction] = useState(null);
    const [functionDetails, setFunctionDetails] = useState(null);

    const handleFileClick = (fileName) => {
        if (expandedFile === fileName) {
            setExpandedFile(null);
            setFileContent(null);
            return;
        }
        
        setExpandedFile(fileName);
        setLoadingFile(true);
        
        // Simular carregamento do conteúdo do arquivo JSON
        setTimeout(() => {
            // Em um cenário real, isso seria uma requisição AJAX
            let mockContent;
            
            if (fileName.includes('sklearn_tree')) {
                mockContent = {
                    classes: [
                        { 
                            name: "DecisionTreeClassifier",
                            docstring: { 
                                description: "Um classificador de árvore de decisão. Leia mais no Guia do Usuário.",
                                parameters: {
                                    criterion: { type: "string", description: "A função para medir a qualidade de uma divisão" },
                                    max_depth: { type: "int", description: "A profundidade máxima da árvore" }
                                },
                                examples: ">>> from sklearn.tree import DecisionTreeClassifier\n>>> clf = DecisionTreeClassifier()\n>>> clf.fit(X, y)"
                            },
                            methods: [
                                { 
                                    name: "fit", 
                                    signature: "fit(X, y)",
                                    docstring: {
                                        description: "Constrói uma árvore de decisão a partir dos dados de treinamento",
                                        parameters: {
                                            X: { type: "array-like", description: "Características dos exemplos de treinamento" },
                                            y: { type: "array-like", description: "Classes alvo" }
                                        },
                                        returns: "self : object"
                                    }
                                },
                                { 
                                    name: "predict", 
                                    signature: "predict(X)",
                                    docstring: {
                                        description: "Prevê a classe para X",
                                        parameters: {
                                            X: { type: "array-like", description: "Os exemplos de entrada" }
                                        },
                                        returns: "y : array-like"
                                    } 
                                }
                            ]
                        },
                        { 
                            name: "DecisionTreeRegressor",
                            docstring: { 
                                description: "Um regressor de árvore de decisão. Leia mais no Guia do Usuário.",
                                parameters: {
                                    criterion: { type: "string", description: "A função para medir a qualidade de uma divisão" },
                                    max_depth: { type: "int", description: "A profundidade máxima da árvore" }
                                },
                                examples: ">>> from sklearn.tree import DecisionTreeRegressor\n>>> reg = DecisionTreeRegressor()\n>>> reg.fit(X, y)"
                            },
                            methods: [
                                { 
                                    name: "fit", 
                                    signature: "fit(X, y)",
                                    docstring: {
                                        description: "Constrói uma árvore de decisão a partir dos dados de treinamento",
                                        parameters: {
                                            X: { type: "array-like", description: "Características dos exemplos de treinamento" },
                                            y: { type: "array-like", description: "Valores alvo" }
                                        },
                                        returns: "self : object"
                                    }
                                },
                                { 
                                    name: "predict", 
                                    signature: "predict(X)",
                                    docstring: {
                                        description: "Prevê o valor para X",
                                        parameters: {
                                            X: { type: "array-like", description: "Os exemplos de entrada" }
                                        },
                                        returns: "y : array-like"
                                    } 
                                }
                            ]
                        }
                    ],
                    functions: [
                        {
                            name: "export_graphviz",
                            signature: "export_graphviz(decision_tree, out_file=None, ...)",
                            docstring: {
                                description: "Exporta uma árvore de decisão no formato DOT",
                                parameters: {
                                    decision_tree: { type: "object", description: "O estimador de árvore de decisão a ser exportado" },
                                    out_file: { type: "object ou str", description: "Objeto de arquivo ou string para gravar a saída" }
                                },
                                returns: "dot_data : string",
                                examples: ">>> from sklearn.tree import export_graphviz\n>>> export_graphviz(clf, out_file='tree.dot')"
                            }
                        },
                        {
                            name: "plot_tree",
                            signature: "plot_tree(decision_tree, ...)",
                            docstring: {
                                description: "Plota uma árvore de decisão",
                                parameters: {
                                    decision_tree: { type: "object", description: "O estimador de árvore de decisão a ser plotado" }
                                },
                                returns: "ax : matplotlib.axes.Axes",
                                examples: ">>> from sklearn.tree import plot_tree\n>>> plot_tree(clf)"
                            }
                        }
                    ]
                };
            } else if (fileName.includes('numpy')) {
                mockContent = {
                    classes: [
                        { 
                            name: "ndarray",
                            docstring: { 
                                description: "Um array multidimensional, homogêneo.",
                                parameters: {
                                    shape: { type: "tuple", description: "Forma do array" },
                                    dtype: { type: "data-type", description: "Tipo de dados do array" }
                                },
                                examples: ">>> import numpy as np\n>>> a = np.array([1, 2, 3])"
                            },
                            methods: [
                                { 
                                    name: "reshape", 
                                    signature: "reshape(shape)",
                                    docstring: {
                                        description: "Retorna um array contendo os mesmos dados com uma nova forma",
                                        parameters: {
                                            shape: { type: "tuple", description: "A nova forma" }
                                        },
                                        returns: "reshaped_array : ndarray"
                                    }
                                },
                                { 
                                    name: "transpose", 
                                    signature: "transpose(*axes)",
                                    docstring: {
                                        description: "Retorna um array com eixos trocados",
                                        parameters: {
                                            axes: { type: "tuple of ints", description: "A permutação dos eixos" }
                                        },
                                        returns: "transposed_array : ndarray"
                                    } 
                                }
                            ]
                        }
                    ],
                    functions: [
                        {
                            name: "array",
                            signature: "array(object, dtype=None, ...)",
                            docstring: {
                                description: "Cria um array",
                                parameters: {
                                    object: { type: "array_like", description: "Um objeto que pode ser convertido para um array" },
                                    dtype: { type: "data-type", description: "Tipo de dados desejado" }
                                },
                                returns: "out : ndarray",
                                examples: ">>> np.array([1, 2, 3])\narray([1, 2, 3])"
                            }
                        },
                        {
                            name: "zeros",
                            signature: "zeros(shape, dtype=float, ...)",
                            docstring: {
                                description: "Retorna um novo array de forma e tipo especificados, preenchido com zeros",
                                parameters: {
                                    shape: { type: "int or tuple of ints", description: "Forma do novo array" },
                                    dtype: { type: "data-type", description: "Tipo de dados desejado" }
                                },
                                returns: "out : ndarray",
                                examples: ">>> np.zeros(5)\narray([0., 0., 0., 0., 0.])"
                            }
                        }
                    ]
                };
            } else {
                mockContent = {
                    classes: [
                        { 
                            name: "ExemploClasse",
                            docstring: { 
                                description: "Esta é uma classe de exemplo para " + fileName,
                                parameters: {
                                    param1: { type: "tipo1", description: "Descrição do parâmetro 1" },
                                    param2: { type: "tipo2", description: "Descrição do parâmetro 2" }
                                },
                                examples: ">>> # Exemplo de uso\n>>> obj = ExemploClasse()\n>>> obj.metodo()"
                            },
                            methods: [
                                { 
                                    name: "metodo1", 
                                    signature: "metodo1(arg1, arg2)",
                                    docstring: {
                                        description: "Este é o método 1",
                                        parameters: {
                                            arg1: { type: "tipo1", description: "Descrição do arg1" },
                                            arg2: { type: "tipo2", description: "Descrição do arg2" }
                                        },
                                        returns: "Tipo de retorno: descrição"
                                    }
                                },
                                { 
                                    name: "metodo2", 
                                    signature: "metodo2(arg)",
                                    docstring: {
                                        description: "Este é o método 2",
                                        parameters: {
                                            arg: { type: "tipo", description: "Descrição do arg" }
                                        },
                                        returns: "Tipo de retorno: descrição"
                                    } 
                                }
                            ]
                        }
                    ],
                    functions: [
                        {
                            name: "funcao1",
                            signature: "funcao1(arg1, arg2, ...)",
                            docstring: {
                                description: "Descrição da função 1",
                                parameters: {
                                    arg1: { type: "tipo1", description: "Descrição do arg1" },
                                    arg2: { type: "tipo2", description: "Descrição do arg2" }
                                },
                                returns: "Tipo de retorno: descrição",
                                examples: ">>> # Exemplo de uso\n>>> funcao1(1, 2)"
                            }
                        },
                        {
                            name: "funcao2",
                            signature: "funcao2(arg, ...)",
                            docstring: {
                                description: "Descrição da função 2",
                                parameters: {
                                    arg: { type: "tipo", description: "Descrição do arg" }
                                },
                                returns: "Tipo de retorno: descrição",
                                examples: ">>> # Exemplo de uso\n>>> funcao2('exemplo')"
                            }
                        }
                    ]
                };
            }
            
            setFileContent(mockContent);
            setLoadingFile(false);
        }, 700); // Simular tempo de carregamento
    };
    
    const handleFunctionClick = (item, isClass = false) => {
        // Identificador único para a função/método
        const functionId = isClass 
            ? `class-${item.name}` 
            : `function-${item.name}`;
            
        if (selectedFunction === functionId) {
            setSelectedFunction(null);
            setFunctionDetails(null);
        } else {
            setSelectedFunction(functionId);
            setFunctionDetails(item);
        }
    };

    return (
        <Dialog 
            open={open} 
            onClose={onClose}
            maxWidth="lg"
            fullWidth
        >
            <DialogTitle>
                Arquivos na pasta: {folder}
                <IconButton
                    aria-label="close"
                    onClick={onClose}
                    sx={{
                        position: 'absolute',
                        right: 8,
                        top: 8,
                        color: (theme) => theme.palette.grey[500],
                    }}
                >
                    <CloseIcon />
                </IconButton>
            </DialogTitle>
            <DialogContent>
                <TableContainer component={Paper}>
                    <Table sx={{ minWidth: 650 }} aria-label="files table">
                        <TableHead>
                            <TableRow>
                                <TableCell><strong>Nome do Arquivo</strong></TableCell>
                                <TableCell align="right"><strong>Tamanho</strong></TableCell>
                                <TableCell align="center"><strong>Ações</strong></TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {files.map((file) => (
                                <React.Fragment key={file.name}>
                                    <TableRow>
                                        <TableCell component="th" scope="row">
                                            {file.name}
                                        </TableCell>
                                        <TableCell align="right">{file.size}</TableCell>
                                        <TableCell align="center">
                                            <Button
                                                variant="contained"
                                                color="primary"
                                                size="small"
                                                startIcon={<DataObjectIcon />}
                                                endIcon={expandedFile === file.name ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
                                                onClick={() => handleFileClick(file.name)}
                                            >
                                                {expandedFile === file.name ? "Fechar" : "Ver Conteúdo"}
                                            </Button>
                                        </TableCell>
                                    </TableRow>
                                    {expandedFile === file.name && (
                                        <TableRow>
                                            <TableCell colSpan={3} style={{ padding: 0 }}>
                                                <Collapse in={expandedFile === file.name} timeout="auto" unmountOnExit>
                                                    <Box sx={{ p: 3 }}>
                                                        {loadingFile ? (
                                                            <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
                                                                <CircularProgress />
                                                                <Typography sx={{ ml: 2 }}>Carregando conteúdo...</Typography>
                                                            </Box>
                                                        ) : fileContent ? (
                                                            <JsonDocViewer 
                                                                content={fileContent} 
                                                                selectedFunction={selectedFunction}
                                                                onFunctionClick={handleFunctionClick}
                                                            />
                                                        ) : (
                                                            <Typography>Nenhum conteúdo disponível</Typography>
                                                        )}
                                                    </Box>
                                                </Collapse>
                                            </TableCell>
                                        </TableRow>
                                    )}
                                </React.Fragment>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
            </DialogContent>
        </Dialog>
    );
};

export default FileExplorer;