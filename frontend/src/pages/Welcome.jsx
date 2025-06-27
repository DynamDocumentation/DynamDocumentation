import React, { useState, useEffect } from "react";
import { 
    Stack, Paper, Typography, Box, IconButton, 
    Card, CardHeader, CardContent, Divider, Button,
    Dialog, DialogTitle, DialogContent, List, ListItem, 
    ListItemButton, ListItemText, DialogActions, CircularProgress
} from "@mui/material";
import AddIcon from '@mui/icons-material/Add';
import CloseIcon from '@mui/icons-material/Close';
import LibraryBooksIcon from '@mui/icons-material/LibraryBooks';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import axios from 'axios';

// Componente para exibir o conteúdo da documentação
const DocContent = ({ content }) => {
    if (!content) return null;
    
    return (
        <Box sx={{ p: 1 }}>
            <Typography variant="h6" gutterBottom>
                {content.name}
            </Typography>
            
            {content.docstring?.description && (
                <Box sx={{ mb: 2 }}>
                    <Typography variant="subtitle1">Descrição:</Typography>
                    <Paper variant="outlined" sx={{ p: 1, mb: 1, backgroundColor: 'rgba(0,0,0,0.02)' }}>
                        <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
                            {content.docstring.description}
                        </Typography>
                    </Paper>
                </Box>
            )}
            
            {content.docstring?.parameters && Object.keys(content.docstring.parameters).length > 0 && (
                <Box sx={{ mb: 2 }}>
                    <Typography variant="subtitle1">Parâmetros:</Typography>
                    {Object.entries(content.docstring.parameters).map(([name, param]) => (
                        <Box key={name} sx={{ mb: 1 }}>
                            <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                                <code>{name}</code> ({param.type || 'unknown'})
                            </Typography>
                            <Typography variant="body2" sx={{ ml: 2 }}>
                                {param.description}
                            </Typography>
                        </Box>
                    ))}
                </Box>
            )}
            
            {content.docstring?.returns && (
                <Box sx={{ mb: 2 }}>
                    <Typography variant="subtitle1">Retorna:</Typography>
                    <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
                        {content.docstring.returns}
                    </Typography>
                </Box>
            )}
            
            {content.docstring?.examples && (
                <Box sx={{ mb: 2 }}>
                    <Typography variant="subtitle1">Exemplos:</Typography>
                    <Paper 
                        variant="outlined" 
                        sx={{ 
                            p: 1, 
                            backgroundColor: 'rgba(0,0,0,0.05)', 
                            fontFamily: 'monospace',
                            whiteSpace: 'pre-wrap'
                        }}
                    >
                        <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                            {content.docstring.examples}
                        </Typography>
                    </Paper>
                </Box>
            )}
        </Box>
    );
};

