package com.example.carcollection.featureuser

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun UserEdit(
    userViewModel: UserViewModel,
    onBackClick: () -> Unit
) {
    val userState by userViewModel.user.collectAsState()

    // Initialize editable fields from the current user; update when userState changes
    var username by remember(userState) { mutableStateOf(userState?.username ?: "") }
    var email by remember(userState) { mutableStateOf(userState?.email ?: "") }
    var photoUrl by remember(userState) { mutableStateOf(userState?.photoUrl ?: "") }
    var password by remember { mutableStateOf("") } // don't preload password

    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // If user gets updated externally, show a brief success message
    LaunchedEffect(userState) {
        if (userState != null) {
            // If the editable fields match the userState and password empty, we assume saved
            successMessage = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }
        if (successMessage.isNotEmpty()) {
            Text(text = successMessage, color = MaterialTheme.colorScheme.primary)
        }

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = photoUrl,
            onValueChange = { photoUrl = it },
            label = { Text("Photo URL") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (leave blank to keep)") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                errorMessage = ""
                successMessage = ""
                if (username.isBlank() || email.isBlank()) {
                    errorMessage = "Username y email son obligatorios."
                    return@Button
                }

                // Call the ViewModel to perform the edit
                scope.launch {
                    try {
                        userViewModel.editUser(username.trim(), photoUrl.trim(), password, email.trim())
                        successMessage = "Solicitud de actualización enviada."
                        // Optionally navigate back immediately
                        onBackClick()
                    } catch (e: Exception) {
                        errorMessage = "Error al actualizar: ${e.message}"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar")
        }
    }
}
