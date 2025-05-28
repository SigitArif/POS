package com.example.pos.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: Long = 0,
    val name: String,
    val price: Double,
    val basePrice: Double = price,
    val productCode: String? = null,
    val category: String
) : Parcelable 