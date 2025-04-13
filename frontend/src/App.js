import React, { useState, useEffect } from 'react';

function App() {
  const [users, setUsers] = useState([]);

  useEffect(() => {
    fetch("http://localhost:8080/users")
      .then((res) => res.json())
      .then((data) => {
        setUsers(data);
      })
      .catch((err) => {
        console.error("Erro ao buscar usuários:", err);
      });
  }, []);

  return (
    <div className="App">
      <header className="App-header">
        <h1>Lista de Usuários</h1>
        {users.length === 0 ? (
          <p>Carregando usuários...</p>
        ) : (
          <div className="user-list">
            {users.map((user) => (
              <div key={user.id} className="user-card">
                <h2>{user.name}</h2>
                <p>ID: {user.id}</p>
                <p>Email: {user.email}</p>
              </div>
            ))}
          </div>
        )}
      </header>
    </div>
  );
}

export default App;
