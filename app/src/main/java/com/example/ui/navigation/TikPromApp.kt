package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AuthPreferences
import com.example.data.remote.SupabaseClient
import com.example.data.remote.TikTokExtractorService
import com.example.data.repository.TikPromRepository
import com.example.ui.components.TikPromBottomNav
import com.example.ui.components.TikPromNavDestination
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.auth.AuthViewModel
import com.example.ui.screens.feed.FeedScreen
import com.example.ui.screens.feed.FeedViewModel
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.profile.ProfileViewModel
import com.example.ui.screens.upload.UploadScreen
import com.example.ui.screens.upload.UploadViewModel
import com.example.ui.theme.TikBlack

@Composable
fun TikPromApp() {
    val context = LocalContext.current

    // Initialize dependencies
    val authPrefs = remember { AuthPreferences(context) }
    val supabaseClient = remember { SupabaseClient() }
    val extractorService = remember { TikTokExtractorService() }
    val repository = remember {
        TikPromRepository(
            supabaseClient = supabaseClient,
            extractorService = extractorService,
            authPreferences = authPrefs
        )
    }

    // ViewModels
    val feedViewModel = remember { FeedViewModel(repository) }
    val uploadViewModel = remember { UploadViewModel(repository) }
    val profileViewModel = remember { ProfileViewModel(repository) }
    val authViewModel = remember { AuthViewModel(repository) }

    var currentDestination by remember { mutableStateOf(TikPromNavDestination.FEED) }
    var isAuthScreenVisible by remember { mutableStateOf(false) }

    fun openAuth(isSignUp: Boolean = false) {
        authViewModel.setSignUpMode(isSignUp)
        isAuthScreenVisible = true
    }

    val currentUser by repository.currentUser.collectAsState()

    if (isAuthScreenVisible) {
        AuthScreen(
            viewModel = authViewModel,
            onAuthSuccess = {
                isAuthScreenVisible = false
                currentDestination = TikPromNavDestination.FEED
                feedViewModel.loadFeed()
            },
            onDismiss = {
                isAuthScreenVisible = false
            }
        )
    } else {
        Scaffold(
            bottomBar = {
                TikPromBottomNav(
                    currentDestination = currentDestination,
                    onNavigate = { dest ->
                        currentDestination = dest
                    }
                )
            },
            containerColor = TikBlack
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TikBlack)
            ) {
                when (currentDestination) {
                    TikPromNavDestination.FEED -> {
                        FeedScreen(
                            viewModel = feedViewModel,
                            onNavigateToUpload = {
                                currentDestination = TikPromNavDestination.UPLOAD
                            },
                            onNavigateToAuth = { isSignUp ->
                                openAuth(isSignUp)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    TikPromNavDestination.UPLOAD -> {
                        UploadScreen(
                            viewModel = uploadViewModel,
                            onUploadSuccess = {
                                currentDestination = TikPromNavDestination.FEED
                                feedViewModel.loadFeed()
                            },
                            onNavigateToAuth = { isSignUp ->
                                openAuth(isSignUp)
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = paddingValues.calculateBottomPadding())
                        )
                    }

                    TikPromNavDestination.PROFILE -> {
                        ProfileScreen(
                            viewModel = profileViewModel,
                            onNavigateToAuth = { isSignUp ->
                                openAuth(isSignUp)
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = paddingValues.calculateBottomPadding())
                        )
                    }
                }
            }
        }
    }
}