// Diálogo para seleção de bibliotecas, namespaces e funções
const LibrarySelector = ({ open, onClose, onSelect }) => {
    const [libraries, setLibraries] = useState(['numpy', 'sklearn']);
    const [selectedLibrary, setSelectedLibrary] = useState(null);
    const [namespaces, setNamespaces] = useState([]);
    const [selectedNamespace, setSelectedNamespace] = useState(null);
    const [functions, setFunctions] = useState([]);
    const [loading, setLoading] = useState(false);
    const [step, setStep] = useState(1); // 1: Bibliotecas, 2: Namespaces, 3: Funções
    
    // Simulação de carregamento de dados
    const loadNamespaces = (library) => {
        setLoading(true);
        
        // Simular uma chamada de API
        setTimeout(() => {
            let mockNamespaces;
            
            if (library === 'numpy') {
                mockNamespaces = [
                    { id: 1, name: 'numpy.core' },
                    { id: 2, name: 'numpy.random' },
                    { id: 3, name: 'numpy.linalg' },
                    { id: 4, name: 'numpy.fft' }
                ];
            } else if (library === 'sklearn') {
                mockNamespaces = [
                    { id: 5, name: 'sklearn.cluster' },
                    { id: 6, name: 'sklearn.linear_model' },
                    { id: 7, name: 'sklearn.tree' },
                    { id: 8, name: 'sklearn.ensemble' }
                ];
            }
            
            setNamespaces(mockNamespaces);
            setLoading(false);
        }, 500);
    };
    
    const loadFunctions = (namespace) => {
        setLoading(true);
        
        // Simular uma chamada de API
        setTimeout(() => {
            let mockFunctions = [];
            
            if (namespace.name === 'numpy.random') {
                mockFunctions = [
                    { 
                        id: 1, 
                        name: 'rand', 
                        signature: 'rand(d0, d1, ..., dn)',
                        docstring: {
                            description: 'Gera uma matriz de números aleatórios uniformemente distribuídos no intervalo [0, 1).',
                            parameters: {
                                d0: { type: 'int', description: 'A dimensão do array resultante' },
                                d1: { type: 'int', description: 'A dimensão do array resultante' }
                            },
                            returns: 'ndarray de forma (d0, d1, ..., dn), preenchido com números aleatórios',
                            examples: '>>> np.random.rand(3,2)\narray([[0.14022471, 0.96360618],\n       [0.37601032, 0.25528411],\n       [0.49313049, 0.94909878]])'
                        }
                    },
                    { 
                        id: 2, 
                        name: 'randn', 
                        signature: 'randn(d0, d1, ..., dn)',
                        docstring: {
                            description: 'Retorna uma amostra de números aleatórios da distribuição normal (Gaussiana).',
                            parameters: {
                                d0: { type: 'int', description: 'A dimensão do array resultante' },
                                d1: { type: 'int', description: 'A dimensão do array resultante' }
                            },
                            returns: 'ndarray de forma (d0, d1, ..., dn), preenchido com números aleatórios',
                            examples: '>>> np.random.randn(3,2)\narray([[ 1.5089, -0.2282],\n       [ 0.3407, -0.7559],\n       [ 0.8900, -0.8212]])'
                        }
                    }
                ];
            } else if (namespace.name === 'sklearn.cluster') {
                mockFunctions = [
                    { 
                        id: 3, 
                        name: 'KMeans', 
                        signature: 'KMeans(n_clusters=8, *, init="k-means++", n_init=10, ...)',
                        docstring: {
                            description: 'K-Means clustering.\n\nImplementa o algoritmo k-means para agrupamento.',
                            parameters: {
                                n_clusters: { type: 'int', description: 'O número de clusters a formar e o número de centróides a gerar.' },
                                init: { type: 'str ou array', description: 'Método para inicialização.' }
                            },
                            examples: '>>> from sklearn.cluster import KMeans\n>>> import numpy as np\n>>> X = np.array([[1, 2], [1, 4], [1, 0], [10, 2], [10, 4], [10, 0]])\n>>> kmeans = KMeans(n_clusters=2, random_state=0).fit(X)\n>>> kmeans.labels_\narray([1, 1, 1, 0, 0, 0])'
                        }
                    },
                    { 
                        id: 4, 
                        name: 'DBSCAN', 
                        signature: 'DBSCAN(eps=0.5, *, min_samples=5, metric="euclidean", ...)',
                        docstring: {
                            description: 'Perform DBSCAN clustering from vector array or distance matrix.',
                            parameters: {
                                eps: { type: 'float', description: 'A distância máxima entre duas amostras para que uma seja considerada vizinha da outra.' },
                                min_samples: { type: 'int', description: 'O número mínimo de amostras em um bairro para que um ponto seja considerado como um ponto central.' }
                            },
                            examples: '>>> from sklearn.cluster import DBSCAN\n>>> import numpy as np\n>>> X = np.array([[1, 2], [2, 2], [2, 3], [8, 7], [8, 8], [25, 80]])\n>>> clustering = DBSCAN(eps=3, min_samples=2).fit(X)\n>>> clustering.labels_\narray([0, 0, 0, 1, 1, -1])'
                        }
                    }
                ];
            }
            
            setFunctions(mockFunctions);
            setLoading(false);
        }, 500);
    };
    
    const handleLibrarySelect = (library) => {
        setSelectedLibrary(library);
        loadNamespaces(library);
        setStep(2);
    };
    
    const handleNamespaceSelect = (namespace) => {
        setSelectedNamespace(namespace);
        loadFunctions(namespace);
        setStep(3);
    };
    
    const handleFunctionSelect = (func) => {
        // Adicionar a informação da biblioteca e namespace ao objeto da função
        const fullFunc = {
            ...func,
            library: selectedLibrary,
            namespace: selectedNamespace.name
        };
        onSelect(fullFunc);
        onClose();
    };
    
    const handleBack = () => {
        if (step === 3) {
            setStep(2);
            setFunctions([]);
        } else if (step === 2) {
            setStep(1);
            setNamespaces([]);
            setSelectedLibrary(null);
        }
    };

    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
            <DialogTitle>
                {step === 1 ? "Selecione uma Biblioteca" : 
                 step === 2 ? `Selecione um Namespace em ${selectedLibrary}` : 
                 `Selecione uma Função em ${selectedNamespace?.name}`}
                
                {step > 1 && (
                    <IconButton
                        aria-label="back"
                        onClick={handleBack}
                        sx={{
                            position: 'absolute',
                            left: 8,
                            top: 8,
                        }}
                    >
                        <ArrowBackIcon />
                    </IconButton>
                )}
            </DialogTitle>
            <DialogContent dividers>
                {loading ? (
                    <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
                        <CircularProgress />
                    </Box>
                ) : (
                    <List>
                        {step === 1 && libraries.map((library) => (
                            <ListItemButton 
                                key={library} 
                                onClick={() => handleLibrarySelect(library)}
                            >
                                <ListItemText primary={library} />
                            </ListItemButton>
                        ))}
                        
                        {step === 2 && namespaces.map((namespace) => (
                            <ListItemButton 
                                key={namespace.id} 
                                onClick={() => handleNamespaceSelect(namespace)}
                            >
                                <ListItemText primary={namespace.name} />
                            </ListItemButton>
                        ))}
                        
                        {step === 3 && functions.map((func) => (
                            <ListItemButton 
                                key={func.id} 
                                onClick={() => handleFunctionSelect(func)}
                            >
                                <ListItemText 
                                    primary={func.name} 
                                    secondary={func.signature} 
                                />
                            </ListItemButton>
                        ))}
                    </List>
                )}
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose}>Cancelar</Button>
            </DialogActions>
        </Dialog>
    );
};

