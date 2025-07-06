import React, { useState } from "react";
import { 
  Box, 
  Typography, 
  Paper, 
  Container,
  TextField,
  Button,
  Avatar,
  Link as MuiLink,
  Alert
} from "@mui/material";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import axios from 'axios';

export default function Admin() {
  // State for form fields
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [showError, setShowError] = useState(false);
  const [success, setSuccess] = useState(false);
  
  // For redirecting after login
  const navigate = useNavigate();

  const handleSubmit = (event) => {
    event.preventDefault();
    
    // Reset any previous error messages
    setShowError(false);
    setError('');
    
    // Check if any field is empty
    if (!email.trim() || !password.trim()) {
      window.alert('Por favor, preencha todos os campos.');
      return;
    }
    
    // Create data object with form values
    const loginData = {
      email: email, // Using email field as username since backend might expect "username"
      password: password
    };
    
    // Send POST request with axios
    axios.post('/api/users/login', loginData, {
      headers: {
        'Content-Type': 'application/json',
      }
    })
      .then(response => {
        console.log('Login response:', response.data);
        
        // Check if we have a successful response with auth token
        if (response.data && 
            response.data.status === 'success' && 
            response.data.data) {
          
          const { authToken } = response.data.data;
          
          if (authToken) {
            // Only store the auth token, not the user data
            sessionStorage.setItem('authToken', authToken);
            
            console.log('Authentication token stored in session storage');
            
            // Show success message
            setSuccess(true);
            setShowError(false);
            
            // Redirect to library requisition list after short delay
            setTimeout(() => {
              navigate('/library-requests');
            }, 1500);
          }
        }
      })
      .catch((error) => {
        console.error('Error during login:', error);
        // Log the response details if available
        if (error.response) {
          console.log('Error response data:', error.response.data);
          console.log('Error response status:', error.response.status);
          
          // Handle user not found case - could be 404 Not Found or a custom message in the response
          if (error.response.status === 404 || 
              (error.response.data && error.response.data.message && 
               error.response.data.message.includes("not found"))) {
            setError('Usuário não encontrado. Verifique seu email e tente novamente.');
            setShowError(true);
          } else if (error.response.status === 401 || error.response.status === 403) {
            setError('Credenciais inválidas. Verifique seu email e senha.');
            setShowError(true);
          } else {
            setError('Erro ao fazer login. Por favor, tente novamente mais tarde.');
            setShowError(true);
          }
        } else {
          // Network error or server not available
          setError('Erro de conexão. Verifique sua internet ou tente novamente mais tarde.');
          setShowError(true);
        }
      });
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
          <LockOutlinedIcon />
        </Avatar>
        <Typography component="h1" variant="h5" sx={{ mb: 2 }}>
          Login de Administrador
        </Typography>
        
        {showError && (
          <Alert severity="error" sx={{ width: '100%', mb: 2 }}>
            {error}
          </Alert>
        )}
        
        {success && (
          <Alert severity="success" sx={{ width: '100%', mb: 2 }}>
            Login bem-sucedido! Redirecionando...
          </Alert>
        )}
        
        <Box component="form" onSubmit={handleSubmit} sx={{ width: '100%' }}>
          <TextField
            required
            fullWidth
            id="email"
            label="Endereço de Email"
            name="email"
            autoComplete="email"
            autoFocus
            variant="outlined"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            sx={{ mb: 2, width: '100%' }}
          />
          <TextField
            required
            fullWidth
            name="password"
            label="Senha"
            type="password"
            id="password"
            autoComplete="current-password"
            variant="outlined"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            sx={{ mb: 2, width: '100%' }}
          />
          <Button
            type="submit"
            fullWidth
            variant="contained"
            color="primary"
            sx={{ mt: 2, mb: 2, py: 1.5 }}
          >
            Entrar
          </Button>
          <Box sx={{ display: 'flex', justifyContent: 'flex-end', width: '100%' }}>
            <MuiLink component={RouterLink} to="/register" variant="body2">
              Não tem uma conta? Cadastre-se
            </MuiLink>
          </Box>
        </Box>
      </Paper>
    </Container>
  );
}
