package com.infomanix.getpyq.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.infomanix.getpyq.R
import com.infomanix.getpyq.ui.theme.LightCyan
import com.infomanix.getpyq.ui.theme.Teal
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesScreen(
    favourites: List<Pair<String, String>>
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Favourites",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* Handle menu */ }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier
                                .size(38.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle search */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White,
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
                .padding(bottom = 15.dp, start = 15.dp, end = 15.dp)
        ) {
            if (favourites.isEmpty()) {
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
                        text = "No documents in Favourites",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\u2022  Add documents to favourite by \n selecting ‘Favourites’ from the \n document bottomsheet",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                // List of favourites
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { clip = false }
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(favourites) { docName ->
                        DocCard(docName = docName.first, date = docName.second)
                    }
                }
            }
        }
    }
}

@Composable
fun DocCard(docName: String, date: String) {
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
                Text(text = docName, fontWeight = FontWeight.Bold)
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
fun FileOptions() {
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
        OptionRow(icon = Icons.Outlined.StarOutline, text = "Remove from favourites")
        OptionRow(icon = Icons.Outlined.Delete, text = "Delete")
    }
}

@Preview(showBackground = true)
@Composable
fun FavouritesScreenPreview_Empty() {
    FavouritesScreen(favourites = emptyList())
}

@Preview(showBackground = true)
@Composable
fun FavouritesScreenPreview_WithData() {
    FavouritesScreen(favourites = listOf("CS101 Mid Sem 2023" to "Jun 20, 2024", "MA102 End Sem 2022" to "May 12, 2023"))
}