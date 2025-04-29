import React from "react";
import { Stack, Paper, Typography } from "@mui/material"

export default function Welcome() {

    return (
        <Stack 
            direction="column"
            justifyContent="center"
            spacing={2}
            sx={{
                padding: 4
            }}
        >
            <Typography variant="h5">
                Bem vindo!
            </Typography>
            <Typography variant="body1">
                Aqui você encontra uma vasta coleção de bibliotecas e suas respectivas documentações, principalmente para Python.
            </Typography>
        </Stack>
    )
}