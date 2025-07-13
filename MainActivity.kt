package com.reelblocker

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reelblocker.ui.theme.ReelBlockerTheme
import com.reelblocker.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReelBlockerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(this)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(activity: MainActivity, viewModel: MainViewModel = viewModel()) {
    val serviceEnabled by viewModel.serviceEnabled.collectAsState()
    val overlayPermissionGranted by viewModel.overlayPermissionGranted.collectAsState()
    val accessibilityPermissionGranted by viewModel.accessibilityPermissionGranted.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Logo and Title
        Spacer(modifier = Modifier.height(32.dp))
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(120.dp)
        )
        
        Text(
            text = "Reel Blocker",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Stay focused 👊",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.secondary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Service Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Service Status",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val statusColor = if (serviceEnabled && accessibilityPermissionGranted && overlayPermissionGranted) 
                        Color.Green else Color.Red
                    val statusText = if (serviceEnabled && accessibilityPermissionGranted && overlayPermissionGranted) 
                        "Active" else "Inactive"
                    
                    Surface(
                        modifier = Modifier.size(12.dp),
                        shape = CircleShape,
                        color = statusColor
                    ) {}
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = statusText,
                        fontSize = 16.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Toggle for Reel Blocking
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Block Reels",
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            
            Switch(
                checked = serviceEnabled,
                onCheckedChange = { 
                    if (it && (!accessibilityPermissionGranted || !overlayPermissionGranted)) {
                        // Show permissions needed
                        if (!accessibilityPermissionGranted) {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            activity.startActivity(intent)
                            Toast.makeText(
                                activity, 
                                "Please enable Reel Blocker in Accessibility Settings", 
                                Toast.LENGTH_LONG
                            ).show()
                        } else if (!overlayPermissionGranted) {
                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                            activity.startActivity(intent)
                            Toast.makeText(
                                activity, 
                                "Please grant overlay permission", 
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        viewModel.setServiceEnabled(it)
                    }
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Permission Status
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Required Permissions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                PermissionItem(
                    title = "Accessibility Service",
                    granted = accessibilityPermissionGranted,
                    onClick = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        activity.startActivity(intent)
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                PermissionItem(
                    title = "Draw Over Other Apps",
                    granted = overlayPermissionGranted,
                    onClick = {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                        activity.startActivity(intent)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Info text
        Text(
            text = "Reel Blocker helps you stay productive by blocking access to Instagram Reels",
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun PermissionItem(title: String, granted: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        
        if (granted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Granted",
                tint = Color.Green
            )
        } else {
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Enable")
            }
        }
    }
}