import React, { useState, useEffect } from "react";
import { 
  AppBar, 
  Toolbar, 
  Typography, 
  Button, 
  Box,
  Tooltip,
  Badge
} from "@mui/material";
import { Link as RouterLink } from "react-router-dom";
import PersonIcon from '@mui/icons-material/Person';
import AddIcon from '@mui/icons-material/Add';
import LockOpenIcon from '@mui/icons-material/LockOpen';
// Import the extracted dialog component
import LibraryRequestDialog from '../dialogs/LibraryRequestDialog';
import { isAuthenticated } from "../../utils/auth";

export default function Header({ onAdminClick }) {
  const [dialogOpen, setDialogOpen] = useState(false);
  const [authenticated, setAuthenticated] = useState(false);
  
  // Check authentication status when component mounts and when session storage changes
  useEffect(() => {
    const checkAuth = () => {
      setAuthenticated(isAuthenticated());
    };
    
    // Initial check
    checkAuth();
    
    // Add event listener for session storage changes
    const handleStorageChange = () => {
      checkAuth();
    };
    
    window.addEventListener('storage', handleStorageChange);
    
    // Clean up event listener
    return () => {
      window.removeEventListener('storage', handleStorageChange);
    };
  }, []);
  
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
            <Tooltip title="Solicitar Nova Biblioteca">
              <Button 
                color="inherit" 
                startIcon={<AddIcon />}
                onClick={handleOpenDialog}
              >
                Nova Solicitação
              </Button>
            </Tooltip>
            
            <Tooltip title={authenticated ? "Gerenciar Solicitações" : "Login de Administrador"}>
              <Button 
                color="inherit" 
                startIcon={authenticated ? <LockOpenIcon /> : <PersonIcon />}
                onClick={onAdminClick}
              >
                {authenticated ? "Gerenciar" : "Admin"}
              </Button>
            </Tooltip>
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
