import React from "react";
import { 
  Box, 
  Typography, 
  Paper, 
  Container,
  TextField,
  Button,
  Avatar,
  Stack,
  Link as MuiLink
} from "@mui/material";
import { Link as RouterLink } from "react-router-dom";
import PersonAddIcon from '@mui/icons-material/PersonAdd';

export default function Register() {
  // This is just a structure with no functionality as requested
  const handleSubmit = (event) => {
    event.preventDefault();
    // No functionality implemented as per requirements
    console.log("Botão de registro clicado - sem funcionalidade implementada");
  };

  return (
    <Container component="main" maxWidth="xs">
      <Paper 
        elevation={3} 
        sx={{
          marginTop: 8,
          p: 3,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          borderRadius: 2,
          width: '100%'
        }}
      >
        <Avatar sx={{ m: 1, bgcolor: 'primary.main' }}>
          <PersonAddIcon />
        </Avatar>
        <Typography component="h1" variant="h5" sx={{ mb: 2 }}>
          Criar Conta
        </Typography>
        
        <Box component="form" onSubmit={handleSubmit} sx={{ width: '100%' }}>
          <Stack direction="row" spacing={2} sx={{ width: '100%', mb: 2 }}>
            <TextField
              name="firstName"
              required
              fullWidth
              id="firstName"
              label="Nome"
              autoFocus
              variant="outlined"
              sx={{ flex: 1 }}
            />
            <TextField
              required
              fullWidth
              id="lastName"
              label="Sobrenome"
              name="lastName"
              variant="outlined"
              sx={{ flex: 1 }}
            />
          </Stack>
          
          <TextField
            required
            fullWidth
            id="email"
            label="Endereço de Email"
            name="email"
            autoComplete="email"
            variant="outlined"
            sx={{ mb: 2, width: '100%' }}
          />
          
          <TextField
            required
            fullWidth
            name="password"
            label="Senha"
            type="password"
            id="password"
            autoComplete="new-password"
            variant="outlined"
            sx={{ mb: 2, width: '100%' }}
          />
          
          <TextField
            required
            fullWidth
            name="confirmPassword"
            label="Confirmar Senha"
            type="password"
            id="confirmPassword"
            autoComplete="new-password"
            variant="outlined"
            sx={{ mb: 2, width: '100%' }}
          />
          
          <Button
            type="submit"
            fullWidth
            variant="contained"
            color="primary"
            sx={{ mt: 2, mb: 2, py: 1.5 }}
          >
            Registrar
          </Button>
          
          <Box sx={{ display: 'flex', justifyContent: 'flex-end', width: '100%' }}>
            <MuiLink component={RouterLink} to="/admin" variant="body2">
              Já possui uma conta? Entre aqui
            </MuiLink>
          </Box>
        </Box>
      </Paper>
    </Container>
  );
}
