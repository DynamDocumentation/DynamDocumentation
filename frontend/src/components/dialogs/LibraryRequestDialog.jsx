import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Alert,
  Typography,
  CircularProgress
} from '@mui/material';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import axios from 'axios';

const LibraryRequestDialog = ({ open, onClose }) => {
  const [libraryName, setLibraryName] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [requestStatus, setRequestStatus] = useState(null); // Can be 'success', 'error', or null
  const [statusMessage, setStatusMessage] = useState('');

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

  const handleClose = () => {
    // Reset all form state
    setLibraryName('');
    setRequestStatus(null);
    setStatusMessage('');
    
    // Call the parent onClose callback
    onClose();
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
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
          onClick={handleClose} 
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
  );
};

export default LibraryRequestDialog;
