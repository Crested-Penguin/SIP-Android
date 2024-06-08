package com.crestedpenguin.sip.screens

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.crestedpenguin.sip.SipScreen
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "LoginScreen"

@Composable
fun LoginScreen(navController: NavController, auth: FirebaseAuth) {
    val context = LocalContext.current
    val signInLauncher = rememberLauncherForActivityResult(
        contract = FirebaseAuthUIActivityResultContract()
    ) { res ->
        onSignInResult(res, navController)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val providers = arrayListOf(
                    AuthUI.IdpConfig.GoogleBuilder().build()
                )
                val signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build()
                signInLauncher.launch(signInIntent)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFFEF6)
            ),
            modifier = Modifier
                .border(1.dp, Color.Black)
                .width(280.dp)
                .height(48.dp)
        ) {
            Text("Sign in with Google", color = Color.Black)
        }
    }
}

private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult, navController: NavController) {
    val response = result.idpResponse
    if (result.resultCode == Activity.RESULT_OK) {
        // Successfully signed in
        val user = FirebaseAuth.getInstance().currentUser
        Log.d(TAG, "Sign-in successful: ${user?.email}")
        // 로그인 성공 시 추가 정보 입력 화면으로 이동
        navController.navigate(SipScreen.AdditionalInfo.route) {
            popUpTo(SipScreen.Login.route) { inclusive = true }
        }
    } else {
        // Sign in failed. If response is null the user canceled the
        // sign-in flow using the back button. Otherwise check
        // response.getError().getErrorCode() and handle the error.
        // ...
        val errorMessage = response?.error?.localizedMessage ?: "Unknown error"
        Log.e(TAG, "Sign-in failed: $errorMessage, ErrorCode: ${response?.error?.errorCode}")
        Toast.makeText(navController.context, "Sign in failed: $errorMessage", Toast.LENGTH_LONG).show()
    }
}
