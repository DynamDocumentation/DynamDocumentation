package com.dynam.models

import kotlinx.serialization.Serializable

import com.dynam.utils.*

interface EntityFacade {

    fun getAllEntityNamesFrom(namespace: Namespace) : List<String>

    fun getDetailsOf(namespace: String?) : EntityDetails
}