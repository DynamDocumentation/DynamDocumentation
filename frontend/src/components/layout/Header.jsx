import React, { useState } from "react";
import { 
  AppBar, 
  Toolbar, 
  Typography, 
  Button, 
  Box,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Tooltip,
  Alert,
  CircularProgress
} from "@mui/material";
import { Link as RouterLink } from "react-router-dom";
import PersonIcon from '@mui/icons-material/Person';
import LibraryBooksIcon from '@mui/icons-material/LibraryBooks';
import AddIcon from '@mui/icons-material/Add';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import axios from 'axios';

export default function Header({ onAdminClick }) {
  const [dialogOpen, setDialogOpen] = useState(false);
  const [libraryName, setLibraryName] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [requestStatus, setRequestStatus] = useState(null); // Can be 'success', 'error', or null
  const [statusMessage, setStatusMessage] = useState('');
  
  const handleOpenDialog = () => {
    setDialogOpen(true);
  };
  
  const handleCloseDialog = () => {
    setDialogOpen(false);
    // Reset all form state
    setLibraryName('');
    setRequestStatus(null);
    setStatusMessage('');
  };
  
  const handleSubmit = async () => {
    // Skip submission if library name is empty
    if (!libraryName.trim()) return;
    
    // Reset previous status and set submitting state
    setRequestStatus(null);
    setStatusMessage('');
    setSubmitting(true);
    
    try {
      // Send request to the backend endpoint
      const response = await axios.post('/api/library/requests', {
        name: libraryName.trim()
      });
      
      // Handle successful response
      console.log('Library request submitted successfully:', response.data);
      
      // Show success message
      setRequestStatus('success');
      setStatusMessage('Solicitação enviada com sucesso!');
      
      // Clear the form but keep dialog open
      setLibraryName('');
      
    } catch (error) {
      console.error('Error submitting library request:', error);
      
      // Set appropriate error message
      setRequestStatus('error');
      
      if (error.response?.status === 409) {
        setStatusMessage('Esta biblioteca já foi solicitada anteriormente.');
      } else {
        setStatusMessage('Erro ao enviar solicitação. Tente novamente.');
      }
      
    } finally {
      // Reset submitting state
      setSubmitting(false);
    }
  };
  
  return (
    <AppBar position="static" color="primary">
      <Toolbar>
        <Typography 
          variant="h6" 
          component={RouterLink} 
          to="/" 
          sx={{ 
            flexGrow: 1, 
            color: 'white', 
            textDecoration: 'none' 
          }}
        >
          DynamDocumentation
        </Typography>
        
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button 
            color="inherit" 
            startIcon={<LibraryBooksIcon />}
            component={RouterLink}
            to="/library-requests"
          >
            Solicitações
          </Button>
          
          <Tooltip title="Solicitar Nova Biblioteca">
            <Button 
              color="inherit" 
              startIcon={<AddIcon />}
              onClick={handleOpenDialog}
            >
              Nova Solicitação
            </Button>
          </Tooltip>
          
          <Button 
            color="inherit" 
            startIcon={<PersonIcon />}
            onClick={onAdminClick}
          >
            Admin
          </Button>
        </Box>
      </Toolbar>
      
      {/* Dialog for submitting library requests */}
      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>Solicitar Nova Biblioteca</DialogTitle>
        <DialogContent>
          <Typography variant="body2" sx={{ mb: 2 }}>
            Solicite a inclusão de uma nova biblioteca na documentação.
          </Typography>
          
          {/* Status notification */}
          {requestStatus && (
            <Alert 
              severity={requestStatus} 
              sx={{ mb: 2 }}
              onClose={() => {
                setRequestStatus(null);
                setStatusMessage('');
              }}
            >
              {statusMessage}
            </Alert>
          )}
          
          <TextField
            autoFocus={!requestStatus} // Only autofocus if there's no status message
            margin="dense"
            label="Nome da Biblioteca"
            type="text"
            fullWidth
            variant="outlined"
            value={libraryName}
            onChange={(e) => setLibraryName(e.target.value)}
            placeholder="Ex: pandas, matplotlib, scikit-learn"
          />
          
          <Alert 
            severity="info" 
            icon={<InfoOutlinedIcon fontSize="inherit" />}
            sx={{ 
              mt: 2, 
              mb: 1,
              '& .MuiAlert-message': { 
                display: 'flex',
                alignItems: 'center'
              }
            }}
          >
                Digite o nome exato da biblioteca conforme aparece no PyPI.
          </Alert>
        </DialogContent>
        <DialogActions>
          <Button 
            onClick={handleCloseDialog} 
            color="error"
            disabled={submitting}
          >
            {requestStatus === 'success' ? 'Fechar' : 'Cancelar'}
          </Button>
          {requestStatus !== 'success' && (
            <Button 
              onClick={handleSubmit} 
              color="primary" 
              variant="contained"
              disabled={!libraryName.trim() || submitting}
              startIcon={submitting ? <CircularProgress size={20} color="inherit" /> : null}
            >
              {submitting ? 'Enviando...' : 'Enviar Solicitação'}
            </Button>
          )}
        </DialogActions>
      </Dialog>
    </AppBar>
  );
}
