package com.example.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TikBlack
import com.example.ui.theme.TikCyan
import com.example.ui.theme.TikDarkSurface
import com.example.ui.theme.TikPink
import com.example.ui.theme.TikPurple
import com.example.ui.theme.TikWhite

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit,
    onDismiss: () -> Unit = onAuthSuccess,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    val isSignUp by viewModel.isSignUp.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val username by viewModel.username.collectAsState()
    val fullName by viewModel.fullName.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TikBlack)
    ) {
        // Top Close Button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E1E2C))
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Close,
                contentDescription = "Close",
                tint = TikWhite
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        // Logo & Hero
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(TikPink, TikPurple, TikCyan))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = TikWhite,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Tik",
                color = TikCyan,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Prom",
                color = TikPink,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isSignUp) "Create your TikProm creator account" else "Sign in to share & copy viral AI prompts",
            color = Color(0xFFA0A0B5),
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Input Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(TikDarkSurface)
                .border(1.dp, Color(0xFF2B2B3D), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Tab switcher (Sign In / Sign Up)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F0F16))
                        .padding(4.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isSignUp) TikPink else Color.Transparent)
                            .clickable { if (isSignUp) viewModel.toggleMode() }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Sign In",
                            color = if (!isSignUp) TikWhite else Color(0xFF7A7A8E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSignUp) TikCyan else Color.Transparent)
                            .clickable { if (!isSignUp) viewModel.toggleMode() }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Sign Up",
                            color = if (isSignUp) Color.Black else Color(0xFF7A7A8E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Sign Up extra fields
                AnimatedVisibility(visible = isSignUp) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { viewModel.fullName.value = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TikCyan) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TikCyan,
                                unfocusedBorderColor = Color(0xFF35354A),
                                focusedTextColor = TikWhite,
                                unfocusedTextColor = TikWhite
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_fullname_input")
                        )

                        OutlinedTextField(
                            value = username,
                            onValueChange = { viewModel.username.value = it },
                            label = { Text("Username (@handle)") },
                            leadingIcon = { Text("@", color = TikPink, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TikPink,
                                unfocusedBorderColor = Color(0xFF35354A),
                                focusedTextColor = TikWhite,
                                unfocusedTextColor = TikWhite
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_username_input")
                        )
                    }
                }

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.email.value = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TikCyan) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TikCyan,
                        unfocusedBorderColor = Color(0xFF35354A),
                        focusedTextColor = TikWhite,
                        unfocusedTextColor = TikWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_input")
                )

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.password.value = it },
                    label = { Text("Password (6+ chars)") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TikPink) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility",
                                tint = Color(0xFFA5A5BA)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TikPink,
                        unfocusedBorderColor = Color(0xFF35354A),
                        focusedTextColor = TikWhite,
                        unfocusedTextColor = TikWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_input")
                )

                // Error message
                if (authState is AuthState.Error) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF3B151F))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = (authState as AuthState.Error).message,
                            color = Color(0xFFFF7B92),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Submit Button
                Button(
                    onClick = {
                        viewModel.submit {
                            Toast.makeText(context, if (isSignUp) "Welcome to TikProm!" else "Logged in successfully!", Toast.LENGTH_SHORT).show()
                            onAuthSuccess()
                        }
                    },
                    enabled = authState !is AuthState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSignUp) TikCyan else TikPink,
                        contentColor = if (isSignUp) Color.Black else TikWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_submit_button")
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(
                            color = if (isSignUp) Color.Black else TikWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Text(
                            text = if (isSignUp) "Create Account" else "Sign In",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Guest / Previewer option
        TextButton(
            onClick = {
                viewModel.continueAsGuest {
                    Toast.makeText(context, "Browsing as Guest prompter", Toast.LENGTH_SHORT).show()
                    onAuthSuccess()
                }
            },
            modifier = Modifier.testTag("auth_guest_button")
        ) {
            Text(
                text = "⚡ Continue as Guest Prompter",
                color = TikCyan,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
    }
}
}
