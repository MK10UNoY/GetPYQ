package com.infomanix.getpyq.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.infomanix.getpyq.R
import com.infomanix.getpyq.ui.theme.DarkCyan
import com.infomanix.getpyq.ui.theme.LightCyan
import com.infomanix.getpyq.ui.theme.Teal
import com.infomanix.getpyq.utils.SubjectDataUtils
import okhttp3.internal.trimSubstring

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectListScreen() {
    val selectedTab = remember { mutableStateOf("All") }
    val yearOptions = listOf("2023", "2024", "2025")
    var expanded by remember { mutableStateOf(false) }
    var selectedYear by remember { mutableStateOf("Choose Year") }

    val theorySubjects = List(9) { "CS101" }
    val labSubjects = List(3) { listOf("CS111", "ME111", "CH111") }.flatten()

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(LightCyan)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { }
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(end = 10.dp)
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Department - CSE",
                            style = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "1st Semester",
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(25.dp))

                // Filter Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilterChip("All", selectedTab)
                    FilterChip("Mid Sem", selectedTab)
                    FilterChip("End Sem", selectedTab)

                    Spacer(modifier = Modifier.width(12.dp))

                    Box {
                        Row(
                            modifier = Modifier
                                .clickable { expanded = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = selectedYear,
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Dropdown Arrow",
                                tint = Color.Black,
                                modifier = Modifier.size(25.dp)
                            )
                        }

                        val screenWidth = LocalConfiguration.current.screenWidthDp.dp

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .width(screenWidth * 0.3f)
                                .background(Color.White)
                                .border(
                                    width = 1.dp,
                                    color = Color.Black,
                                )
                        ) {
                            yearOptions.forEachIndexed { index, year ->
                                androidx.compose.material3.DropdownMenuItem(
                                    text = {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = year,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedYear = year
                                        expanded = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (index < yearOptions.lastIndex) {
                                    Divider(
                                        color = Color.Black,
                                        thickness = 1.dp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(34.dp))

                // Theory Subjects
                Text(
                    "Theory Subjects",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(14.dp))

                SubjectGrid(subjects = theorySubjects)

                Spacer(modifier = Modifier.height(34.dp))

                // Labs
                Text("Labs", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold))
                Spacer(modifier = Modifier.height(14.dp))

                SubjectGrid(subjects = labSubjects)

                Spacer(modifier = Modifier.weight(1f))

                // End Box
                Text(
                    text = "\u272F Note - Please Select the year from the drop down menu",
                    style = TextStyle(color = Color.DarkGray, fontSize = 14.sp),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun FilterChip(label: String, selectedTab: MutableState<String>) {
    val isSelected = selectedTab.value == label
    Box(
        modifier = Modifier
            .background(if (isSelected) DarkCyan else Color.White, RoundedCornerShape(6.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(6.dp))
            .clickable { selectedTab.value = label }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun SubjectGrid(subjects: List<String>) {
    val rows = subjects.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Teal, RoundedCornerShape(8.dp))
                            .padding(vertical = 15.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = it,
                            color = Color.White,
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                        )
                    }
                }
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewSubjectListScreen() {
    SubjectListScreen()
}


