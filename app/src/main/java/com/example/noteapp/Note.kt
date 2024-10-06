package com.example.noteapp.model

import java.time.LocalDate

data class Note(
    val id: Int,
    val title: String,
    val content: String,
    val date: LocalDate // Ensure this property is added to track the note's creation date
)
