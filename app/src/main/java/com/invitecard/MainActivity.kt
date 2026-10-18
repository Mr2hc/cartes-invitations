package com.invitecard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.invitecard.ui.Dashboard
import com.invitecard.ui.Editor
import com.invitecard.ui.dashboard.DashboardScreen
import com.invitecard.ui.editor.InvitationEditorScreen
import com.invitecard.ui.theme.InviteCardTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InviteCardTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController, 
        startDestination = Dashboard,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        composable<Dashboard> {
            DashboardScreen(
                viewModel = hiltViewModel(),
                onNavigateToEditor = { templateId ->
                    navController.navigate(Editor(templateId = templateId))
                }
            )
        }
        composable<Editor> {
            InvitationEditorScreen(viewModel = hiltViewModel())
        }
    }
}
