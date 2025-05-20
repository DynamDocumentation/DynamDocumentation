package com.dynam.models

import com.dynam.utils.*

class ClassModel : ClassModelFacade {

    override fun getAllEntityNamesFrom(namespace: Namespace) : List<String> {
        val testTable = mapOf(
            "sklearn.compose" to listOf("ColumnTransformer", "TransformedTargetRegressor"),
            "sklearn.covariance" to listOf(
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

        return testTable.get(namespace.name) ?: listOf("null")
    }

    override fun getDetailsOf(namespace: String?) : EntityDetails {
        return EntityDetails(EntityType.CLASS, "huh", null, null)
    }
}