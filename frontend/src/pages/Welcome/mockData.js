export const getNamespaces = (library) => {
    if (library === 'numpy') {
        return [
            { id: 1, name: 'numpy.core' },
            { id: 2, name: 'numpy.random' },
            { id: 3, name: 'numpy.linalg' },
            { id: 4, name: 'numpy.fft' }
        ];
    } else if (library === 'sklearn') {
        return [
            { id: 5, name: 'sklearn.cluster' },
            { id: 6, name: 'sklearn.linear_model' },
            { id: 7, name: 'sklearn.tree' },
            { id: 8, name: 'sklearn.ensemble' }
        ];
    }
    return [];
};

export const getFunctions = (namespace) => {
    if (namespace.name === 'numpy.random') {
        return [
            { 
                id: 1, 
                name: 'rand', 
                signature: 'rand(d0, d1, ..., dn)',
                docstring: {
                    description: 'Gera uma matriz de números aleatórios uniformemente distribuídos no intervalo [0, 1).',
                    parameters: {
                        d0: { type: 'int', description: 'A dimensão do array resultante' },
                        d1: { type: 'int', description: 'A dimensão do array resultante' }
                    },
                    returns: 'ndarray de forma (d0, d1, ..., dn), preenchido com números aleatórios',
                    examples: '>>> np.random.rand(3,2)\narray([[0.14022471, 0.96360618],\n       [0.37601032, 0.25528411],\n       [0.49313049, 0.94909878]])'
                }
            },
            { 
                id: 2, 
                name: 'randn', 
                signature: 'randn(d0, d1, ..., dn)',
                docstring: {
                    description: 'Retorna uma amostra de números aleatórios da distribuição normal (Gaussiana).',
                    parameters: {
                        d0: { type: 'int', description: 'A dimensão do array resultante' },
                        d1: { type: 'int', description: 'A dimensão do array resultante' }
                    },
                    returns: 'ndarray de forma (d0, d1, ..., dn), preenchido com números aleatórios',
                    examples: '>>> np.random.randn(3,2)\narray([[ 1.5089, -0.2282],\n       [ 0.3407, -0.7559],\n       [ 0.8900, -0.8212]])'
                }
            }
        ];
    } else if (namespace.name === 'sklearn.cluster') {
        return [
            { 
                id: 3, 
                name: 'KMeans', 
                signature: 'KMeans(n_clusters=8, *, init="k-means++", n_init=10, ...)',
                docstring: {
                    description: 'K-Means clustering.\n\nImplementa o algoritmo k-means para agrupamento.',
                    parameters: {
                        n_clusters: { type: 'int', description: 'O número de clusters a formar e o número de centróides a gerar.' },
                        init: { type: 'str ou array', description: 'Método para inicialização.' }
                    },
                    examples: '>>> from sklearn.cluster import KMeans\n>>> import numpy as np\n>>> X = np.array([[1, 2], [1, 4], [1, 0], [10, 2], [10, 4], [10, 0]])\n>>> kmeans = KMeans(n_clusters=2, random_state=0).fit(X)\n>>> kmeans.labels_\narray([1, 1, 1, 0, 0, 0])'
                }
            },
            { 
                id: 4, 
                name: 'DBSCAN', 
                signature: 'DBSCAN(eps=0.5, *, min_samples=5, metric="euclidean", ...)',
                docstring: {
                    description: 'Perform DBSCAN clustering from vector array or distance matrix.',
                    parameters: {
                        eps: { type: 'float', description: 'A distância máxima entre duas amostras para que uma seja considerada vizinha da outra.' },
                        min_samples: { type: 'int', description: 'O número mínimo de amostras em um bairro para que um ponto seja considerado como um ponto central.' }
                    },
                    examples: '>>> from sklearn.cluster import DBSCAN\n>>> import numpy as np\n>>> X = np.array([[1, 2], [2, 2], [2, 3], [8, 7], [8, 8], [25, 80]])\n>>> clustering = DBSCAN(eps=3, min_samples=2).fit(X)\n>>> clustering.labels_\narray([0, 0, 0, 1, 1, -1])'
                }
            }
        ];
    }
    return [];
};