import React from 'react';
import { 
    Box, Typography, List, Accordion, AccordionSummary, 
    AccordionDetails, Card, CardContent, Paper
} from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ParameterTable from './ParameterTable';
import ClassDocViewer from './ClassDocViewer';
import FunctionDocViewer from './FunctionDocViewer';

const JsonDocViewer = ({ content, selectedFunction, onFunctionClick }) => {
    return (
        <Box>
            {/* Seção de Classes */}
            {content.classes && content.classes.length > 0 && (
                <Box sx={{ mb: 3 }}>
                    <Typography variant="h6" gutterBottom component="div">
                        Classes
                    </Typography>
                    <List>
                        {content.classes.map((classItem) => (
                            <ClassDocViewer 
                                key={`class-${classItem.name}`}
                                classItem={classItem}
                                isSelected={selectedFunction === `class-${classItem.name}`}
                                onSelect={() => onFunctionClick(classItem, true)}
                            />
                        ))}
                    </List>
                </Box>
            )}
            
            {/* Seção de Funções */}
            {content.functions && content.functions.length > 0 && (
                <Box>
                    <Typography variant="h6" gutterBottom component="div">
                        Funções
                    </Typography>
                    <List>
                        {content.functions.map((func) => (
                            <FunctionDocViewer 
                                key={`function-${func.name}`}
                                func={func}
                                isSelected={selectedFunction === `function-${func.name}`}
                                onSelect={() => onFunctionClick(func)}
                            />
                        ))}
                    </List>
                </Box>
            )}
        </Box>
    );
};

export default JsonDocViewer;