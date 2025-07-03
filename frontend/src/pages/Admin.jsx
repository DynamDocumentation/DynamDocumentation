import React from "react";
import { 
  Box, 
  Typography, 
  Paper, 
  Container,
  TextField,
  Button,
  Avatar
} from "@mui/material";
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';

export default function Admin() {
  // This is just a structure with no functionality as requested
  const handleSubmit = (event) => {
    event.preventDefault();
    // No functionality implemented as per requirements
    console.log("Botão de login clicado - sem funcionalidade implementada");
  };

  return (
    <Container component="main" maxWidth="xs">
      <Paper 
        elevation={3} 
        sx={{
          marginTop: 8,
          p: 4,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          borderRadius: 2
        }}
      >
        <Avatar sx={{ m: 1, bgcolor: 'primary.main' }}>
          <LockOutlinedIcon />
        </Avatar>
        <Typography component="h1" variant="h5" sx={{ mb: 3 }}>
          Login de Administrador
        </Typography>
        
        <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1, width: '100%' }}>
          <TextField
            margin="normal"
            required
            fullWidth
            id="email"
            label="Endereço de Email"
            name="email"
            autoComplete="email"
            autoFocus
          />
          <TextField
            margin="normal"
            required
            fullWidth
            name="password"
            label="Senha"
            type="password"
            id="password"
            autoComplete="current-password"
          />
          <Button
            type="submit"
            fullWidth
            variant="contained"
            sx={{ mt: 3, mb: 2, py: 1.5 }}
          >
            Entrar
          </Button>
        </Box>
      </Paper>
    </Container>
  );
}
