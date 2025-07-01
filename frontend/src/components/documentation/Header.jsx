import React from "react";
import { Card, CardHeader, CardContent, Typography } from "@mui/material";

const Header = () => {
    return (
        <Card
            sx={{
                mb: 2,
                mx: 0.5,
                boxShadow: 3,
                overflow: 'hidden',
                display: 'flex',
                flexDirection: 'column'
            }}
        >
            <CardHeader
                title={
                    <Typography variant="h5" noWrap sx={{ maxWidth: '100%' }}>
                        Bem-vindo ao DynamDocumentation!
                    </Typography>
                }
                sx={{ pb: 0 }}
            />
            <CardContent>
                <Typography variant="body1" paragraph sx={{ mb: 0 }}>
                    Aqui você encontra uma vasta coleção de bibliotecas e suas respectivas documentações, principalmente para Python.
                    Você pode adicionar painéis clicando no ícone + e fechar painéis com o ícone X.
                </Typography>
            </CardContent>
        </Card>
    );
};

export default Header;
