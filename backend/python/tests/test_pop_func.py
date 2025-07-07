from pop_func import parse_docstring

def test_parse_docstring_numpy():
    doc = """
    Parameters
    ----------
    x : int
        The input value.
    Returns
    -------
    int
        The result.
    """
    result = parse_docstring(doc)
    assert isinstance(result, dict)
    assert 'parameters' in result  # lowercase key
    assert 'returns' in result or 'Returns' in result or 'return' in result  # accept any common variant

def test_parse_docstring_empty():
    doc = """"""
    result = parse_docstring(doc)
    assert isinstance(result, dict)
