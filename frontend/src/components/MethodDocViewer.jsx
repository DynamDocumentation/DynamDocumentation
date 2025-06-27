import React from 'react';
import { 
    Typography, Accordion, AccordionSummary, AccordionDetails, Box
} from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ParameterTable from './ParameterTable';

const MethodDocViewer = ({ method, className }) => {
    return (
        <Accordion sx={{ mb: 1 }}>
            <AccordionSummary
                expandIcon={<ExpandMoreIcon />}
            >
                <Typography sx={{ fontWeight: 'medium', fontFamily: 'monospace' }}>
                    {method.signature || method.name}
                </Typography>
            </AccordionSummary>
            <AccordionDetails>
                <Typography variant="body2" sx={{ mb: 2 }}>
                    {method.docstring?.description || "Sem descrição"}
                </Typography>
                
                {method.docstring?.parameters && Object.keys(method.docstring.parameters).length > 0 && (
                    <Box sx={{ mb: 2 }}>
                        <Typography variant="subtitle1" gutterBottom>
                            Parâmetros
                        </Typography>
                        <ParameterTable parameters={method.docstring.parameters} />
                    </Box>
                )}
                
                {method.docstring?.returns && (
                    <Box>
                        <Typography variant="subtitle1" gutterBottom>
                            Retornos
                        </Typography>
                        <Typography variant="body2">
                            {method.docstring.returns}
                        </Typography>
                    </Box>
                )}
            </AccordionDetails>
        </Accordion>
    );
};

export default MethodDocViewer;