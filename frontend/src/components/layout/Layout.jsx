import React, { useState } from "react";
import { Box } from "@mui/material";
import { Outlet, useNavigate } from "react-router-dom";
import Header from "./Header";
import WelcomePopup from "../WelcomePopup";

export default function Layout() {
  const [showWelcomePopup, setShowWelcomePopup] = useState(false);
  const navigate = useNavigate();
  
  // Handle closing the welcome popup
  const handleCloseWelcomePopup = () => {
    setShowWelcomePopup(false);
    localStorage.setItem('hasVisitedBefore', 'true');
  };

  // Function for admin login button click - redirects to admin page
  const handleAdminLogin = () => {
    navigate('/admin');
  };

  return (
    <Box 
      sx={{ 
        height: '100vh', 
        width: '100%',
        backgroundColor: 'white',
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden'
      }}
    >
      {/* Shared Header */}
      <Header onAdminClick={handleAdminLogin} />
      
      {/* Content Area - Renders the current route */}
      <Box 
        sx={{ 
          height: 'calc(100vh - 64px)', /* Fixed height based on viewport minus AppBar height */
          width: '100%',
          overflow: 'auto',
        }}
      >
        <Outlet />
      </Box>
      
      {/* Welcome popup */}
      <WelcomePopup 
        open={showWelcomePopup} 
        onClose={handleCloseWelcomePopup} 
      />
    </Box>
  );
}
