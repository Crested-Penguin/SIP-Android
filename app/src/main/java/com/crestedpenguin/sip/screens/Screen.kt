package com.crestedpenguin.sip.screens

sealed class Screen(val route: String, val contentDescription: String) {
    data object Home: Screen("home", "Home")
    data object Search: Screen("search", "Search")
    data object Star: Screen("star", "Star")
    data object Settings: Screen("settings", "Settings")
}