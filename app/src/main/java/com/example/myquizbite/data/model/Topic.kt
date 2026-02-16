package com.example.myquizbite.data.model

data class Topic(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String = "code"
)
