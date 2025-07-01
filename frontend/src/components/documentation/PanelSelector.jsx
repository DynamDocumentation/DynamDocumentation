import React, { useState } from "react";
import { Box, Button, Typography } from "@mui/material";
import LibraryBooksIcon from '@mui/icons-material/LibraryBooks';
import LibrarySelector from "./LibrarySelector";

// Component for empty panel content selector
const PanelSelector = ({ panel, onAddContent }) => {
    const [selectorOpen, setSelectorOpen] = useState(false);
    
    const handleOpenSelector = () => {
        setSelectorOpen(true);
    };
    
    const handleCloseSelector = () => {
        setSelectorOpen(false);
    };
    
    const handleSelectContent = (content) => {
        onAddContent(panel.id, content);
    };
    
    return (
        <>
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
            
            <LibrarySelector 
                open={selectorOpen}
                onClose={handleCloseSelector}
                onSelect={handleSelectContent}
            />
        </>
    );
};

export default PanelSelector;
