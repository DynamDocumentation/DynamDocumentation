import sys
import types
import builtins
import MainDatabase

def test_main_calls_clean_and_populate(monkeypatch):
    calls = []
    # Mock modules
    class MockClean:
        @staticmethod
        def clean_table(name):
            calls.append(f"clean:{name}")
    class MockPopNS:
        @staticmethod
        def populate_namespaces_from_output(path):
            calls.append(f"popNS:{path}")
    class MockPopEntities:
        @staticmethod
        def populate_entities_from_namespaces(path):
            calls.append(f"popEntities:{path}")
    class MockPopVars:
        @staticmethod
        def populate_variables(path):
            calls.append(f"popVars:{path}")
    monkeypatch.setattr(MainDatabase, 'clean', MockClean)
    monkeypatch.setattr(MainDatabase, 'popNameSpaces', MockPopNS)
    monkeypatch.setattr(MainDatabase, 'popEntities', MockPopEntities)
    monkeypatch.setattr(MainDatabase, 'popVariables', MockPopVars)
    MainDatabase.main()
    assert any("clean:Variables" in c for c in calls)
    assert any("popNS:" in c for c in calls)
    assert any("popEntities:" in c for c in calls)
    assert any("popVars:" in c for c in calls)
