import React, { useState } from 'react';
import axios from 'axios';
import { 
  Box, 
  Button, 
  TextField, 
  Typography, 
  CircularProgress, 
  Paper, 
  Alert,
  Container
} from '@mui/material';

const LibraryInput = () => {
  const [library, setLibrary] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!library.trim()) {
      setError('Please enter a library name');
      return;
    }

    setLoading(true);
    setResult(null);
    setError(null);

    try {
      // Use absolute URL instead of relative
      const response = await axios.post('http://localhost:8080/api/library/install', { libraryName: library });
      setResult(response.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to process library. Please try again.');
      console.error('Error:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="md">
      <Paper elevation={3} sx={{ p: 4, mt: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Library Documentation Generator
        </Typography>
        
        <Typography variant="body1" paragraph>
          Enter the name of a Python library to generate documentation JSON files.
        </Typography>
        
        <Box component="form" onSubmit={handleSubmit} sx={{ mb: 3 }}>
          <TextField
            fullWidth
            label="Library Name"
            variant="outlined"
            value={library}
            onChange={(e) => setLibrary(e.target.value)}
            placeholder="e.g., numpy, torch, sklearn"
            disabled={loading}
            margin="normal"
          />
          
          <Button 
            type="submit" 
            variant="contained" 
            color="primary"
            disabled={loading}
            sx={{ mt: 2 }}
          >
            {loading ? <CircularProgress size={24} /> : 'Generate Documentation'}
          </Button>
        </Box>
        
        {error && (
          <Alert severity="error" sx={{ mt: 2 }}>
            {error}
          </Alert>
        )}
        
        {result && (
          <Alert severity="success" sx={{ mt: 2 }}>
            Documentation for {library} has been generated successfully!
          </Alert>
        )}
      </Paper>
    </Container>
  );
};

export default LibraryInput;