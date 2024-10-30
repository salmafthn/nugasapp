package com.example.nugasapp

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nugasapp.data.local.TugasRepository
import com.example.nugasapp.navigation.NavigationItem
import com.example.nugasapp.navigation.Screen
import com.example.nugasapp.screen.MatkulScreen
import com.example.nugasapp.screen.ProfileScreen
import com.example.nugasapp.screen.TugasScreen
import com.example.nugasapp.ui.theme.Modul2Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        setContent {
            Modul2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainActivityContent(auth = auth)
                }
            }
        }
    }
}

@Composable
fun MainActivityContent(
    auth: FirebaseAuth,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
    application: Application = LocalContext.current.applicationContext as Application
) {
    // Create an instance of TugasRepository
    val tugasRepository = TugasRepository(application)

    val showBottomBar = remember { mutableStateOf(true) }

    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    LaunchedEffect(currentBackStackEntry.value) {
        showBottomBar.value = currentBackStackEntry.value?.destination?.route !in listOf("login")
    }

    Scaffold(
        bottomBar = { if (showBottomBar.value) BottomBar(navController) },
        modifier = modifier
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = if (auth.currentUser != null) Screen.Matkul.route else "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Matkul.route) {
                MatkulScreen(auth = auth, navController = navController)
            }
            composable(Screen.Profil.route) {
                ProfileScreen(auth = auth, navController = navController, tugasRepository = tugasRepository) // Pass the repository here
            }
            composable("login") {
                LoginScreen(auth = auth, navController = navController)
            }
            composable(Screen.Tugas.route) {
                TugasScreen(application = application) // Pass the application context here
            }
        }
    }
}


@Composable
private fun BottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
    ) {
        val navigationItems = listOf(
            NavigationItem(
                title = stringResource(R.string.matkul),
                icon = painterResource(R.drawable.books),
                screen = Screen.Matkul
            ),

            NavigationItem(
                title = stringResource(R.string.tugas),
                icon = painterResource(R.drawable.tugas),
                screen = Screen.Tugas
            ),
            NavigationItem(
                title = stringResource(R.string.profil),
                icon = painterResource(R.drawable.github),
                screen = Screen.Profil
            )
        )
        val backStackEntry = navController.currentBackStackEntryAsState()

        navigationItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(item.title) },
                selected = false,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        restoreState = true
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}