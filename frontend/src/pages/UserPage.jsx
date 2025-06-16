// src/pages/UsersPage.jsx

import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { List, ListItem, ListItemText, Typography, Divider } from '@mui/material';

export default function UserPage() {
  const [users, setUsers] = useState([]);

  useEffect(() => {
    axios.get('http://localhost:8080/users')
      .then(response => setUsers(response.data))
      .catch(error => console.error('Erro ao carregar usuários:', error));
  }, []);

  return (
    <div>
      <Typography variant="h4" gutterBottom>
        Lista de Usuários
      </Typography>
      <Divider sx={{ mb: 2 }} />
      <List>
        {users.map((user) => (
          <ListItem key={user.id}>
            <ListItemText primary={user.name} secondary={user.email} />
          </ListItem>
        ))}
      </List>
    </div>
  );
}

