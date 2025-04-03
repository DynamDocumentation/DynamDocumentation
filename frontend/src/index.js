import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import reportWebVitals from './reportWebVitals';
import App from './App';
import { ThemeProvider, createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    // mode: "light",
    // primary: {
    //   main: "#697565",
    // },
    // background: {
    //   default: "#181C14",
    // },
    // text: {
    //   primary: "#ECDFCC",
    // },
    // divider: "#697565",
  },

  // components: {
  //   MuiAppBar: {
  //     styleOverrides: {
  //       colorPrimary: {
  //         backgroundColor: "#181C14",
  //       },
  //     },
  //   },
  // },
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
