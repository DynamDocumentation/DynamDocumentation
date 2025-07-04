import React from 'react';
import { 
    Typography, Accordion, AccordionSummary, AccordionDetails, 
    Card, CardContent, Box, Paper
} from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ParameterTable from './ParameterTable';

const FunctionDocViewer = ({ func, isSelected, onSelect }) => {
    return (
        <Accordion 
            expanded={isSelected}
            onChange={onSelect}
        >
            <AccordionSummary
                expandIcon={<ExpandMoreIcon />}
            >
                <Typography sx={{ fontWeight: 'bold', fontFamily: 'monospace' }}>
                    {func.name}
                </Typography>
            </AccordionSummary>
            <AccordionDetails>
                <Card variant="outlined">
                    <CardContent>
                        <Typography variant="subtitle1" gutterBottom sx={{ fontFamily: 'monospace' }}>
                            {func.signature || func.name}
                        </Typography>
                        
                        <Typography variant="body2" sx={{ mb: 2, whiteSpace: 'pre-wrap' }}>
                            {func.docstring?.description || "Sem descrição"}
                        </Typography>
                        
                        {func.docstring?.parameters && Object.keys(func.docstring.parameters).length > 0 && (
                            <Box sx={{ mb: 2 }}>
                                <Typography variant="subtitle1" gutterBottom>
                                    Parâmetros
                                </Typography>
                                <ParameterTable parameters={func.docstring.parameters} />
                            </Box>
                        )}
                        
                        {func.docstring?.returns && (
                            <Box sx={{ mb: 2 }}>
                                <Typography variant="subtitle1" gutterBottom>
                                    Retornos
                                </Typography>
                                <Typography variant="body2">
                                    {func.docstring.returns}
                                </Typography>
                            </Box>
                        )}
                        
                        {func.docstring?.examples && (
                            <Box>
                                <Typography variant="subtitle1" gutterBottom>
                                    Exemplos
                                </Typography>
                                <Paper 
                                    variant="outlined" 
                                    sx={{ 
                                        p: 2, 
                                        backgroundColor: '#f5f5f5',
                                        fontFamily: 'monospace',
                                        whiteSpace: 'pre-wrap'
                                    }}
                                >
                                    {func.docstring.examples}
                                </Paper>
                            </Box>
                        )}
                    </CardContent>
                </Card>
            </AccordionDetails>
        </Accordion>
    );
};

export default FunctionDocViewer;