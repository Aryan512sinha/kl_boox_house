package eu.tutorials.kl_boox_house

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Surface

import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                myApp() // your NavHost setup
            }
        }
    }

    @Composable
    fun myApp() {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "loginPage") {
            composable(route = "loginpage") {
                LoginPage{
                    navController.navigate("mainscreen")
                }
            }
            composable(route = "mainscreen") {
                MainScreen()
            }
        }
    }
}





