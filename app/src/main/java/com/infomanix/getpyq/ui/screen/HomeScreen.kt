package com.infomanix.getpyq.ui.screen

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.infomanix.getpyq.R
import com.infomanix.getpyq.data.UserPreferences
import com.infomanix.getpyq.data.UserState
import com.infomanix.getpyq.ui.theme.LightCyan
import com.infomanix.getpyq.ui.theme.Teal
import com.infomanix.getpyq.ui.theme.Teal15
import com.infomanix.getpyq.ui.viewmodels.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun Home() {
    val semesterFolderNames = (1..8).map { "$it" }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var isMidsem by rememberSaveable { mutableStateOf(true) }
    var selectedBranch by rememberSaveable { mutableStateOf("CS") }
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences.getInstance(context) }
    val userName by userPreferences.userName.collectAsStateWithLifecycle(initialValue = "Guest")
    val branches = listOf("CE", "CS", "EE", "EC", "EI", "ME")



    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(bottomStart = 46.dp, bottomEnd = 46.dp))
                    .background(Teal)
            ) {
                TopAppBar(
                    modifier = Modifier
                        .padding(top = 15.dp)
                        .height(80.dp),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val painter: Painter = runCatching {
                                    painterResource(id = R.drawable.profile)
                                }.getOrElse {
                                    rememberVectorPainter(Icons.Default.AccountCircle)
                                }
                                Icon(
                                    painter = painter,
                                    contentDescription = "Profile",
                                    modifier = Modifier.size(42.dp),
                                    tint = if (painter is VectorPainter) Color.Gray else Color.Unspecified
                                )
                                Spacer(Modifier.width(0.dp))

                                Text(
                                    "GetPYQ",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 45.sp
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(top = 30.dp),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.width(1.dp))

                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = "Notifications",
                                    modifier = Modifier
                                        .size(42.dp)
                                        .padding(top = 4.dp, end = 15.dp)
                                )
                            }
                        }
                    },
                )

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 10.dp)
                ) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("Search Semester") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 16.dp)
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 18.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color.White),
                        trailingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                        },
                        singleLine = true

                    )
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = Color.White,
                        modifier = Modifier
                            .size(35.dp)
                            .padding(end = 2.dp)
                            .align(Alignment.CenterVertically)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        containerColor = Teal15
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            Spacer(modifier = Modifier.height(19.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(semesterFolderNames.size) { index ->
                    FolderItem(folderName = semesterFolderNames[index])
                }
            }
        }
    }
}


@Composable
fun FolderItem(folderName: String) {

    Card(
        shape = RoundedCornerShape(30.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .shadow(
                elevation = 15.dp,
                shape = RoundedCornerShape(30.dp),
                clip = true
            )
            .border(
                width = 1.dp,
                color = Color.Black,
                shape = RoundedCornerShape(30.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            val painter: Painter = runCatching {
                painterResource(id = R.drawable.folder)
            }.getOrElse {
                rememberVectorPainter(Icons.Default.Folder)
            }

            Icon(
                painter = painter,
                contentDescription = "Folder",
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.CenterHorizontally),
                tint = if (painter is VectorPainter) Teal else Color.Unspecified
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("1st Sem", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "CS",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Preview
@Composable
fun AppDrawerContent(
    onEditProfile: () -> Unit = {},
    onDownloads: () -> Unit = {},
    onUploads: () -> Unit = {},
    onPrivacyPolicy: () -> Unit = {},
    onRateApp: () -> Unit = {},
    onShareApp: () -> Unit = {},
    onLogout: () -> Unit = {}
) {

    val painter: Painter = runCatching {
        painterResource(id = R.drawable.profile)
    }.getOrElse {
        rememberVectorPainter(Icons.Default.AccountCircle)
    }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(topEnd = 46.dp))
            .fillMaxHeight()
            .width(screenWidth * 0.6f)
            .background(Teal)
            .padding(16.dp)
    ) {
        // Profile section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {

            Icon(
                painter = painter,
                contentDescription = "Profile",
                modifier = Modifier.size(80.dp),
                tint = if (painter is VectorPainter) Color.Gray else Color.Unspecified
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text("user A", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Divider(color = Color.White.copy(alpha = 0.4f))

        Spacer(modifier = Modifier.height(12.dp))

        // Top Section: Edit Profile, Downloads
        DrawerItem("Edit Profile", Icons.Outlined.Edit, onClick = onEditProfile)
        DrawerItem("Downloads", Icons.Outlined.Download, onClick = onDownloads)
        DrawerItem("Your Uploads", Icons.Outlined.CloudUpload, onClick = onUploads)

        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = Color.White.copy(alpha = 0.4f))

        Spacer(modifier = Modifier.height(12.dp))

        // Middle Section: Privacy, Rate, Share
        DrawerItem("Privacy Policy", Icons.Outlined.Info, onClick = onPrivacyPolicy)
        DrawerItem("Rate The App", Icons.Outlined.StarOutline, onClick = onRateApp)
        DrawerItem("Share The App", Icons.Outlined.Share, onClick = onShareApp)

        Spacer(modifier = Modifier.weight(1f))
        Divider(color = Color.White.copy(alpha = 0.4f))

        // Logout
        DrawerItem("Logout", Icons.Outlined.ExitToApp, onClick = onLogout)
    }
}

@Composable
fun DrawerItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color.White
        )
    }
}


@Composable
fun CustomDropdown(
    label: String,
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(selectedItem) }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    Box(
        modifier = Modifier.width(screenWidth * 0.6f),
        contentAlignment = Alignment.TopCenter,
    )

    {
        Button(
            onClick = { expanded = !expanded },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Teal),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = selected,
                color = Color.White,
                modifier = Modifier.weight(1f),
                fontSize = 18.sp
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "$label Dropdown Arrow",
                tint = Color.White
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(Teal)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    onClick = {
                        selected = item
                        onItemSelected(item)
                        expanded = false
                    }
                )
                Divider(color = Color.Black, thickness = 0.5.dp)
            }
        }
    }
}

@Preview
@Composable
fun BranchDropdown() {
    CustomDropdown(
        label = "Branch",
        items = listOf("CSE", "ECE", "EE", "EI", "ME", "CE"),
        selectedItem = "Branch",
        onItemSelected = { selected -> println("Selected Branch: $selected") }
    )
}

@Preview
@Composable
fun SemDropdown() {
    CustomDropdown(
        label = "Semester",
        items = listOf(
            "1st sem",
            "2nd sem",
            "3rd sem",
            "4th sem",
            "5th sem",
            "6th sem",
            "7th sem",
            "8th sem"
        ),
        selectedItem = "Semester",
        onItemSelected = { selected -> println("Selected Semester: $selected") }
    )
}