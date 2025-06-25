import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import reportWebVitals from './reportWebVitals';
import App from './App';
import { createTheme, ThemeProvider } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    mode: "dark",
    primary: {
      main: "#6366f1", // Roxo azulado moderno
      contrastText: "#fff",
    },
    secondary: {
      main: "#06b6d4", // Ciano para detalhes
    },
    background: {
      default: "#181f2a", // Azul escuro para o fundo principal
      paper: "#23293a",   // Azul escuro um pouco mais claro para cards/painéis
    },
    text: {
      primary: "#e0e6ed", // Quase branco
      secondary: "#94a3b8", // Cinza azulado
    },
    divider: "#23293a",
    success: {
      main: "#22d3ee",
    },
    info: {
      main: "#818cf8",
    },
  },
  components: {
    MuiAppBar: {
      styleOverrides: {
        colorPrimary: {
          backgroundColor: "#181f2a",
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          backgroundImage: "none",
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          backgroundColor: "#1a2233",
        },
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          "&.Mui-selected, &.Mui-selected:hover": {
            backgroundColor: "rgba(255,255,255,0.08)", // branco translúcido
            color: "#fff",
          },
          "&:hover": {
            backgroundColor: "rgba(255,255,255,0.08)", // branco translúcido
          },
        },
      },
    },
  },
});

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <ThemeProvider theme={theme}>
      <App />
    </ThemeProvider>
  </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
