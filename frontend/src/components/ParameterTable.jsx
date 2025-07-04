import React from 'react';
import { 
    Table, TableBody, TableCell, TableContainer, TableHead, TableRow, 
    Paper, Typography
} from '@mui/material';

const ParameterTable = ({ parameters }) => {
    if (!parameters || Object.keys(parameters).length === 0) {
        return <Typography color="text.secondary">Nenhum parâmetro</Typography>;
    }
    
    return (
        <TableContainer component={Paper} variant="outlined">
            <Table size="small">
                <TableHead>
                    <TableRow>
                        <TableCell><strong>Nome</strong></TableCell>
                        <TableCell><strong>Tipo</strong></TableCell>
                        <TableCell><strong>Descrição</strong></TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {Object.entries(parameters).map(([name, details]) => (
                        <TableRow key={name}>
                            <TableCell><code>{name}</code></TableCell>
                            <TableCell>{details.type || "N/A"}</TableCell>
                            <TableCell>{details.description || "Sem descrição"}</TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
    );
};

export default ParameterTable;