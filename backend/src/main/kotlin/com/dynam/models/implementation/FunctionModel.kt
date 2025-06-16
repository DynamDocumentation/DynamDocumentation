package com.dynam.models

import com.dynam.utils.*

class FunctionModel : FunctionModelFacade {

    override fun getAllEntityNamesFrom(namespace: Namespace) : List<String> {
        val testTable = mapOf(
            "sklearn.compose" to listOf("make_column_selector", "make_column_transformer"),
            "sklearn.covariance" to listOf(
                                        "empirical_covariance",
                                        "graphical_lasso",
                                        "ledoit_wolf",
                                        "ledoit_wolf_shrinkage",
                                        "oas",
                                        "shrunk_covariance",
                                    )
        )

        return testTable.get(namespace.name) ?: listOf("null")
    }

    override fun getDetailsOf(namespace: String?) : EntityDetails {
        val testFunction = EntityDetails(
            EntityType.FUNCTION,
            "sklearn.covariance.ShrunkCovariance",
            listOf(
                Value("store_precision", "bool", "True", "Specify if the estimated precision is stored."),
                Value("assume_centered", "bool", "False", "If True, data will not be centered before computation. Useful when working with data whose mean is almost, but not exactly zero. If False, data will be centered before computation."),
                Value("shrinkage", "float", "0.1", "Coefficient in the convex combination used for the computation of the shrunk estimate. Range is [0, 1].")
            ),
            listOf(
                Value("covariance_", "ndarray of shape (n_features, n_features)", null, "Estimated covariance matrix"),
                Value("location_", "ndarray of shape (n_features,)", null, "Estimated location, i.e. the estimated mean."),
                Value("precision_", "ndarray of shape (n_features, n_features)", null, "Estimated pseudo inverse matrix. (stored only if store_precision is True)"),
                Value("n_features_in_", "int", null, "Number of features seen during fit."),
                Value("feature_names_in_", "ndarray of shape (n_features_in_,)", null, "Names of features seen during fit. Defined only when X has feature names that are all strings.")
            )
        )
        return testFunction
    }
}