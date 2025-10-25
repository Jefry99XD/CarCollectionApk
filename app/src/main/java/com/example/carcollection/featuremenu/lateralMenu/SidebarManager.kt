package com.example.carcollection.featuremenu.lateralMenu

import android.content.Context
import androidx.drawerlayout.widget.DrawerLayout

class SidebarManager(
    private val context: Context,
    private val drawerLayout: DrawerLayout,
) {
    init {
        setupMenu()
    }

    private fun setupMenu() {
    }

    fun openDrawer() {
        drawerLayout.open()
    }

    fun closeDrawer() {
        drawerLayout.close()
    }

    fun toggleDrawer() {
        if (drawerLayout.isOpen) closeDrawer() else openDrawer()
    }
}
