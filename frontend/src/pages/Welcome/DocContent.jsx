import React from "react";
import { 
    Typography, Box, Paper
} from "@mui/material";

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

export default DocContent;