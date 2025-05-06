package com.dynam.controllers

import com.dynam.utils.*

interface DetailsControllerFacade {
    // What should Details do
    // Details should send information about a name
    fun getEntityDetails(namespace: String, entityName: String, entityType: String) : EntityDetails
}