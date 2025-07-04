import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';

// Layout Component
import Layout from './components/layout/Layout';

// Page Components
import Welcome from "./pages/Welcome";
import Admin from "./pages/Admin";
import Register from "./pages/Register";
import LibraryRequest from "./pages/LibraryRequest";

const theme = createTheme({
  palette: {
    primary: {
      main: '#556cd6',
    },
    secondary: {
      main: '#19857b',
    },
    background: {
      default: '#f5f5f5',
      paper: '#ffffff',
    },
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <Routes>
          {/* Layout Route with Nested Routes */}
          <Route element={<Layout />}>
            <Route path="/" element={<Welcome />} />
            <Route path="/admin" element={<Admin />} />
            <Route path="/register" element={<Register />} />
            <Route path="/library-requests" element={<LibraryRequest />} />
          </Route>
        </Routes>
      </Router>
    </ThemeProvider>
  );
}

export default App;