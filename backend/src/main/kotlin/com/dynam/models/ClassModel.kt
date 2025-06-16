package com.dynam.models

class ClassModel : NamespaceChildInterface {

    override fun getFromNamespace(namespace: String) : Array<String> {
        val testTable = mapOf(
            "sklearn.compose" to arrayOf("ColumnTransformer", "TransformedTargetRegressor"),
            "sklearn.covariance" to arrayOf(
                                        "EllipticEnvelope",
                                        "EmpiricalCovariance",
                                        "GraphicalLasso",
                                        "GraphicalLassoCV",
                                        "LedoitWolf",
                                        "MinCovDet",
                                        "OAS",
                                        "ShrunkCovariance"
                                    )
        )

        return testTable.get(namespace) ?: arrayOf("null")
    }

    override fun getDetailsOf(namespace: String?) : ChildDetails {
        return ChildDetails("huh", null, null)
    }
}