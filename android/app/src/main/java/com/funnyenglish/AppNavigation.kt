@file:Suppress("MatchingDeclarationName")

package com.funnyenglish

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.funnyenglish.feature.chat.ChatScreen
import com.funnyenglish.feature.dictionary.DictionaryScreen
import com.funnyenglish.feature.games.GamesScreen
import com.funnyenglish.feature.home.HomeScreen
import com.funnyenglish.feature.profile.ProfileScreen
import com.funnyenglish.feature.quiz.QuizScreen

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : BottomNavItem("home", "Главная", Icons.Default.Home)
    data object Dictionary : BottomNavItem("dictionary", "Словарь", Icons.Default.Search)
    data object Quiz : BottomNavItem("quiz", "Квиз", Icons.Default.PlayArrow)
    data object Chat : BottomNavItem("chat", "Чат", Icons.Default.Email)
    data object Profile : BottomNavItem("profile", "Профиль", Icons.Default.Person)
}

private const val NAV_ANIMATION_DURATION = 300

private val bottomNavItems =
    listOf(
        BottomNavItem.Home,
        BottomNavItem.Dictionary,
        BottomNavItem.Quiz,
        BottomNavItem.Chat,
        BottomNavItem.Profile
    )

@Composable
private fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(tween(NAV_ANIMATION_DURATION)) +
                slideInVertically(tween(NAV_ANIMATION_DURATION)) { it / 8 }
        },
        exitTransition = {
            fadeOut(tween(NAV_ANIMATION_DURATION)) +
                slideOutVertically(tween(NAV_ANIMATION_DURATION)) { it / 8 }
        },
        popEnterTransition = { fadeIn(tween(NAV_ANIMATION_DURATION)) },
        popExitTransition = {
            fadeOut(tween(NAV_ANIMATION_DURATION)) +
                slideOutVertically(tween(NAV_ANIMATION_DURATION)) { it / 8 }
        }
    ) {
        composable(BottomNavItem.Home.route) {
            HomeScreen(onNavigateTo = { navController.navigate(it) })
        }
        composable(BottomNavItem.Dictionary.route) { DictionaryScreen() }
        composable(BottomNavItem.Quiz.route) { QuizScreen() }
        composable(BottomNavItem.Chat.route) { ChatScreen() }
        composable(BottomNavItem.Profile.route) { ProfileScreen() }
        composable("games") { GamesScreen() }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        AppNavHost(navController = navController, modifier = Modifier.padding(paddingValues))
    }
}