// Função recursiva para renderizar os painéis
const RenderPanel = ({ node, onSplit, onClose, onAddContent, depth = 0 }) => {
    const [selectorOpen, setSelectorOpen] = useState(false);
    
    const handleOpenSelector = () => {
        setSelectorOpen(true);
    };
    
    const handleCloseSelector = () => {
        setSelectorOpen(false);
    };
    
    const handleSelectContent = (content) => {
        onAddContent(node.id, content);
    };
    
    // Determinar a direção da divisão baseada na profundidade do nó
    const isHorizontalSplit = depth % 2 === 0;

    return (
        <>
            <Box
                sx={{
                    display: 'flex',
                    flexDirection: isHorizontalSplit ? 'row' : 'column',
                    width: '100%',
                    height: '100%',
                    position: 'relative',
                }}
            >
                {node.children ? (
                    // Se tem filhos, renderiza os filhos recursivamente e adiciona botão de fechar
                    <>
                        <IconButton
                            aria-label="close"
                            onClick={() => onClose(node.id)}
                            sx={{
                                position: 'absolute',
                                right: 8,
                                top: 8,
                                zIndex: 10,
                                backgroundColor: 'rgba(255, 255, 255, 0.7)',
                                '&:hover': {
                                    backgroundColor: 'rgba(255, 0, 0, 0.1)',
                                }
                            }}
                        >
                            <CloseIcon />
                        </IconButton>
                        {node.children.map((child, index) => (
                            <Box 
                                key={child.id} 
                                sx={{ 
                                    flex: 1,
                                    padding: 1,
                                    position: 'relative'
                                }}
                            >
                                <RenderPanel 
                                    node={child} 
                                    onSplit={onSplit}
                                    onClose={onClose}
                                    onAddContent={onAddContent}
                                    depth={depth + 1}
                                />
                            </Box>
                        ))}
                    </>
                ) : (
                    // Se não tem filhos, renderiza o conteúdo do painel
                    <Card 
                        sx={{ 
                            height: '100%', 
                            width: '100%',
                            display: 'flex',
                            flexDirection: 'column',
                            position: 'relative',
                            boxShadow: 3,
                        }}
                    >
                        <CardHeader 
                            title={node.content ? `${node.content.namespace} - ${node.content.name}` : `Painel ${node.id}`}
                            action={
                                <IconButton 
                                    aria-label="split" 
                                    onClick={() => onSplit(node.id)}
                                >
                                    <AddIcon />
                                </IconButton>
                            }
                            sx={{ pb: 0 }}
                        />
                        <Divider />
                        <CardContent sx={{ flexGrow: 1, overflow: 'auto', display: 'flex', flexDirection: 'column', justifyContent: node.content ? 'flex-start' : 'center', alignItems: 'center' }}>
                            {node.content ? (
                                <DocContent content={node.content} />
                            ) : (
                                <Box sx={{ textAlign: 'center' }}>
                                    <Button
                                        variant="contained"
                                        color="primary"
                                        startIcon={<LibraryBooksIcon />}
                                        onClick={handleOpenSelector}
                                        sx={{ mb: 2 }}
                                    >
                                        Adicionar Biblioteca
                                    </Button>
                                    <Typography variant="body2" color="text.secondary">
                                        Selecione uma biblioteca para exibir sua documentação neste painel
                                    </Typography>
                                </Box>
                            )}
                        </CardContent>
                    </Card>
                )}
            </Box>
            
            <LibrarySelector 
                open={selectorOpen}
                onClose={handleCloseSelector}
                onSelect={handleSelectContent}
            />
        </>
    );
};

