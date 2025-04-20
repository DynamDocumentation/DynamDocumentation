// src/App.js

import React, { useState, useEffect } from "react";
import axios from "axios";

function App() {
  const [users, setUsers] = useState([]);

  useEffect(() => {
    // Faz a requisição para o backend em Kotlin
    axios.get("http://localhost:8080/users")
      .then(response => {
        setUsers(response.data);
      })
      .catch(error => {
        console.error("Erro ao carregar os dados", error);
      });
  }, []);

  return (
    <div className="App">
      <h1>Usuários</h1>
      <ul>
        {users.map(user => (
          <li key={user.id}>
            {user.name} - {user.email}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default App;
