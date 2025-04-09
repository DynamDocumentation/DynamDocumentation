import { useEffect, useState } from 'react';
import './App.css';

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
        <h1>Usuários</h1>
        {users.length === 0 ? (
          <p>Carregando usuários...</p>
        ) : (
          <ul>
            {users.map((user, idx) => (
              <li key={idx}>{JSON.stringify(user)}</li>
            ))}
          </ul>
        )}
      </header>
    </div>
  );
}

export default App;

