package com.example.nugasapp.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.nugasapp.data.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.nugasapp.data.local.TugasRepository
import com.example.nugasapp.data.viewmodel.MainViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    auth: FirebaseAuth,
    tugasRepository: TugasRepository // Accept TugasRepository as a parameter
) {
    // Create the ViewModel using the factory
    val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(tugasRepository))
    val user by viewModel.user.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getGithubProfile("salmafthn")
        println("Fetching GitHub profile data...")
    }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile") },
                colors = TopAppBarDefaults.smallTopAppBarColors(),
                actions = {
                    Button(onClick = {
                        auth.signOut()
                        navController.navigate("login"){
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                        }

                    }) {
                        Text("Logout")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    modifier = modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    painter = rememberAsyncImagePainter(user?.avatar_url),
                    contentDescription = "User Profile Picture"
                )

                Spacer(modifier = modifier.padding(0.dp, 10.dp))

                Box(
                    modifier = Modifier
                        .padding(5.dp),
                ) {
                    user?.let {
                        Text("Username: ${it.login}")
                    } ?: Text("Loading...")
                }

                Spacer(modifier = modifier.padding(0.dp, 10.dp))

                Box(
                    modifier = Modifier
                        .padding(5.dp),
                ) {
                    user?.let {
                        Text("Name: ${it.name}")
                    }?: Text("Loading...")
                }

                Spacer(modifier = modifier.padding(0.dp, 10.dp))

                Box(
                    modifier = Modifier

                        .padding(5.dp),
                ) {
                    user?.let {
                        Text("Public Repository: ${it.public_repos}")
                    }?: Text("Loading...")
                }

                Spacer(modifier = modifier.padding(0.dp, 10.dp))

                Box(
                    modifier = Modifier

                        .padding(5.dp),
                ) {
                    user?.let {
                        Text("Followers: ${it.followers}")
                    }?: Text("Loading...")
                }

                Spacer(modifier = modifier.padding(0.dp, 10.dp))

                Box(
                    modifier = Modifier

                        .padding(5.dp),
                ) {
                    user?.let {
                        Text("Following: ${it.following}")
                    }?: Text("Loading...")
                }


            }
        }
    }
}