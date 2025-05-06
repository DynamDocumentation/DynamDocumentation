package com.dynam.models

import com.dynam.utils.*

interface NamespaceModelFacade {

    suspend fun getAllNamespaces() : List<Namespace> 
}