package com.example.credentialmanagerdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import coil.compose.AsyncImage
import com.example.credentialmanagerdemo.ui.theme.CredentialManagerDemoTheme
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var credentialManager: CredentialManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        credentialManager = CredentialManager.create(this)
        val auth = Firebase.auth
        val credManager = CredManager(credentialManager, auth)
        setContent {
            CredentialManagerDemoTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    val scope = rememberCoroutineScope()
                    if (auth.currentUser != null) {
                        UserProfile(
                            user = auth.currentUser,
                            onLogoutClicked = {
                                scope.launch {
                                    /**
                                     * This is not working as expected. It is not clearing the
                                     * credential state instead it is throwing an exception.
                                     *
                                     * Exception:
                                     * java.lang.NullPointerException: Parameter specified as non-null is null: method androidx.credentials.CredentialProviderFrameworkImpl$onClearCredential$outcome$1.onResult, parameter response
                                     *
                                     *  val request = ClearCredentialStateRequest()
                                     *  credentialManager.clearCredentialState(request = request)
                                     */
                                    auth.signOut()
                                }
                            },
                        )
                    } else {
                        Login(
                            onLoginClicked = {
                                scope.launch {
                                    credManager.login(context = this@MainActivity)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Login(
    onLoginClicked: () -> Unit = {}
) {
    Button(
        onClick = onLoginClicked,
        modifier = Modifier.size(120.dp, 60.dp)
    ) {
        Text(text = "Login")
    }
}

@Composable
fun UserProfile(
    user: FirebaseUser?,
    onLogoutClicked: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = user?.photoUrl,
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )
        Text(text = "Email: ${user?.email}", color = MaterialTheme.colorScheme.onBackground)
        Text(text = "Name: ${user?.displayName}", color = MaterialTheme.colorScheme.onBackground)
        Text(text = "Uid: ${user?.uid}", color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(50.dp))
        Button(onClick = onLogoutClicked, modifier = Modifier.size(120.dp, 60.dp)) {
            Text(text = "Logout")
        }
    }
}