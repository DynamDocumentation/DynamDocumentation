package com.dynam.models

class NamespaceModel : NamespaceInterface {
    override fun getNamespaces() : Array<String> {
        return arrayOf("sklearn", "sklearn.base", "sklearn.calibration", "sklearn.cluster", "sklearn.compose", "sklearn.covariance")
    }
}