package com.example.simplealarmclockapp

data class AlarmItem(
    val time: String,
    val message: String,
    val isActive: Boolean = true
)
