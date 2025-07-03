import React from "react";
import { AppBar, Toolbar, Typography, Button } from "@mui/material";
import { Link as RouterLink } from "react-router-dom";
import PersonIcon from '@mui/icons-material/Person';

export default function Header({ onAdminClick }) {
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
        
        <Button 
          color="inherit" 
          startIcon={<PersonIcon />}
          onClick={onAdminClick}
        >
          Admin
        </Button>
      </Toolbar>
    </AppBar>
  );
}
