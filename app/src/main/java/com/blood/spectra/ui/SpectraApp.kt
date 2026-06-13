package com.blood.spectra.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.InvertColors as InvertColorsFilled
import androidx.compose.material.icons.filled.Palette as PaletteFilled
import androidx.compose.material.icons.outlined.Colorize as ColorizeOutlined
import androidx.compose.material.icons.outlined.InvertColors as InvertColorsOutlined
import androidx.compose.material.icons.outlined.Palette as PaletteOutlined
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.blood.spectra.ui.contrast.ContrastScreen
import com.blood.spectra.ui.palettes.PalettesScreen
import com.blood.spectra.ui.picker.PickerScreen

private enum class Tab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val icon: ImageVector,
) {
    PICKER("picker", "Picker", Icons.Filled.Colorize, ColorizeOutlined),
    PALETTES("palettes", "Palettes", PaletteFilled, PaletteOutlined),
    CONTRAST("contrast", "Contrast", InvertColorsFilled, InvertColorsOutlined),
}

@Composable
fun SpectraApp() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentDest = backStack?.destination

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                Tab.entries.forEach { tab ->
                    val selected = currentDest?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                if (selected) tab.selectedIcon else tab.icon,
                                contentDescription = tab.label,
                            )
                        },
                        label = { Text(tab.label, style = MaterialTheme.typography.labelMedium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Tab.PICKER.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(Tab.PICKER.route) { PickerScreen() }
            composable(Tab.PALETTES.route) { PalettesScreen() }
            composable(Tab.CONTRAST.route) { ContrastScreen() }
        }
    }
}
