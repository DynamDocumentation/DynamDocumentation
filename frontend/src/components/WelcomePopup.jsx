import React from 'react';
import { 
  Dialog, 
  DialogTitle, 
  DialogContent, 
  DialogActions, 
  Button, 
  Typography, 
  Box 
} from '@mui/material';
import LibraryBooksIcon from '@mui/icons-material/LibraryBooks';

const WelcomePopup = ({ open, onClose }) => {
  return (
    <Dialog 
      open={open} 
      onClose={onClose}
      maxWidth="sm"
      fullWidth
      PaperProps={{
        elevation: 5,
        sx: { borderRadius: 2 }
      }}
    >
      <DialogTitle sx={{ bgcolor: 'primary.main', color: 'white', pb: 1 }}>
        <Box display="flex" alignItems="center" gap={1}>
          <LibraryBooksIcon />
          <Typography variant="h5" component="span" fontWeight="500">
            Bem vindo ao DynamDocumentation
          </Typography>
        </Box>
      </DialogTitle>
      <DialogContent sx={{ mt: 2 }}>
        <Typography variant="body1" paragraph>
          Este é um sistema interativo que permite explorar documentações de bibliotecas de forma dinâmica e eficiente.
        </Typography>
        <Typography variant="body1" paragraph>
          Para começar, clique em "Adicionar Biblioteca" para selecionar uma biblioteca 
          e visualizar sua documentação. Você pode dividir a tela em múltiplos painéis para 
          comparar diferentes partes da documentação.
        </Typography>
        <Typography variant="body1" paragraph>
          Funcionalidades principais:
        </Typography>
        <Box component="ul" sx={{ pl: 3 }}>
          <li>
            <Typography variant="body1">
              <strong>Múltiplos painéis:</strong> Utilize o botão + para adicionar novos painéis
            </Typography>
          </li>
          <li>
            <Typography variant="body1">
              <strong>Navegação contextual:</strong> Acesse facilmente namespaces relacionados
            </Typography>
          </li>
          <li>
            <Typography variant="body1">
              <strong>Visualização detalhada:</strong> Examine funções, classes e métodos
            </Typography>
          </li>
        </Box>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button 
          onClick={onClose} 
          variant="contained" 
          color="primary"
          size="large"
        >
          Começar a usar
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default WelcomePopup;
