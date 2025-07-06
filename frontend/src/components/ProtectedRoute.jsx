import React, { useEffect } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { isAuthenticated } from '../utils/auth';

/**
 * Protected Route component that redirects unauthenticated users to the login page
 * @param {Object} props Component props
 * @param {React.ReactNode} props.children The component to render if authenticated
 * @returns {React.ReactNode} The children if authenticated, or Navigate to login if not
 */
const ProtectedRoute = ({ children }) => {
  const location = useLocation();
  
  // If not authenticated, redirect to login with current location in state for redirect back after login
  if (!isAuthenticated()) {
    return <Navigate to="/admin" state={{ from: location }} replace />;
  }

  // If authenticated, render the protected component
  return children;
};

export default ProtectedRoute;
