from unittest.mock import patch, MagicMock
import builtins
from data_create import var_pop

def test_populate_variables(monkeypatch):
    mock_conn = MagicMock()
    mock_cursor = MagicMock()
    mock_conn.cursor.return_value = mock_cursor
    monkeypatch.setattr(var_pop.mariadb, 'connect', lambda **kwargs: mock_conn)
    # Simulate classes and functions returned from DB
    def fetchall_side_effect():
        if not hasattr(fetchall_side_effect, 'called'):
            fetchall_side_effect.called = True
            return [(1, 'A', 'lib')]
        # Function: func_id, func_name, parent_class_id, parent_class_name, parent_class_namespace, parent_namespace_id, parent_namespace_name
        return [(2, 'f', None, None, None, None, 'lib.mod2')]
    mock_cursor.fetchall.side_effect = fetchall_side_effect
    # Patch os.path and open to simulate JSON files
    monkeypatch.setattr(var_pop.os.path, 'isfile', lambda path: True)
    monkeypatch.setattr(var_pop.os.path, 'isdir', lambda path: True)
    monkeypatch.setattr(var_pop.os, 'listdir', lambda path: ['lib'])
    def fake_json_load(f):
        return {
            "classes": [
                {"name": "A", "documentation": {"parameters": {"x": {"type": "int", "description": "desc"}}}}
            ],
            "functions": [
                {"name": "f", "documentation": {"parameters": {"y": {"type": "float", "description": "desc2"}}}}
            ]
        }
    monkeypatch.setattr(var_pop.json, 'load', fake_json_load)
    with patch.object(builtins, 'open', lambda *a, **k: MagicMock()):
        var_pop.populate_variables(output_dir="/fake")
    sqls = [call[0][0] for call in mock_cursor.execute.call_args_list]
    print('Executed SQLs:', sqls)
    assert any("INSERT INTO Variables" in sql for sql in sqls)
