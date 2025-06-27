import React, { useState, useEffect } from "react";
import { 
    Dialog, DialogTitle, DialogContent, DialogActions,
    List, ListItemButton, ListItemText,
    Button, IconButton, Box, CircularProgress
} from "@mui/material";
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { getNamespaces, getFunctions } from './mockData';

const LibrarySelector = ({ open, onClose, onSelect }) => {
    const [libraries] = useState(['numpy', 'sklearn']);
    const [selectedLibrary, setSelectedLibrary] = useState(null);
    const [namespaces, setNamespaces] = useState([]);
    const [selectedNamespace, setSelectedNamespace] = useState(null);
    const [functions, setFunctions] = useState([]);
    const [loading, setLoading] = useState(false);
    const [step, setStep] = useState(1); // 1: Bibliotecas, 2: Namespaces, 3: Funções
    
    // Carregamento de dados
    const loadNamespaces = (library) => {
        setLoading(true);
        
        // Simular uma chamada de API
        setTimeout(() => {
            const mockNamespaces = getNamespaces(library);
            setNamespaces(mockNamespaces);
            setLoading(false);
        }, 500);
    };
    
    const loadFunctions = (namespace) => {
        setLoading(true);
        
        // Simular uma chamada de API
        setTimeout(() => {
            const mockFunctions = getFunctions(namespace);
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

    // Reset state when dialog opens
    useEffect(() => {
        if (open) {
            setStep(1);
            setSelectedLibrary(null);
            setNamespaces([]);
            setSelectedNamespace(null);
            setFunctions([]);
        }
    }, [open]);

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

export default LibrarySelector;