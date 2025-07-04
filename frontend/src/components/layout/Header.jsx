import React, { useState } from "react";
import { 
  AppBar, 
  Toolbar, 
  Typography, 
  Button, 
  Box,
  Tooltip
} from "@mui/material";
import { Link as RouterLink } from "react-router-dom";
import PersonIcon from '@mui/icons-material/Person';
import LibraryBooksIcon from '@mui/icons-material/LibraryBooks';
import AddIcon from '@mui/icons-material/Add';
// Import the extracted dialog component
import LibraryRequestDialog from '../dialogs/LibraryRequestDialog';

export default function Header({ onAdminClick }) {
  const [dialogOpen, setDialogOpen] = useState(false);
  
  const handleOpenDialog = () => {
    setDialogOpen(true);
  };
  
  const handleCloseDialog = () => {
    setDialogOpen(false);
  };
  
  return (
    <>
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
      </AppBar>
      
      {/* Use the extracted dialog component */}
      <LibraryRequestDialog 
        open={dialogOpen} 
        onClose={handleCloseDialog} 
      />
    </>
  );
}
