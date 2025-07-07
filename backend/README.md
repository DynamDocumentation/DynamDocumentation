# Backend - DynamDocumentation

Este diretório contém o backend do projeto DynamDocumentation, incluindo serviços em Kotlin e scripts/utilitários em Python.

## Banco de Dados
O backend utiliza um banco de dados MariaDB. É necessário que exista uma instância do MariaDB acessível com:
- Nome do banco: `dynam`
- Usuário: `root` (ou outro configurado no projeto)
- Senha: `1234`

Certifique-se de que o banco esteja criado e acessível antes de executar o backend.

## Estrutura
- `build.gradle.kts`, `settings.gradle.kts`: Configuração do projeto Kotlin.
- `python/`: Scripts e módulos Python para manipulação de dados e testes.
- `output/`: Dados e arquivos auxiliares.

## Como Executar

### Backend Kotlin
1. Entre na pasta `backend/`.
2. Execute:
   ```sh
   ./gradlew run
   ```

### Scripts Python
1. Entre na pasta `backend/python/`.
2. Instale as dependências (se necessário):
   ```sh
   pip install -r requirements.txt
   ```
3. Execute os scripts conforme necessário.

## Como Rodar os Testes Python
Para executar os testes automatizados do backend (Python):

1. Entre na pasta dos scripts Python:
   ```sh
   cd backend/python
   ```
2. Execute o comando abaixo para rodar todos os testes com detalhes:
   ```sh
   pytest -v
   ```

Se preferir, pode rodar apenas um arquivo de teste específico:
```sh
pytest -v tests/nome_do_arquivo.py
```

## Como Rodar os Testes Kotlin
Para executar os testes automatizados do backend Kotlin:

1. Entre na pasta `backend/`:
   ```sh
   cd backend
   ```
2. Execute o comando:
   ```sh
   ./gradlew test
   ```

Os relatórios de teste serão gerados em `backend/build/reports/tests/test/index.html`.
