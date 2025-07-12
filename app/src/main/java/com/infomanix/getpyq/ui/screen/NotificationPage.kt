package com.infomanix.getpyq.ui.screen


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavHostController
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.infomanix.getpyq.R
import com.infomanix.getpyq.ui.theme.DarkCyan
import com.infomanix.getpyq.ui.theme.LightCyan

@Preview
@Composable
fun NotificationPage() {
    val notifications = listOf(
        "Check out HS111 Notes has been Uploaded !" to "Today",
        "Check out HS111 Notes has been Uploaded !" to "Yesterday",
        "Check out HS111 Notes has been Uploaded !" to "23/06/2025",
        "Check out HS111 Notes has been Uploaded !" to "22/06/2025",
        "Check out HS111 Notes has been Uploaded !" to "21/06/2025",
        "Check out HS111 Notes has been Uploaded !" to "20/06/2025"
    )

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(LightCyan)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { }
                    )
                    Text(
                        text = "Notifications",
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                LazyColumn {
                    items(notifications) { (title, date) ->
                        NotificationItem(message = title, date = date)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(message: String, date: String) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(65.dp)
                    .background(DarkCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val painter: Painter = runCatching {
                    painterResource(id = R.drawable.bell)
                }.getOrElse {
                    rememberVectorPainter(Icons.Outlined.Notifications)
                }
                Icon(
                    painter = painter,
                    contentDescription = "Notification Icon",
                    modifier = Modifier.size(34.dp),
                    tint = if (painter is VectorPainter) Color.Yellow else Color.Unspecified
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = message,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = date,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Divider(thickness = 2.dp, color = Color.DarkGray.copy(alpha = 0.4f))
    }
}
