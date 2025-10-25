package com.example.carcollection.presentation.user.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.carcollection.featureuser.data.UserMethods
import com.example.carcollection.presentation.navigation.NavRoutes
import com.example.carcollection.presentation.user.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginForm(
    onBackClick: () -> Unit,
    navController: NavController,
    userViewModel: UserViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        if (successMessage.isNotEmpty()) {
            Text(
                text = successMessage,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = email.isEmpty() && errorMessage.isNotEmpty()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = password.isEmpty() && errorMessage.isNotEmpty()
        )
        Button(
            onClick = {
                if (email.isEmpty() || password.isEmpty()) {
                    errorMessage = "Email y contraseña son obligatorios."
                    successMessage = ""
                } else {
                    errorMessage = ""
                    successMessage = ""
                    coroutineScope.launch {
                        val result = UserMethods().loginUser(email, password)
                        result.onSuccess {
                            successMessage = "Inicio de sesión exitoso."
                            // Refresh the ViewModel so UI StateFlows update across the app
                            userViewModel.fetchUserProfile()
                            userViewModel.fetchCarCount()
                            // Navigate back to previous screen so menu/sidebar can reflect new state
                            onBackClick()
                        }.onFailure {
                            errorMessage = it.message ?: "Error desconocido."
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar sesión")
        }

        TextButton(
            onClick = {
                // Navigate to the registration screen when this button is clicked.
                navController.navigate(NavRoutes.REGISTER)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("¿No tienes una cuenta? Regístrate")
        }
    }
}
