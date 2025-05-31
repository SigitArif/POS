package com.example.pos.util

import kotlin.random.Random

object StringUtils {
    private const val ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    
    fun generateRandomAlphanumeric(length: Int = 6): String {
        return (1..length)
            .map { ALPHANUMERIC_CHARS[Random.nextInt(0, ALPHANUMERIC_CHARS.length)] }
            .joinToString("")
    }
} 