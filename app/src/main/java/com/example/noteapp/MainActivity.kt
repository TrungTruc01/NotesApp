package com.example.noteapp

import android.os.Bundle
import androidx.compose.foundation.clickable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.noteapp.model.Note
import com.example.noteapp.ui.theme.NoteAppTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }

            NoteAppTheme(darkTheme = isDarkMode) {
                MainScreen(isDarkMode) { isDarkMode = !isDarkMode }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(isDarkMode: Boolean, toggleDarkMode: () -> Unit) {
    var notes by remember { mutableStateOf(listOf<Note>()) }
    var editNote by remember { mutableStateOf<Note?>(null) } // Track which note is being edited

    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy") // Update date format

    var showNoteInput by remember { mutableStateOf(false) } // State to toggle note input visibility
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Ghi chú", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = toggleDarkMode) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Dark Mode"
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
            )
        },
        floatingActionButton = {
            if (!showNoteInput) {
                FloatingActionButton(
                    onClick = {
                        showNoteInput = true // Show note input when adding a new note
                        noteTitle = ""
                        noteContent = ""
                        editNote = null // Clear edit note
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    content = { Icon(Icons.Default.Add, contentDescription = "Add Note") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (showNoteInput) {
                // New screen for adding/editing notes
                NoteInputScreen(
                    title = noteTitle,
                    content = noteContent,
                    onTitleChange = { noteTitle = it },
                    onContentChange = { noteContent = it },
                    onSave = {
                        if (editNote == null) {
                            // Add new note
                            notes = notes + Note(
                                id = notes.size + 1,
                                title = noteTitle,
                                content = noteContent,
                                date = LocalDate.now()
                            )
                        } else {
                            // Update existing note
                            notes = notes.map {
                                if (it.id == editNote!!.id) {
                                    it.copy(title = noteTitle, content = noteContent)
                                } else {
                                    it
                                }
                            }
                            editNote = null // Clear edit note after saving
                        }
                        showNoteInput = false // Hide input screen after saving
                    },
                    onCancel = { showNoteInput = false } // Hide input screen on cancel
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f) // This allows LazyColumn to take the remaining space
                ) {
                    // Only show "Hôm nay" if there are notes from today
                    val todayNotes = notes.filter { it.date == today }
                    if (todayNotes.isNotEmpty()) {
                        item {
                            Text(text = "Hôm nay", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        items(todayNotes) { note ->
                            NoteCard(
                                note = note,
                                onDelete = {
                                    notes = notes.filter { it.id != note.id }
                                },
                                onEdit = {
                                    noteTitle = note.title
                                    noteContent = note.content
                                    editNote = note // Set the note being edited
                                    showNoteInput = true // Show note input screen for editing
                                },
                                isDarkMode = isDarkMode
                            )
                        }
                    }

                    // Only show "Hôm qua" if there are notes from yesterday
                    val yesterdayNotes = notes.filter { it.date == yesterday }
                    if (yesterdayNotes.isNotEmpty()) {
                        item {
                            Text(text = "Hôm qua", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        items(yesterdayNotes) { note ->
                            NoteCard(
                                note = note,
                                onDelete = {
                                    notes = notes.filter { it.id != note.id }
                                },
                                onEdit = {
                                    noteTitle = note.title
                                    noteContent = note.content
                                    editNote = note // Set the note being edited
                                    showNoteInput = true // Show note input screen for editing
                                },
                                isDarkMode = isDarkMode
                            )
                        }
                    }

                    // Show other dates
                    val otherDates = notes.filter { it.date != today && it.date != yesterday }
                    otherDates.groupBy { it.date }.forEach { (date, notesOnDate) ->
                        item {
                            Text(text = date.format(dateFormatter), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        items(notesOnDate) { note ->
                            NoteCard(
                                note = note,
                                onDelete = {
                                    notes = notes.filter { it.id != note.id }
                                },
                                onEdit = {
                                    noteTitle = note.title
                                    noteContent = note.content
                                    editNote = note // Set the note being edited
                                    showNoteInput = true // Show note input screen for editing
                                },
                                isDarkMode = isDarkMode
                            )
                        }
                    }
                }

                // Show note count at the bottom, centered
                Spacer(modifier = Modifier.height(16.dp)) // Add spacing before the note count
                Text(
                    text = "${notes.size} ghi chú",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp) // Add bottom padding for aesthetics
                )
            }
        }
    }
}

@Composable
fun NoteCard(note: Note, onDelete: () -> Unit, onEdit: () -> Unit, isDarkMode: Boolean) {
    var isExpanded by remember { mutableStateOf(false) } // State to track expanded note

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = note.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))

            // Show only part of the content initially
            Text(
                text = if (isExpanded) note.content else note.content.take(50) + if (note.content.length > 50) "..." else "",
                maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                modifier = Modifier.clickable { isExpanded = !isExpanded } // Toggle expansion on click
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Date display


            // Edit and delete buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(), // Fill width for alignment
                horizontalArrangement = Arrangement.SpaceBetween // Space between for horizontal arrangement
            ) {
                Text(text = note.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))) // Format date here
                IconButton(onClick = onEdit, colors = IconButtonDefaults.iconButtonColors(containerColor = if (isDarkMode) Color.Blue else Color(0xFF4CAF50))) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Note")
                }
                IconButton(onClick = onDelete, colors = IconButtonDefaults.iconButtonColors(containerColor = if (isDarkMode) Color(0xFFFF5722) else Color.Red)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Note")
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteInputScreen(
    title: String,
    content: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Tiêu đề") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = content,
            onValueChange = onContentChange,
            label = { Text("Nội dung") },
            modifier = Modifier.fillMaxWidth().weight(1f), // Make content field larger
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = onCancel) {
                Text("Hủy")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onSave) {
                Text("Lưu")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NoteAppTheme {
        MainScreen(isDarkMode = false, toggleDarkMode = {})
    }
}
