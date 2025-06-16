package com.dynam.controllers

interface NavigationControllerFacade {
    // Navigation should get everything for the side drawer
    fun getAllPathsForNavigation() : Map<String, Map<String, List<String>>>
}