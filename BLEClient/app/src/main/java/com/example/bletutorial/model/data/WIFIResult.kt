package com.example.bletutorial.model.data

import com.example.bletutorial.model.domain.ConnectionState

data class WIFIResult(
    val connectionState: ConnectionState,
    val sensors: String,
    val img: String,
)
