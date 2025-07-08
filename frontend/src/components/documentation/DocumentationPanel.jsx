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
                width: {
                    xs: '100%',
                    sm: '100%',
                    md: '100%', // Always 100% width for column layout
                    lg: panels.length > 1 ? `${100 / Math.min(panels.length, 3)}%` : '100%', // Only split horizontally on large screens
                },
                // Remove minWidth and maxWidth for xs, sm, md
                minWidth: undefined,
                maxWidth: undefined,
                height: {
                    xs: panels.length > 1 ? '500px' : '100%',
                    sm: panels.length > 1 ? '500px' : '100%',
                    md: '100%',
                },
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
                    overflow: 'hidden',
                    minHeight: 0 /* Allow proper flex behavior */
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
                                title="Adicionar novo painel"
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
                <CardContent sx={{ 
                    flexGrow: 1, 
                    overflow: 'auto', 
                    display: 'flex', 
                    flexDirection: 'column', 
                    justifyContent: panel.content ? 'flex-start' : 'center', 
                    alignItems: 'center',
                    padding: 2,
                    minHeight: 0, /* Allow proper flex behavior */
                    height: '100%'
                }}>
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
