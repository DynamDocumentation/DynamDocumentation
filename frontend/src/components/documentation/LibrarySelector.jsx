import React, { useState, useEffect } from "react";
import { 
    Dialog, DialogTitle, DialogContent, DialogActions, 
    List, ListItemButton, ListItemText, 
    Button, IconButton, Box, CircularProgress, 
    Alert, Typography
} from "@mui/material";
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import axios from 'axios';

// Dialog for selecting libraries, namespaces, and functions
const LibrarySelector = ({ open, onClose, onSelect }) => {
    const [libraries, setLibraries] = useState([]);
    const [selectedLibrary, setSelectedLibrary] = useState(null);
    const [namespaces, setNamespaces] = useState([]);
    const [selectedNamespace, setSelectedNamespace] = useState(null);
    const [functions, setFunctions] = useState([]);
    const [classes, setClasses] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [step, setStep] = useState(1); // 1: Libraries, 2: Namespaces, 3: Functions/Classes
    
    // Fetch libraries when dialog opens
    useEffect(() => {
        if (open) {
            loadLibraries();
        }
    }, [open]);
    
    // Fetch libraries from backend
    const loadLibraries = async () => {
        setLoading(true);
        setError(null);
        
        try {
            // Make API call to backend to get all available libraries
            const response = await axios.get('/library');
            
            if (response.data && response.data.libraries && Array.isArray(response.data.libraries)) {
                setLibraries(response.data.libraries);
            } else {
                console.warn("Unexpected data format received from server, using fallback data");
                // Fallback to hardcoded libraries if response format is unexpected
                setLibraries(['numpy', 'seaborn', 'matplotlib']);
            }
        } catch (error) {
            console.error("Error fetching libraries:", error);
            setError("Failed to load libraries. Using fallback data.");
            // Fallback to hardcoded libraries if API call fails
            setLibraries(['numpy', 'seaborn', 'matplotlib']);
        } finally {
            setLoading(false);
        }
    };
    
    // Fetch namespaces from backend
    const loadNamespaces = async (library) => {
        setLoading(true);
        setError(null);
        
        try {
            // Make API call to backend to get the namespaces for the selected library
            const response = await axios.get(`/library/${library}`);
            
            console.log("Namespace response data:", JSON.stringify(response.data, null, 2));
            
            if (response.data && Array.isArray(response.data)) {
                // Process the response to extract namespaces
                const fetchedNamespaces = response.data.map((namespace) => ({
                    id: namespace.name, // Using name as ID for uniqueness
                    name: namespace.name,
                    classes: namespace.classes || [],
                    functions: namespace.functions || []
                }));
                
                setNamespaces(fetchedNamespaces);
            } else {
                // If the data is not in the expected format, use fallback mock data
                console.warn("Unexpected data format received from server, using fallback data");
                const mockNamespaces = createMockNamespaces(library);
                setNamespaces(mockNamespaces);
            }
        } catch (error) {
            console.error("Error fetching namespaces:", error);
            setError(`Failed to load modules for ${library}. Using fallback data.`);
            
            // Use fallback mock data if the API call fails
            const mockNamespaces = createMockNamespaces(library);
            setNamespaces(mockNamespaces);
        } finally {
            setLoading(false);
        }
    };
    
    // Fallback function to create mock namespaces if API call fails
    const createMockNamespaces = (library) => {
        if (library === 'numpy') {
            return [
                { id: 'numpy.core', name: 'numpy.core' },
                { id: 'numpy.random', name: 'numpy.random' },
                { id: 'numpy.linalg', name: 'numpy.linalg' },
                { id: 'numpy.fft', name: 'numpy.fft' }
            ];
        } else if (library === 'seaborn') {
            return [
                { id: 'seaborn.categorical', name: 'seaborn.categorical' },
                { id: 'seaborn.relational', name: 'seaborn.relational' },
                { id: 'seaborn.distributions', name: 'seaborn.distributions' },
                { id: 'seaborn.regression', name: 'seaborn.regression' }
            ];
        } else if (library === 'matplotlib') {
            return [
                { id: 'matplotlib.pyplot', name: 'matplotlib.pyplot' },
                { id: 'matplotlib.figure', name: 'matplotlib.figure' },
                { id: 'matplotlib.axes', name: 'matplotlib.axes' },
                { id: 'matplotlib.animation', name: 'matplotlib.animation' }
            ];
        }
        return [];
    };
    
    const loadEntities = async (namespace) => {
        setLoading(true);
        setError(null);
        
        try {
            console.log("Loading entities for namespace:", namespace);
            
            // The namespace object already contains classes and functions from the previous API call
            if (namespace.classes && namespace.functions) {
                // Process the classes and functions - enhance them with signature property
                const processedClasses = namespace.classes.map(cls => ({
                    ...cls,
                    signature: cls.signature || "", // Ensure signature exists
                    // Create docstring structure for compatibility with hardcoded data
                    docstring: {
                        description: cls.description || "", // Use description from backend
                        parameters: cls.parameters || {},   // Parameters will be loaded in detail view
                        returns: cls.returnType || ""       // Use returnType from backend
                    },
                    // Add example if available
                    example: cls.example || ""
                }));
                
                const processedFunctions = namespace.functions.map(func => ({
                    ...func,
                    signature: func.signature || "", // Ensure signature exists
                    // Create docstring structure for compatibility with hardcoded data
                    docstring: {
                        description: func.description || "", // Use description from backend
                        parameters: func.parameters || {},   // Parameters will be loaded in detail view
                        returns: func.returnType || "",      // Use returnType from backend
                        examples: func.example || ""         // Use example from backend
                    },
                    // Add direct properties for API compatibility
                    description: func.description || "",
                    returnType: func.returnType || "",
                    example: func.example || ""
                }));
                
                console.log("Processed classes:", processedClasses);
                console.log("Processed functions:", processedFunctions);
                
                setClasses(processedClasses);
                setFunctions(processedFunctions);
            } else {
                console.warn("No classes or functions found in namespace data, using fallback data");
                const mockFunctions = createMockFunctions(namespace);
                setFunctions(mockFunctions);
                setClasses([]);
            }
        } catch (error) {
            console.error("Error loading functions and classes:", error);
            setError(`Failed to load entities for ${namespace.name}. Using fallback data.`);
            
            // Use fallback mock data
            const mockFunctions = createMockFunctions(namespace);
            setFunctions(mockFunctions);
            setClasses([]);
        } finally {
            setLoading(false);
        }
    };
    
    // Function to create mock functions data (fallback)
    const createMockFunctions = (namespace) => {
        if (namespace.name === 'numpy.random') {
            return [
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
        } else if (namespace.name === 'matplotlib.pyplot') {
            return [
                { 
                    id: 3, 
                    name: 'plot', 
                    signature: 'plot(*args, scalex=True, scaley=True, data=None, **kwargs)',
                    docstring: {
                        description: 'Plota linhas e/ou marcadores para os dados fornecidos. Este é o comando mais básico de plotagem para criar figuras.',
                        parameters: {
                            args: { type: 'array', description: 'Os dados a serem plotados' },
                            scalex: { type: 'bool', description: 'Se True, a visualização ajustará os limites de x' },
                            scaley: { type: 'bool', description: 'Se True, a visualização ajustará os limites de y' }
                        },
                        returns: 'Uma lista de objetos Line2D que foram adicionados ao plot',
                        examples: '>>> plt.plot([1, 2, 3, 4])\n>>> plt.plot(x, y, "ro-")\n>>> plt.plot([1, 2, 3, 4], [1, 4, 9, 16])'
                    }
                },
                { 
                    id: 4, 
                    name: 'figure', 
                    signature: 'figure(num=None, figsize=None, dpi=None, facecolor=None, edgecolor=None, frameon=True, ...)',
                    docstring: {
                        description: 'Cria uma nova figura ou ativa uma figura existente.',
                        parameters: {
                            num: { type: 'int or str or Figure', description: 'Um identificador único para a figura' },
                            figsize: { type: 'tuple of floats', description: 'Largura e altura da figura em polegadas' },
                            dpi: { type: 'float', description: 'Resolução da figura em pontos por polegada' }
                        },
                        returns: 'Objeto Figure',
                        examples: '>>> plt.figure()\n>>> plt.figure(figsize=(8, 6))\n>>> plt.figure(2)'
                    }
                }
            ];
        } else if (namespace.name === 'seaborn.categorical') {
            return [
                { 
                    id: 5, 
                    name: 'barplot', 
                    signature: 'barplot(*, x=None, y=None, hue=None, data=None, ...)',
                    docstring: {
                        description: 'Mostra estimativas pontuais e intervalos de confiança usando barras verticais.',
                        parameters: {
                            x: { type: 'str or vector', description: 'Variável no eixo x' },
                            y: { type: 'str or vector', description: 'Variável no eixo y' },
                            hue: { type: 'str or vector', description: 'Variável para dividir os dados em terceiros' }
                        },
                        returns: 'Objeto AxesSubplot',
                        examples: '>>> import seaborn as sns\n>>> tips = sns.load_dataset("tips")\n>>> sns.barplot(x="day", y="total_bill", data=tips)'
                    }
                },
                { 
                    id: 6, 
                    name: 'countplot', 
                    signature: 'countplot(*, x=None, y=None, hue=None, data=None, ...)',
                    docstring: {
                        description: 'Mostra a contagem de observações em cada categoria usando barras.',
                        parameters: {
                            x: { type: 'str or vector', description: 'Variável no eixo x' },
                            y: { type: 'str or vector', description: 'Variável no eixo y' },
                            hue: { type: 'str or vector', description: 'Variável para dividir os dados em terceiros' }
                        },
                        returns: 'Objeto AxesSubplot',
                        examples: '>>> import seaborn as sns\n>>> titanic = sns.load_dataset("titanic")\n>>> sns.countplot(x="class", data=titanic)'
                    }
                }
            ];
        }
        return [];
    };
    
    const handleLibrarySelect = async (library) => {
        setSelectedLibrary(library);
        await loadNamespaces(library);
        setStep(2);
    };
    
    const handleNamespaceSelect = async (namespace) => {
        setSelectedNamespace(namespace);
        await loadEntities(namespace);
        setStep(3);
    };
    
    const handleEntitySelect = (entity, type) => {
        // When an entity (class or function) is selected
        // Add library and namespace information to the entity object
        console.log("Selected entity:", entity);
        console.log("Entity type:", type);
        
        // Ensure docstring is properly formatted
        if (!entity.docstring) {
            entity.docstring = {
                description: entity.description || "",
                parameters: {},
                returns: entity.returnType || entity.returns || "",
                examples: entity.example || ""
            };
        }
        
        // Create mock parameters if we don't have real ones yet
        // These will be replaced by real data when the entity details are loaded
        let mockParameters = [];
        if (entity.signature) {
            // Extract parameter names from signature if available
            const signatureMatch = entity.signature.match(/\(([^)]*)\)/);
            if (signatureMatch && signatureMatch[1]) {
                const paramString = signatureMatch[1].trim();
                if (paramString) {
                    mockParameters = paramString.split(',').map((param, index) => {
                        const paramParts = param.trim().split('=');
                        const paramName = paramParts[0].trim();
                        const defaultValue = paramParts.length > 1 ? paramParts[1].trim() : null;
                        
                        return {
                            id: `param-${index}`,
                            name: paramName,
                            dataType: '',
                            description: 'Parameter details will be loaded',
                            defaultValue: defaultValue
                        };
                    });
                }
            }
        }
        
        // Create a comprehensive entity object with all needed fields
        const fullEntity = {
            ...entity,
            library: selectedLibrary,
            namespace: selectedNamespace.name,
            type: type, // 'class' or 'function'
            
            // Add these fields explicitly to ensure they exist
            id: entity.id,
            name: entity.name,
            signature: entity.signature || "",
            description: entity.description || entity.docstring?.description || "",
            returnType: entity.returnType || entity.docstring?.returns || "",
            
            // Add parameters both at the root level and in docstring for compatibility
            parameters: entity.parameters || mockParameters,
            
            // This format works better with the DocContent component
            docstring: {
                ...entity.docstring,
                description: entity.description || entity.docstring?.description || "",
                parameters: entity.parameters || entity.docstring?.parameters || mockParameters,
                returns: entity.returnType || entity.docstring?.returns || "",
                examples: entity.example || entity.docstring?.examples || ""
            }
        };
        
        console.log("Passing to parent:", fullEntity);
        onSelect(fullEntity);
        onClose();
    };
    
    const handleBack = () => {
        if (step === 3) {
            setStep(2);
            setFunctions([]);
            setClasses([]);
        } else if (step === 2) {
            setStep(1);
            setNamespaces([]);
            setSelectedLibrary(null);
        }
    };

    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
            <DialogTitle sx={{ 
                display: 'flex', 
                alignItems: 'center',
                pl: step > 1 ? 6 : 2, // Add padding on the left when the back button is present
                position: 'relative'
            }}>
                {step > 1 && (
                    <IconButton
                        aria-label="back"
                        onClick={handleBack}
                        sx={{
                            position: 'absolute',
                            left: 8,
                        }}
                    >
                        <ArrowBackIcon />
                    </IconButton>
                )}
                <Typography sx={{ width: '100%', textAlign: 'center' }}>
                    {step === 1 ? "Selecione uma Biblioteca" : 
                     step === 2 ? `Selecione um Namespace em ${selectedLibrary}` : 
                     `Selecione uma Função ou Classe em ${selectedNamespace?.name}`}
                </Typography>
            </DialogTitle>
            <DialogContent dividers>
                {loading ? (
                    <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
                        <CircularProgress />
                    </Box>
                ) : (
                    <>
                        {error && (
                            <Alert severity="warning" sx={{ mb: 2 }}>
                                {error}
                            </Alert>
                        )}
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
                            
                            {step === 3 && (
                                <>
                                    {classes.length > 0 && (
                                        <>
                                            <Typography variant="subtitle1" sx={{ ml: 2, mt: 1, fontWeight: 'bold' }}>
                                                Classes
                                            </Typography>
                                            {classes.map((cls) => (
                                                <ListItemButton 
                                                    key={`class-${cls.id}`} 
                                                    onClick={() => handleEntitySelect(cls, 'class')}
                                                >
                                                    <ListItemText 
                                                        primary={cls.name}
                                                    />
                                                </ListItemButton>
                                            ))}
                                        </>
                                    )}
                                    
                                    {functions.length > 0 && (
                                        <>
                                            <Typography variant="subtitle1" sx={{ ml: 2, mt: 2, fontWeight: 'bold' }}>
                                                Funções
                                            </Typography>
                                            {functions.map((func) => (
                                                <ListItemButton 
                                                    key={`func-${func.id}`} 
                                                    onClick={() => handleEntitySelect(func, 'function')}
                                                >
                                                    <ListItemText 
                                                        primary={func.name}
                                                    />
                                                </ListItemButton>
                                            ))}
                                        </>
                                    )}
                                    
                                    {classes.length === 0 && functions.length === 0 && (
                                        <Alert severity="info" sx={{ m: 2 }}>
                                            Nenhuma classe ou função encontrada neste namespace
                                        </Alert>
                                    )}
                                </>
                            )}
                        </List>
                    </>
                )}
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose}>Cancelar</Button>
            </DialogActions>
        </Dialog>
    );
};

export default LibrarySelector;
