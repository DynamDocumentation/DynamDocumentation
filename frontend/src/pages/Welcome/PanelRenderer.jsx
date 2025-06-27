import React, { useState } from "react";
import { 
    Box, IconButton, Card, CardHeader, CardContent, Divider, 
    Typography, Button
} from "@mui/material";
import AddIcon from '@mui/icons-material/Add';
import CloseIcon from '@mui/icons-material/Close';
import LibraryBooksIcon from '@mui/icons-material/LibraryBooks';

import DocContent from './DocContent';
import LibrarySelector from './LibrarySelector';

const PanelRenderer = ({ node, onSplit, onClose, onAddContent, depth = 0 }) => {
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
                        {node.children.map((child) => (
                            <Box 
                                key={child.id} 
                                sx={{ 
                                    flex: 1,
                                    padding: 1,
                                    position: 'relative'
                                }}
                            >
                                <PanelRenderer 
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
                        <CardContent sx={{ 
                            flexGrow: 1, 
                            overflow: 'auto', 
                            display: 'flex', 
                            flexDirection: 'column', 
                            justifyContent: node.content ? 'flex-start' : 'center', 
                            alignItems: 'center' 
                        }}>
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

export default PanelRenderer;