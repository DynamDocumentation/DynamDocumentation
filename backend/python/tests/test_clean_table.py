from unittest.mock import patch, MagicMock
from data_create.clean_table import clean_table as clean_table_func

def test_clean_table_success():
    mock_conn = MagicMock()
    mock_cursor = MagicMock()
    mock_conn.cursor.return_value = mock_cursor
    # Simulate table exists
    mock_cursor.fetchone.return_value = True
    with patch('mariadb.connect', return_value=mock_conn):
        clean_table_func('TestTable')
        mock_cursor.execute.assert_any_call("SHOW TABLES LIKE 'TestTable'")
        mock_cursor.execute.assert_any_call("DELETE FROM TestTable;")
        mock_cursor.execute.assert_any_call("ALTER TABLE TestTable AUTO_INCREMENT = 1;")
        mock_conn.commit.assert_called_once()
        mock_cursor.close.assert_called_once()
        mock_conn.close.assert_called()

def test_clean_table_table_not_exist():
    mock_conn = MagicMock()
    mock_cursor = MagicMock()
    mock_conn.cursor.return_value = mock_cursor
    # Simulate table does not exist
    mock_cursor.fetchone.return_value = False
    with patch('mariadb.connect', return_value=mock_conn):
        clean_table_func('NonExistent')
        mock_cursor.execute.assert_called_with("SHOW TABLES LIKE 'NonExistent'")
        mock_conn.close.assert_called()
