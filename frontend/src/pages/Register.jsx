import React, { useState, useEffect } from "react";
import { 
  Box, 
  Typography, 
  Paper, 
  Container,
  TextField,
  Button,
  Avatar,
  Stack,
  Link as MuiLink,
  Alert
} from "@mui/material";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import axios from 'axios';

export default function Register() {
  const [formData, setFormData] = React.useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: ''
  });
  const [error, setError] = React.useState('');
  const [success, setSuccess] = React.useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };
  
  const handleSubmit = async (event) => {
    event.preventDefault();
    
    // Basic validation
    if (formData.password !== formData.confirmPassword) {
      return;
    }
    
    try {
      // Prepare the data according to the backend User model
      const userData = {
        username: `${formData.firstName} ${formData.lastName}`, // Combining first and last name for username
        email: formData.email,
        password: formData.password
      };
      
      // Make the API call to the register endpoint using axios
      // Note: Using the proxy from package.json, we don't need to include the full URL
      await axios.post('/api/users/register', userData, {
        headers: {
          'Content-Type': 'application/json'
        }
      });
      
      console.log('Registration request sent');
      setSuccess(true);
      setError('');
      // No handling of response as requested
    } catch (error) {
      console.error('Error during registration:', error);
      if (error.response && error.response.status === 409) {
        // Check if the error message contains information about what's conflicting
        const errorMessage = error.response.data?.message || '';
        if (errorMessage.toLowerCase().includes('username')) {
          setError('Nome de usuário já existe. Por favor, tente um nome diferente.');
        } else if (errorMessage.toLowerCase().includes('email')) {
          setError('Este e-mail já está registrado. Por favor, use outro e-mail.');
        } else {
          // Generic conflict message
          setError('Usuário já existe! Por favor, tente com informações diferentes.');
        }
      } else {
        setError('Ocorreu um erro durante o registro. Por favor, tente novamente.');
      }
      setSuccess(false);
    }
  };

  // Redirect to login page on successful registration
  React.useEffect(() => {
    if (success) {
      const timer = setTimeout(() => {
        navigate('/admin'); // Redirecting to the login page
      }, 3000); // Redirect after 3 seconds

      // Cleanup the timer on component unmount
      return () => clearTimeout(timer);
    }
  }, [success, navigate]);

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
        </Avatar>          <Typography component="h1" variant="h5" sx={{ mb: 2 }}>
          Criar Conta
        </Typography>
        
        {error && (
          <Alert severity="error" sx={{ width: '100%', mb: 2 }}>
            {error}
          </Alert>
        )}
        
        {success && (
          <Alert severity="success" sx={{ width: '100%', mb: 2 }}>
            Registro realizado com sucesso! Redirecionando para a página de login...
          </Alert>
        )}
        
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
              value={formData.firstName}
              onChange={handleChange}
              sx={{ flex: 1 }}
            />
            <TextField
              required
              fullWidth
              id="lastName"
              label="Sobrenome"
              name="lastName"
              variant="outlined"
              value={formData.lastName}
              onChange={handleChange}
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
            value={formData.email}
            onChange={handleChange}
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
            value={formData.password}
            onChange={handleChange}
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
            value={formData.confirmPassword}
            onChange={handleChange}
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
