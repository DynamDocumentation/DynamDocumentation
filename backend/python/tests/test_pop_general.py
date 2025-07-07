import pytest
import types
from pop_general import (
    is_torch_module, is_jax_module, is_tensorflow_module, is_numpy_module,
    is_requests_module, is_matplotlib_module, is_seaborn_module, get_function_signature
)

def test_is_torch_module():
    assert is_torch_module('torch')
    assert is_torch_module('torch.nn')
    assert not is_torch_module('numpy')

def test_is_jax_module():
    assert is_jax_module('jax')
    assert is_jax_module('jax.numpy')
    assert not is_jax_module('torch')

def test_is_tensorflow_module():
    assert is_tensorflow_module('tensorflow')
    assert is_tensorflow_module('tf')
    assert is_tensorflow_module('tensorflow.keras')
    assert not is_tensorflow_module('jax')

def test_is_numpy_module():
    assert is_numpy_module('numpy')
    assert is_numpy_module('numpy.linalg')
    assert not is_numpy_module('torch')

def test_is_requests_module():
    assert is_requests_module('requests')
    assert is_requests_module('requests.api')
    assert not is_requests_module('numpy')

def test_is_matplotlib_module():
    assert is_matplotlib_module('matplotlib')
    assert is_matplotlib_module('matplotlib.pyplot')
    assert not is_matplotlib_module('seaborn')

def test_is_seaborn_module():
    assert is_seaborn_module('seaborn')
    assert is_seaborn_module('seaborn.axisgrid')
    assert not is_seaborn_module('matplotlib')

def test_get_function_signature():
    def foo(a, b=2):
        return a + b
    sig = get_function_signature(foo)
    assert 'foo' in sig
    assert 'a' in sig
    assert 'b' in sig
