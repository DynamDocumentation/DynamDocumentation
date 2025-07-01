import React from "react";
import { Card, CardHeader, CardContent, Divider, IconButton, Typography, Box } from "@mui/material";
import AddIcon from '@mui/icons-material/Add';
import CloseIcon from '@mui/icons-material/Close';
import DocContent from "./DocContent";
import PanelSelector from "./PanelSelector";

const DocumentationPanel = ({ panel, panels, onSplit, onClose, onAddContent }) => {
    return (
        <Box 
            sx={{ 
                flex: 1,
                minWidth: panels.length > 1 ? 
                    (panels.length > 3 ? '280px' : `${100 / Math.min(panels.length, 3)}%`) : 
                    '100%',
                maxWidth: panels.length > 2 ? 
                    (panels.length > 4 ? '400px' : '33.33%') : 
                    'unset',
                height: '100%',
                padding: 1,
                position: 'relative'
            }}
        >
            <Card 
                sx={{ 
                    height: '100%', 
                    width: '100%',
                    display: 'flex',
                    flexDirection: 'column',
                    position: 'relative',
                    boxShadow: 3,
                    overflow: 'hidden'
                }}
            >
                <CardHeader 
                    title={
                        <Typography noWrap sx={{ maxWidth: '100%' }}>
                            {panel.content ? `${panel.content.namespace} - ${panel.content.name}` : `Painel ${panel.id}`}
                        </Typography>
                    }
                    action={
                        <Box sx={{ display: 'flex', gap: 1 }}>
                            <IconButton 
                                aria-label="split" 
                                onClick={() => onSplit(panel.id)}
                                title="Adicionar painel à direita"
                                size="small"
                            >
                                <AddIcon />
                            </IconButton>
                            
                            {/* Close button only if there's more than one panel */}
                            {panels.length > 1 && (
                                <IconButton 
                                    aria-label="close"
                                    onClick={() => onClose(panel.id)}
                                    title="Fechar painel"
                                    size="small"
                                    color="error"
                                >
                                    <CloseIcon />
                                </IconButton>
                            )}
                        </Box>
                    }
                    sx={{ 
                        pb: 0,
                        '& .MuiCardHeader-content': {
                            overflow: 'hidden'
                        }
                    }}
                />
                <Divider />
                <CardContent sx={{ flexGrow: 1, overflow: 'auto', display: 'flex', flexDirection: 'column', justifyContent: panel.content ? 'flex-start' : 'center', alignItems: 'center' }}>
                    {panel.content ? (
                        <DocContent content={panel.content} />
                    ) : (
                        <PanelSelector panel={panel} onAddContent={onAddContent} />
                    )}
                </CardContent>
            </Card>
        </Box>
    );
};

export default DocumentationPanel;
