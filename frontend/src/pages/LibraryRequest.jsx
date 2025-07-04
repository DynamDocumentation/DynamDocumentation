import React, { useState, useEffect } from "react";
import { 
  Box, 
  Typography, 
  Paper, 
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Button,
  Container,
  CircularProgress,
  Alert,
  Snackbar
} from "@mui/material";
import axios from 'axios';
import { getAuthToken } from '../utils/auth';
import { useNavigate } from 'react-router-dom';

export default function LibraryRequest() {
  const [libraryRequests, setLibraryRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [processingId, setProcessingId] = useState(null);
  const [notification, setNotification] = useState({ open: false, message: '', severity: 'info' });
  const navigate = useNavigate();
  
  // Function to fetch library requests from the API
  const fetchLibraryRequests = async () => {
    setLoading(true);
    try {
      const response = await axios.get('/api/library/requests');
      // Check if we have valid data in the response
      if (response.data && response.data.status === 'success' && Array.isArray(response.data.data)) {
        setLibraryRequests(response.data.data);
      } else {
        console.warn('Unexpected data format:', response.data);
        setError('Formato de dados inesperado recebido do servidor.');
        // Fallback to empty array to prevent errors
        setLibraryRequests([]);
      }
    } catch (err) {
      console.error('Error fetching library requests:', err);
      setError(`Erro ao buscar solicitações: ${err.message}`);
      // Fallback to empty array to prevent errors
      setLibraryRequests([]);
    } finally {
      setLoading(false);
    }
  };
  
  // Handle library installation
  const handleInstall = async (requestId) => {
    // Get authentication token
    const authToken = getAuthToken();
    
    if (!authToken) {
      setNotification({
        open: true,
        message: 'Você precisa estar autenticado para realizar esta ação.',
        severity: 'error'
      });
      
      // Redirect to login after a short delay
      setTimeout(() => {
        navigate('/admin');
      }, 1500);
      
      return;
    }
    
    setProcessingId(requestId);
    
    try {
      const response = await axios.post('/api/library/install', {
        requestId: requestId,
        authToken: authToken
      });
      
      if (response.data && response.data.status === 'success') {
        // Update the local state to reflect the change
        setLibraryRequests(libraryRequests.map(req => 
          req.id === requestId ? { ...req, accepted: true } : req
        ));
        
        setNotification({
          open: true,
          message: 'Biblioteca instalada com sucesso!',
          severity: 'success'
        });
      } else {
        throw new Error(response.data?.message || 'Erro desconhecido');
      }
    } catch (err) {
      console.error('Error installing library:', err);
      
      // Check if the error is due to invalid/expired auth token
      if (err.response && (err.response.status === 401 || err.response.status === 403)) {
        // Clear session storage
        sessionStorage.removeItem('authToken');
        
        setNotification({
          open: true,
          message: 'Sua sessão expirou ou é inválida. Redirecionando para o login...',
          severity: 'error'
        });
        
        // Redirect to login after a short delay
        setTimeout(() => {
          navigate('/admin');
        }, 1500);
      } else {
        setNotification({
          open: true,
          message: `Erro ao instalar biblioteca: ${err.response?.data?.message || err.message}`,
          severity: 'error'
        });
      }
    } finally {
      setProcessingId(null);
    }
  };
  
  // Close notification
  const handleCloseNotification = () => {
    setNotification({ ...notification, open: false });
  };

  useEffect(() => {
    // Call the fetch function when component mounts
    fetchLibraryRequests();
  }, []); // Empty dependency array means this runs once on component mount

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Paper sx={{ p: 3, mb: 4 }}>
        <Typography variant="h4" gutterBottom>
          Solicitações de Bibliotecas
        </Typography>
        <Typography variant="body1" paragraph>
          Esta página exibe todas as solicitações de bibliotecas enviadas pelos usuários. Administradores podem revisar e gerenciar essas solicitações.
        </Typography>
        
        {/* Show appropriate UI based on state */}
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
            <CircularProgress />
          </Box>
        ) : error ? (
          <Alert severity="error" sx={{ mt: 2, mb: 2 }}>{error}</Alert>
        ) : libraryRequests.length === 0 ? (
          <Alert severity="info" sx={{ mt: 2, mb: 2 }}>Não há solicitações de bibliotecas disponíveis.</Alert>
        ) : (
          <TableContainer component={Paper} sx={{ mt: 3 }}>
            <Table sx={{ minWidth: 650, tableLayout: 'fixed' }} aria-label="tabela de solicitações de bibliotecas">
              <TableHead>
                <TableRow>
                  <TableCell width="100px">ID</TableCell>
                  <TableCell>Nome da Biblioteca</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell width="130px"></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {libraryRequests.map((request) => (
                  <TableRow
                    key={request.id}
                    sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                  >
                    <TableCell component="th" scope="row">
                      {request.id}
                    </TableCell>
                    <TableCell>{request.name}</TableCell>
                    <TableCell>
                      {request.accepted ? 
                        <Box sx={{ color: 'success.main', fontWeight: 'bold' }}>Aceito</Box> : 
                        <Box sx={{ color: 'warning.main', fontWeight: 'bold' }}>Pendente</Box>
                      }
                    </TableCell>
                    <TableCell align="right" sx={{ pr: 2 }}>
                      <Button 
                        variant="contained" 
                        color={request.accepted ? "success" : "primary"}
                        size="small"
                        disabled={request.accepted || processingId === request.id}
                        sx={{ 
                          minWidth: '100px',
                          width: '100px',
                          height: '36px'
                        }}
                        onClick={() => !request.accepted && handleInstall(request.id)}
                      >
                        {processingId === request.id ? (
                          <CircularProgress size={24} color="inherit" />
                        ) : (
                          request.accepted ? 'Aceito' : 'Aceitar'
                        )}
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Paper>
      
      {/* Notification Snackbar */}
      <Snackbar 
        open={notification.open} 
        autoHideDuration={6000} 
        onClose={handleCloseNotification}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert onClose={handleCloseNotification} severity={notification.severity} sx={{ width: '100%' }}>
          {notification.message}
        </Alert>
      </Snackbar>
    </Container>
  );
}
