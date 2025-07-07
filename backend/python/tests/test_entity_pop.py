from unittest.mock import patch, MagicMock
import builtins
from data_create import entity_pop

def test_populate_entities_from_namespaces(monkeypatch):
    mock_conn = MagicMock()
    mock_cursor = MagicMock()
    mock_conn.cursor.return_value = mock_cursor
    monkeypatch.setattr(entity_pop.mariadb, 'connect', lambda **kwargs: mock_conn)
    # Simulate Namespaces returned from DB
    def fetchall_side_effect():
        if not hasattr(fetchall_side_effect, 'called'):
            fetchall_side_effect.called = True
            return [(1, 'lib.mod1'), (2, 'lib.mod2')]
        return []
    mock_cursor.fetchall.side_effect = fetchall_side_effect
    # Patch os.path and open to simulate JSON files
    monkeypatch.setattr(entity_pop.os.path, 'isfile', lambda path: True)
    monkeypatch.setattr(entity_pop.os.path, 'isdir', lambda path: True)
    monkeypatch.setattr(entity_pop.os, 'listdir', lambda path: ['lib'])
    def fake_json_load(f):
        return {
            "classes": [
                {"name": "A", "documentation": {"description": "desc", "examples": "ex"}, "methods": [
                    {"name": "m", "signature": "sig", "documentation": {"description": "mdesc", "returns": "ret", "examples": "mex"}}
                ]}
            ],
            "functions": [
                {"name": "f", "signature": "fsig", "documentation": {"description": "fdesc", "returns": "fret", "examples": "fex"}}
            ]
        }
    monkeypatch.setattr(entity_pop.json, 'load', fake_json_load)
    with patch.object(builtins, 'open', lambda *a, **k: MagicMock()):
        entity_pop.populate_entities_from_namespaces(output_dir="/fake")
    sqls = [call[0][0] for call in mock_cursor.execute.call_args_list]
    print('Executed SQLs:', sqls)
    assert any("INSERT INTO Classes" in sql for sql in sqls)
    assert any("INSERT INTO Functions" in sql for sql in sqls)
