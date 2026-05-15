package com.example.datamobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datamobile.ui.theme.BingwaTheme
import com.example.datamobile.utils.TokenManager
import com.example.datamobile.utils.TransactionDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BingwaTheme {
                BingwaApp()
            }
        }
    }
}

@Composable
fun BingwaApp() {
    var selectedScreen by remember { mutableStateOf(Screen.Dashboard) }
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    var tokenBalance by remember { mutableStateOf(tokenManager.getTokenBalance()) }
    var isUnlimited by remember { mutableStateOf(tokenManager.isUnlimited()) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                val items = listOf(
                    Screen.Dashboard to Pair(Icons.Default.Home, "Home"),
                    Screen.Transactions to Pair(Icons.Default.History, "History"),
                    Screen.Tokens to Pair(Icons.Default.Star, "Tokens"),
                    Screen.Settings to Pair(Icons.Default.Settings, "Settings")
                )
                items.forEach { (screen, pair) ->
                    val (icon, label) = pair
                    NavigationBarItem(
                        selected = selectedScreen == screen,
                        onClick = { selectedScreen = screen },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF6C63FF),
                            selectedTextColor = Color(0xFF6C63FF),
                            unselectedIconColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (selectedScreen) {
                Screen.Dashboard -> DashboardScreen(tokenBalance, isUnlimited)
                Screen.Transactions -> TransactionsScreen()
                Screen.Tokens -> TokensScreen(tokenManager) { newBalance, unlimited ->
                    tokenBalance = newBalance
                    isUnlimited = unlimited
                }
                Screen.Settings -> SettingsScreen()
            }
        }
    }
}

enum class Screen { Dashboard, Transactions, Tokens, Settings }

@Composable
fun DashboardScreen(tokenBalance: Int, isUnlimited: Boolean) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        Box(modifier = Modifier.fillMaxWidth().height(240.dp).background(
            Brush.linearGradient(listOf(Color(0xFF6C63FF), Color(0xFF3F3D9E))))) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(40.dp))
                Text("Token Balance", color = Color.White.copy(alpha = 0.9f), fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                if (isUnlimited) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("UNLIMITED", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(tokenBalance.toString(), color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Bold)
                    Text("Tokens Available", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(50.dp), modifier = Modifier.fillMaxWidth(0.6f).height(48.dp)) {
                    Icon(Icons.Default.Add, null, tint = Color(0xFF6C63FF))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buy Tokens", color = Color(0xFF6C63FF), fontWeight = FontWeight.Bold)
                }
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Quick Actions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            QuickActionButton("Buy Airtime", Icons.Default.Phone, { }, Modifier.weight(1f))
                            QuickActionButton("Send Money", Icons.Default.Send, { }, Modifier.weight(1f))
                            QuickActionButton("Data Bundle", Icons.Default.Wifi, { }, Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier.height(80.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(12.dp), elevation = ButtonDefaults.buttonElevation(0.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = text, tint = Color(0xFF6C63FF), modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, fontSize = 11.sp, color = Color(0xFF212121))
        }
    }
}

@Composable
fun TransactionsScreen() {
    val context = LocalContext.current
    val db = remember { TransactionDatabase.getInstance(context) }
    val transactions = remember { mutableStateOf(db.getAllTransactions()) }
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 4.dp) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Transaction History", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                Text("All token purchases and usage", fontSize = 14.sp, color = Color.Gray)
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(transactions.value) { transaction ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(
                                if (transaction.type == "PURCHASE") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)),
                                contentAlignment = Alignment.Center) {
                                Icon(if (transaction.type == "PURCHASE") Icons.Default.Add else Icons.Default.Remove,
                                    transaction.type, tint = if (transaction.type == "PURCHASE") Color(0xFF4CAF50) else Color(0xFFFF9800),
                                    modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(transaction.description, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF212121))
                                Text(transaction.date, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(if (transaction.type == "PURCHASE") "+${transaction.tokens}" else "-${transaction.tokens}",
                                fontSize = 16.sp, fontWeight = FontWeight.Bold,
                                color = if (transaction.type == "PURCHASE") Color(0xFF4CAF50) else Color(0xFFFF9800))
                            Text(transaction.status, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TokensScreen(tokenManager: TokenManager, onTokensUpdated: (Int, Boolean) -> Unit) {
    var selectedPackage by remember { mutableStateOf(0) }
    var showPaymentInfo by remember { mutableStateOf(false) }
    val packages = listOf(
        TokenPackage(50, 45, "Basic", "Great for occasional use", 7),
        TokenPackage(100, 95, "Standard", "Most popular choice", 14),
        TokenPackage(250, 225, "Premium", "Best value package", 30),
        TokenPackage(500, 475, "Ultimate", "Unlimited for 30 days", 30)
    )
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 4.dp) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Buy Tokens", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                Text("Send money to CORMAKS TECH Till Number", fontSize = 14.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium)
                Text("Payment will be detected automatically", fontSize = 12.sp, color = Color.Gray)
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(packages.size) { index ->
                val pkg = packages[index]
                Card(modifier = Modifier.fillMaxWidth().clickable { selectedPackage = index }, shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (selectedPackage == index) Color(0xFF6C63FF).copy(alpha = 0.1f) else Color.White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(pkg.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (selectedPackage == index) Color(0xFF6C63FF) else Color(0xFF212121))
                            Text(pkg.description, fontSize = 12.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("${pkg.tokens} Tokens", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                            Text("KSh ${pkg.price}", fontSize = 14.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                        }
                        RadioButton(selected = selectedPackage == index, onClick = { selectedPackage = index },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF6C63FF)))
                    }
                }
            }
            item {
                Button(onClick = { showPaymentInfo = true }, modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF)), shape = RoundedCornerShape(12.dp)) {
                    Text("Pay KSh ${packages[selectedPackage].price} to CORMAKS TECH", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    if (showPaymentInfo) {
        AlertDialog(onDismissRequest = { showPaymentInfo = false }, title = { Text("Payment Instructions") },
            text = { Text("Till Number: CORMAKS TECH\nAmount: KSh ${packages[selectedPackage].price}\nAfter payment, tokens will be added automatically.") },
            confirmButton = { TextButton(onClick = { showPaymentInfo = false }) { Text("OK") } })
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoRenewEnabled by remember { mutableStateOf(tokenManager.getAutoRenew()) }
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 4.dp) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Settings", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                Text("Customize your experience", fontSize = 14.sp, color = Color.Gray)
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column {
                        SettingsSwitchItem("Push Notifications", "Receive transaction alerts", Icons.Default.Notifications, notificationsEnabled) { notificationsEnabled = it }
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsSwitchItem("Auto-Renew Subscription", "Automatically renew tokens", Icons.Default.Autorenew, autoRenewEnabled) {
                            autoRenewEnabled = it; tokenManager.setAutoRenew(it)
                        }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Bingwa Mobile v1.0.0", fontSize = 14.sp, color = Color.Gray)
                        Text("Powered by CORMAKS TECH", fontSize = 12.sp, color = Color(0xFF6C63FF))
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
            Icon(icon, title, tint = Color(0xFF6C63FF), modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF212121))
            Text(subtitle, fontSize = 13.sp, color = Color.Gray)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF6C63FF)))
    }
}

data class TokenPackage(val tokens: Int, val price: Int, val name: String, val description: String, val days: Int)