export default function Welcome() {
    // Estado para armazenar a estrutura de painéis em formato de árvore
    const [panelTree, setPanelTree] = useState({
        id: 1,
        children: null,
        content: null
    });

    let nextId = 2;

    // Função para dividir um painel
    const handleSplit = (panelId) => {
        // Função recursiva para encontrar e dividir o painel correto
        const splitNodeById = (node) => {
            if (node.id === panelId) {
                // Dividir este nó criando dois filhos
                return {
                    ...node,
                    children: [
                        { id: nextId++, children: null, content: null },
                        { id: nextId++, children: null, content: null }
                    ]
                };
            }
            
            // Se este nó não tem filhos, retorná-lo sem modificações
            if (!node.children) {
                return node;
            }
            
            // Procurar nos filhos e atualizá-los se necessário
            return {
                ...node,
                children: node.children.map(child => splitNodeById(child))
            };
        };

        setPanelTree(splitNodeById(panelTree));
    };

    // Função para fechar um painel dividido
    const handleClose = (panelId) => {
        // Função recursiva para encontrar e fechar o painel correto
        const closeNodeById = (node) => {
            // Se este é o nó que queremos fechar
            if (node.id === panelId) {
                // Remover os filhos, voltando ao estado original
                return {
                    ...node,
                    children: null
                };
            }
            
            // Se este nó não tem filhos, retorná-lo sem modificações
            if (!node.children) {
                return node;
            }
            
            // Procurar nos filhos e atualizá-los se necessário
            return {
                ...node,
                children: node.children.map(child => closeNodeById(child))
            };
        };

        setPanelTree(closeNodeById(panelTree));
    };
    
    // Função para adicionar conteúdo a um painel
    const handleAddContent = (panelId, content) => {
        // Função recursiva para encontrar o painel correto e adicionar conteúdo
        const addContentToNode = (node) => {
            if (node.id === panelId) {
                // Adicionar conteúdo a este nó
                return {
                    ...node,
                    content
                };
            }
            
            // Se este nó não tem filhos, retorná-lo sem modificações
            if (!node.children) {
                return node;
            }
            
            // Procurar nos filhos e atualizá-los se necessário
            return {
                ...node,
                children: node.children.map(child => addContentToNode(child))
            };
        };
        
        setPanelTree(addContentToNode(panelTree));
    };

    return (
        <Box sx={{ padding: 3, height: 'calc(100vh - 120px)' }}>
            <Typography variant="h5" gutterBottom>
                Bem-vindo ao DynamDocumentation!
            </Typography>
            <Typography variant="body1" paragraph>
                Aqui você encontra uma vasta coleção de bibliotecas e suas respectivas documentações, principalmente para Python.
                Você pode dividir os painéis clicando no ícone + e fechar divisões com o ícone X.
            </Typography>
            
            <Paper 
                elevation={3} 
                sx={{ 
                    height: 'calc(100% - 100px)', 
                    width: '100%', 
                    overflow: 'hidden' 
                }}
            >
                <RenderPanel 
                    node={panelTree} 
                    onSplit={handleSplit} 
                    onClose={handleClose}
                    onAddContent={handleAddContent}
                />
            </Paper>
        </Box>
    );
}