package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.security.AdminSecurityManager
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CrimsonNeon
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import kotlinx.coroutines.delay

@Composable
fun SecretAdminDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lockoutSeconds by remember { mutableIntStateOf(AdminSecurityManager.getRemainingLockoutSeconds()) }

    // Countdown timer for brute-force lockout
    LaunchedEffect(lockoutSeconds) {
        if (lockoutSeconds > 0) {
            delay(1000)
            lockoutSeconds = AdminSecurityManager.getRemainingLockoutSeconds()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceDark,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("secret_admin_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Secret RS Hacker Emblem
                RsHackerEmblem(size = 56.dp)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Robiul • Root Gateway",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Enter administrative authorization key to proceed",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (lockoutSeconds > 0) {
                    // Lockout Warning
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CrimsonNeon.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = CrimsonNeon,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Too many failed attempts. Locked for $lockoutSeconds s",
                                color = CrimsonNeon,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Password Field
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        errorMessage = null
                    },
                    label = { Text("Security Key") },
                    singleLine = true,
                    enabled = lockoutSeconds == 0,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (passwordInput.isNotBlank() && lockoutSeconds == 0) {
                                val success = AdminSecurityManager.authenticate(passwordInput.trim())
                                if (success) {
                                    onSuccess()
                                } else {
                                    val remaining = AdminSecurityManager.getRemainingAttempts()
                                    lockoutSeconds = AdminSecurityManager.getRemainingLockoutSeconds()
                                    errorMessage = if (lockoutSeconds > 0) {
                                        "Account locked out for $lockoutSeconds seconds"
                                    } else {
                                        "Invalid key. $remaining attempts remaining"
                                    }
                                }
                            }
                        }
                    ),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide key" else "Show key",
                                tint = TextMuted
                            )
                        }
                    },
                    isError = errorMessage != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VioletAccent,
                        unfocusedBorderColor = Color(0xFF2C3246),
                        focusedLabelColor = VioletAccent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = VioletAccent,
                        errorBorderColor = CrimsonNeon,
                        errorLabelColor = CrimsonNeon
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_password_input")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = CrimsonNeon,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("admin_dialog_cancel_btn")
                    ) {
                        Text(text = "Cancel", color = TextSecondary)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (passwordInput.isNotBlank() && lockoutSeconds == 0) {
                                val success = AdminSecurityManager.authenticate(passwordInput.trim())
                                if (success) {
                                    onSuccess()
                                } else {
                                    val remaining = AdminSecurityManager.getRemainingAttempts()
                                    lockoutSeconds = AdminSecurityManager.getRemainingLockoutSeconds()
                                    errorMessage = if (lockoutSeconds > 0) {
                                        "Account locked out for $lockoutSeconds seconds"
                                    } else {
                                        "Invalid key. $remaining attempts remaining"
                                    }
                                }
                            }
                        },
                        enabled = passwordInput.isNotBlank() && lockoutSeconds == 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VioletAccent,
                            disabledContainerColor = VioletAccent.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("admin_dialog_unlock_btn")
                    ) {
                        Text(text = "Authenticate", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
