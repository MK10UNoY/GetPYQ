package com.infomanix.getpyq.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.infomanix.getpyq.R
import com.infomanix.getpyq.ui.theme.LightCyan
import com.infomanix.getpyq.ui.theme.Teal


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploaderScreen(
    files: List<Pair<String, String>>
) {
    val selectedTab = remember { mutableStateOf("All docs") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Handle FAB click */ },
                containerColor = Teal,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.White,
                    modifier = Modifier.size(45.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "GetPYQ",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* Handle menu */ }) {
                        Icon(
                            Icons.Default.Menu, contentDescription = "Menu", tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle search */ }) {
                        Icon(
                            Icons.Default.Search, contentDescription = "Search", tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Teal
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(LightCyan)
                .padding(25.dp)
        ) {
            // Tabs
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TabButton("All docs", selectedTab)
                TabButton("Favourites", selectedTab)
                TabButton("Trash", selectedTab)
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Files (${files.size})", fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(18.dp))

            if (files.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = (-90).dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.no_file),
                        contentDescription = "No Favourites",
                        modifier = Modifier
                            .size(200.dp)
                            .alpha(0.75f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No documents in Uploads",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            } else {
                // File list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { clip = false }
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(files) { (fileName, date) ->
                        FileCard(fileName = fileName, date = date)
                    }
                }
            }
        }
    }
}

@Composable
fun TabButton(label: String, selectedTab: MutableState<String>) {
    val isSelected = selectedTab.value == label
    val bgColor = if (isSelected) Teal else LightCyan
    val textColor = if (isSelected) Color.White else Color.Black
    val tintColor = if (isSelected) Color.White else Color.Black

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(15.dp))
            .background(bgColor)
            .border(1.dp, Color.Black, RoundedCornerShape(15.dp))
            .clickable { selectedTab.value = label }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, color = textColor, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.width(4.dp))
            if (label == "Favourites")
                Icon(
                    imageVector = Icons.Outlined.StarOutline,
                    contentDescription = "Favourite",
                    tint = tintColor,
                    modifier = Modifier.size(16.dp)
                )
            if (label == "Trash")
                Icon(
                    painter = painterResource(R.drawable.outline_delete),
                    contentDescription = "Delete",
                    tint = tintColor,
                    modifier = Modifier.size(16.dp)
                )
        }
    }
}

@Composable
fun FileCard(fileName: String, date: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false
            )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
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
                    .size(50.dp),
                tint = if (painter is VectorPainter) Teal else Color.Unspecified
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = fileName, fontWeight = FontWeight.Bold)
                Text(text = date, fontSize = 12.sp, color = Color.Gray)
            }
            IconButton(onClick = { /* overflow */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
        }
    }
}

@Preview
@Composable
fun UploaderOptions(
    onEditProfile: () -> Unit = {},
    Home: () -> Unit = {},
    onTrash: () -> Unit = {},
    onSettings: () -> Unit = {},
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

        DrawerItem("Edit Profile", Icons.Outlined.Edit, onClick = onEditProfile)
        DrawerItem("Home", Icons.Outlined.Home, onClick = Home)
        DrawerItem("Trash", Icons.Outlined.Delete, onClick = onTrash)
        DrawerItem("Settings", Icons.Outlined.Settings, onClick = onSettings)

        Spacer(modifier = Modifier.weight(1f))
        Divider(color = Color.White.copy(alpha = 0.4f))

        // Logout
        DrawerItem("Logout", Icons.Outlined.ExitToApp, onClick = onLogout)
    }
}

@Preview
@Composable
fun FileOptionsBottomSheet() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightCyan, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            val painter: Painter = runCatching {
                painterResource(id = R.drawable.folder)
            }.getOrElse {
                rememberVectorPainter(Icons.Default.Folder)
            }

            Icon(
                painter = painter,
                contentDescription = "Folder",
                modifier = Modifier.size(60.dp),
                tint = if (painter is VectorPainter) Teal else Color.Unspecified
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("MA101 Mid sem 2017", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("15th June , 2025   8:18 PM", fontSize = 14.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider(color = Color.Gray.copy(alpha = 0.3f))

        // Actions
        OptionRow(icon = Icons.Outlined.Edit, text = "Rename the file")
        OptionRow(icon = Icons.Outlined.Download, text = "Download the file")
        OptionRow(icon = Icons.Outlined.StarOutline, text = "Add to favourites")
        OptionRow(icon = Icons.Outlined.Delete, text = "Delete")
    }
}

@Composable
fun OptionRow(icon: ImageVector, text: String) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* Handle click */ }
                .padding(vertical = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(32.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = text, fontSize = 18.sp, color = Color.Black)
        }
        Divider(color = Color.Gray.copy(alpha = 0.2f))
    }
}

@Preview
@Composable
fun UploadDocumentsBottomSheet() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .padding(vertical = 16.dp, horizontal = 20.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            val painter: Painter = runCatching {
                painterResource(id = R.drawable.document)
            }.getOrElse {
                rememberVectorPainter(Icons.Default.NoteAdd)
            }

            Icon(
                painter = painter,
                contentDescription = "Folder",
                modifier = Modifier.size(40.dp),
                tint = if (painter is VectorPainter) Color(0xFF2196F3) else Color.Unspecified
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Upload Your Documents",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Choose from below options",
                    fontSize = 14.sp,
                    color = Color.Gray
                    //textAlign = Alignment.Center
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Divider()

        // Option: Scan from Camera
        UploadOptionRow(
            icon = painterResource(id = R.drawable.camera),
            subs = Icons.Default.PhotoCamera,
            label = "Scan from Camera"
        )

        Divider()

        // Option: Import from Gallery
        UploadOptionRow(
            icon = painterResource(id = R.drawable.gallery),
            subs = Icons.Default.Folder,
            label = "Import from Gallery"
        )

        Divider()
    }
}

@Composable
fun UploadOptionRow(icon: Painter, subs: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* handle click */ }
            .padding(vertical = 16.dp)
    ) {
        val symbol: Painter = runCatching { icon }.getOrElse {
            rememberVectorPainter(subs)
        }

        Icon(
            painter = symbol,
            contentDescription = null,
            modifier = Modifier.size(30.dp),
            tint = if (symbol is VectorPainter) Color(0xFF2196F3) else Color.Unspecified
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier
                .size(28.dp)
        )
    }
}

@Composable
fun RenameDialog(
    currentName: String,
    onCancel: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var folderName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onCancel,
        shape = RectangleShape,
        modifier = Modifier
            .height(280.dp)
            .padding(vertical = 8.dp),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        containerColor = Color.White,
        confirmButton = {},
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Rename",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(bottom = 11.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Text(
                    text = "Folder name",
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 4.dp, start = 10.dp)
                )

                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Name", color = Color.Black) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onCancel) {
                        Text(
                            text = "Cancel",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { onConfirm(folderName) }) {
                        Text(
                            text = "OK",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun RenameDialogPreview() {
    RenameDialog(
        currentName = "MA101 Mid sem 2017",
        onCancel = {},
        onConfirm = {}
    )
}

@Preview(showBackground = true)
@Composable
fun UploaderScreenPreview_Empty() {
    UploaderScreen(files = emptyList())
}

@Preview(showBackground = true)
@Composable
fun UploaderScreenPreview_WithData() {
    UploaderScreen(files = List(6) { "MA101 Mid sem 2017" to "Jun 20, 2024" })
}
