from unittest.mock import patch, MagicMock
import os
import json
from data_create import namespace_pop

def test_populate_namespaces_from_output(monkeypatch, tmp_path):
    # Setup fake output dir and index.json
    output_dir = tmp_path / "lib"
    output_dir.mkdir(parents=True)
    index_path = output_dir / "index.json"
    with open(index_path, "w", encoding="utf-8") as f:
        json.dump({"modules": ["mod1", "mod2"]}, f)
    
    mock_conn = MagicMock()
    mock_cursor = MagicMock()
    mock_conn.cursor.return_value = mock_cursor
    monkeypatch.setattr(namespace_pop.mariadb, 'connect', lambda **kwargs: mock_conn)
    # Patch os.listdir to only return our fake lib
    monkeypatch.setattr(os, 'listdir', lambda path: ["lib"])
    # Patch os.path.isdir and os.path.isfile
    monkeypatch.setattr(os.path, 'isdir', lambda path: True)
    monkeypatch.setattr(os.path, 'isfile', lambda path: str(index_path) in path)
    namespace_pop.populate_namespaces_from_output(str(tmp_path))
    # Check that insert was called for each module
    calls = [call[0][1][0] for call in mock_cursor.execute.call_args_list if call[0][0].startswith("INSERT INTO")]
    assert "mod1" in calls and "mod2" in calls
