import React from 'react';
import { 
    Typography, Accordion, AccordionSummary, AccordionDetails, 
    Card, CardContent, Box, Paper, List
} from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ParameterTable from './ParameterTable';
import MethodDocViewer from './MethodDocViewer';

const ClassDocViewer = ({ classItem, isSelected, onSelect }) => {
    return (
        <Accordion 
            expanded={isSelected}
            onChange={onSelect}
        >
            <AccordionSummary
                expandIcon={<ExpandMoreIcon />}
            >
                <Typography sx={{ fontWeight: 'bold' }}>
                    Classe: {classItem.name}
                </Typography>
            </AccordionSummary>
            <AccordionDetails>
                <Card variant="outlined" sx={{ mb: 2 }}>
                    <CardContent>
                        <Typography variant="h6" gutterBottom>
                            Descrição
                        </Typography>
                        <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
                            {classItem.docstring?.description || "Sem descrição"}
                        </Typography>
                        
                        {classItem.docstring?.parameters && Object.keys(classItem.docstring.parameters).length > 0 && (
                            <Box sx={{ mt: 2 }}>
                                <Typography variant="h6" gutterBottom>
                                    Parâmetros
                                </Typography>
                                <ParameterTable parameters={classItem.docstring.parameters} />
                            </Box>
                        )}
                        
                        {classItem.docstring?.examples && (
                            <Box sx={{ mt: 2 }}>
                                <Typography variant="h6" gutterBottom>
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
                                    {classItem.docstring.examples}
                                </Paper>
                            </Box>
                        )}
                        
                        {classItem.methods && classItem.methods.length > 0 && (
                            <Box sx={{ mt: 3 }}>
                                <Typography variant="h6" gutterBottom>
                                    Métodos
                                </Typography>
                                <List>
                                    {classItem.methods.map((method) => (
                                        <MethodDocViewer 
                                            key={`method-${classItem.name}-${method.name}`}
                                            method={method}
                                            className={classItem.name}
                                        />
                                    ))}
                                </List>
                            </Box>
                        )}
                    </CardContent>
                </Card>
            </AccordionDetails>
        </Accordion>
    );
};

export default ClassDocViewer;