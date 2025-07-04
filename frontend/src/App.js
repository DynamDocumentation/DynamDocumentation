import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import Layout from './pages/Layout';
import LibraryInput from './pages/LibraryInput';
import Login from "./pages/Test";
import UserPage from './pages/UserPage';
import Welcome from "./pages/Welcome";
import Details from "./pages/Details";

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
          <Route path="/" element={<Layout />}>
            <Route index element={<Welcome />} />
            <Route path="/documentation" element={<div>Documentação</div>} />
            <Route path="/library-input" element={<LibraryInput />} />
            <Route path="/users" element={<div>Usuários</div>} />
            <Route path="/about" element={<div>Sobre</div>} />
            <Route path="details/:entityId" element={<Details />} />
          </Route>
        </Routes>
      </Router>
    </ThemeProvider>
  );
}

export default App;