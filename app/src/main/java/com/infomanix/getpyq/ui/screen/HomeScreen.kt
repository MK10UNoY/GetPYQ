package com.infomanix.getpyq.ui.screen

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.infomanix.getpyq.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navController: androidx.navigation.NavController) {
    val semesterFolderNames = (1..8).map { "$it" }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var isMidsem by rememberSaveable { mutableStateOf(true) }

    var selectedBranch by rememberSaveable { mutableStateOf("CS") }
    // Background Animation
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val color1 by infiniteTransition.animateColor(
        initialValue = Color(0xFF1565C0),
        targetValue = Color(0xFF42A5F5),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    val color2 by infiniteTransition.animateColor(
        initialValue = Color(0xFF42A5F5),
        targetValue = Color(0xFF81D4FA),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(color1, color2)))
            .padding(
                top = 0.dp, // Custom top padding
                bottom = WindowInsets.navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding() // Only for nav bar
            )
    ) {
        TopAppBar(
            title = { Text("GetPYQ") },
            navigationIcon = {
                IconButton(onClick = {
                    scope.launch {
                        if (drawerState.isOpen) {
                            drawerState.close()
                        } else {
                            drawerState.open()
                        }
                    }
                }) {
                    Icon(
                        imageVector = if (drawerState.isOpen) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Menu,
                        contentDescription = if (drawerState.isOpen) "Close Drawer" else "Open Drawer"
                    )
                }
            },
            actions = {
                Row(
                    modifier = Modifier.padding(16.dp, 0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    val branches = listOf("CS", "EC", "EE", "ME", "CE", "EI")

                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(1.dp)
                    ) {
                        TextButton(
                            onClick = { expanded = true },
                            colors = ButtonDefaults.textButtonColors(Color.LightGray),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(selectedBranch, color = Color.Blue)
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Select Branch",
                                tint = Color.Blue
                            )
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            branches.forEach { branch ->
                                DropdownMenuItem(
                                    text = { Text(branch, color = Color.Black) },
                                    onClick = {
                                        selectedBranch = branch
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Switch(
                        checked = isMidsem,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF006400), // Midsem Thumb
                            checkedTrackColor = Color(0xFFB2DFDB), // Midsem Track (Light Green)
                            checkedIconColor = Color.White, // Midsem Icon
                            checkedBorderColor = Color(0xFF1E88E5),

                            uncheckedThumbColor = Color.Red, // Endsem Thumb
                            uncheckedTrackColor = Color(0xFFFFCDD2), // Endsem Track (Light Red)
                            uncheckedIconColor = Color.White, // Endsem Icon
                            uncheckedBorderColor = Color(0xFF1E88E5)
                        ),
                        onCheckedChange = { isMidsem = it },
                        thumbContent = {
                            Text(
                                text = if (isMidsem) "M" else "E",
                                color = Color.White, // Text color
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(4.dp, 0.dp)
                            )
                        }
                    )
                }
            }
        )
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent2(
                    onItemClick = { scope.launch { drawerState.close() } },
                    navController = navController
                )
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp, 10.dp, 8.dp, 2.dp)
            ) {
                Column {
                    val selectedBranchState = remember { mutableStateOf("CS") }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(0.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(semesterFolderNames) { semester ->
                            FolderItem(
                                semester,
                                navController,
                                isMidsem,
                                selectedBranch = selectedBranch
                            )
                        }
                    }
                }
                // Floating Action Button
                FloatingActionButton(
                    onClick = { navController.navigate("camera") },
                    containerColor = Color(0xFF42A5F5),
                    contentColor = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp)
                ) {
                    Icon(painterResource(R.drawable.ic_scan), contentDescription = "Scan")
                }
                FloatingActionButton(
                    onClick = { navController.navigate("pdfs") },
                    containerColor = Color(0xFF2E5500),
                    contentColor = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp,100.dp)
                ) {
                    Icon(Icons.Default.Update, contentDescription = "Scan")
                }
            }
        }
    }
}

@Composable
fun FolderItem(
    semester: String,
    navController: androidx.navigation.NavController,
    isMidsem: Boolean,
    selectedBranch: String,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = {
                navController.navigate("subjects/$selectedBranch/$semester/$isMidsem")
            }),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp, 0.dp, 0.dp, 0.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_folder),
                contentDescription = "Folder",
                modifier = Modifier.size(50.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Sem $semester", fontSize = 18.sp, color = Color.Black)
        }
    }
}

@Composable
fun DrawerContent2(onItemClick: () -> Unit, navController: NavController) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val topBarHeight = 0.dp
    val sidePadding = 4.dp
    var clickCount by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(Color(0xFFFFFFFF)) // Dark background
            .padding(top = topBarHeight, start = sidePadding, end = sidePadding)
            .width(screenWidth * 0.7f)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Profile Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 0.dp, 0.dp, 0.dp),
            horizontalAlignment = Alignment.Start

        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile",
                modifier = Modifier.size(64.dp),
                tint = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Replace with Username
            // Text("MRINMOY K", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            // Replace with email
            // Text("ultimatepro190@gmail.com", color = Color.Gray, fontSize = 14.sp)
            // Replace with contributions
            // Text("338.44 MB / 1.24 GB used", color = Color.Gray, fontSize = 12.sp)
            Text(
                "View Full Profile",
                color = Color(0xFF42A5F5),
                fontSize = 14.sp,
                modifier = Modifier.clickable { /* Handle Click */ }
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { /* Handle Buy Storage */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Login", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = Color.Gray)

        // Drawer Items
        val menuItems = listOf(
            "Home" to Icons.Default.Home,
            "Settings" to Icons.Default.Settings,
            "Share" to Icons.Default.Share,
            "Help Center" to Icons.Default.Build,
            "Support Us" to Icons.Default.ThumbUp,
            "About Us" to Icons.Default.Info
        )

        menuItems.forEach { (title, icon) ->
            DrawerItem(title, icon, onItemClick)
            if (title in listOf("Refer & Earn", "Support Us")) HorizontalDivider(color = Color.Gray)
        }

        Spacer(modifier = Modifier.weight(1f))

        // App Version
        var clickCount by remember { mutableStateOf(0) }
        var firstClickTime by remember { mutableStateOf<Long?>(null) }
        val coroutineScope = rememberCoroutineScope()

        Text(
            "V1.0.0",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
                .clickable {
                    val currentTime = System.currentTimeMillis()

                    if (firstClickTime == null) {
                        firstClickTime = currentTime
                        coroutineScope.launch {
                            delay(3000) // Wait 7 seconds
                            firstClickTime = null
                            clickCount = 0 // Reset after 7 seconds
                        }
                    }

                    clickCount++

                    if (clickCount == 7) {
                        clickCount = 0 // Reset counter
                        firstClickTime = null
                        navController.navigate("myL7V3") // Navigate to the secret screen
                    }
                }
        )

    }
}

@Composable
fun DrawerItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = Color.DarkGray)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = Color.DarkGray, fontSize = 20.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    val navController = rememberNavController()
    Home(navController)
}
