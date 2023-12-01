package com.example.bletutorial.model.data

import com.example.bletutorial.model.domain.ConnectionState

data class BLEResult(
    val connectionState: ConnectionState,
    val sensors: String
)
