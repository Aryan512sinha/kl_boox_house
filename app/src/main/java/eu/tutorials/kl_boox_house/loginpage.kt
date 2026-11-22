package eu.tutorials.kl_boox_house

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginPage(navigationToMainScreen: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    val navy = Color(0xFF111827)
    val gold = Color(0xFFCDA45D)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to navy,
                    0.46f to Color(0xFF1A263A),
                    1f to Color(0xFFF1F3F7)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(92.dp),
                shape = RoundedCornerShape(24.dp),
                color = gold,
                shadowElevation = 18.dp
            ) {
                Image(
                    painter = painterResource(id = R.drawable.library4),
                    contentDescription = "KL Boox House",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                color = Color.White,
                shadowElevation = 24.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 30.dp)
                ) {
                    Text(
                        text = "WELCOME BACK",
                        color = gold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Library Administration",
                        color = navy,
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sign in to manage seats and students",
                        color = Color(0xFF7A8190),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; showError = false },
                        leadingIcon = {
                            Icon(Icons.Outlined.Person, contentDescription = null, tint = Color(0xFF8A93A5))
                        },
                        label = { Text("Username") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = gold,
                            unfocusedBorderColor = Color(0xFFDDE1EA),
                            focusedContainerColor = Color(0xFFFBFCFE),
                            unfocusedContainerColor = Color(0xFFFBFCFE),
                            cursorColor = gold
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; showError = false },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = Color(0xFF8A93A5))
                        },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                Text(if (passwordVisible) "Hide" else "Show", color = gold, fontWeight = FontWeight.Bold)
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = gold,
                            unfocusedBorderColor = Color(0xFFDDE1EA),
                            focusedContainerColor = Color(0xFFFBFCFE),
                            unfocusedContainerColor = Color(0xFFFBFCFE),
                            cursorColor = gold
                        )
                    )

                    if (showError) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFDECEC)
                        ) {
                            Text(
                                text = "Incorrect username or password",
                                color = Color(0xFFB42318),
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (username == "admin" && password == "1234") {
                                navigationToMainScreen()
                            } else {
                                showError = true
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = navy),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = "Sign in",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("KL", color = gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("  BOOX HOUSE", color = Color.White.copy(alpha = 0.72f), fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 1.sp)
            }
        }
    }
}
