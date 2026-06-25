package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Singleton row
    val heightCm: Float = 170f,
    val chestCm: Float = 95f,
    val waistCm: Float = 80f,
    val hipsCm: Float = 95f,
    val shoulderCm: Float = 42f,
    val inseamCm: Float = 75f
) {
    fun isValid(): Boolean {
        return heightCm in 100f..250f &&
               chestCm in 40f..200f &&
               waistCm in 30f..200f &&
               hipsCm in 40f..200f &&
               shoulderCm in 25f..70f &&
               inseamCm in 40f..130f
    }
}

@Entity(tableName = "garment_scan")
data class GarmentScan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imagePath: String,
    val name: String,
    val type: String, // "Jacket", "T-Shirt", "Dress", "Jeans", etc.
    val colorHex: String,
    val fitType: String, // "Slim", "Standard", "Oversized"
    val fittingAdvice: String,
    val timestamp: Long = System.currentTimeMillis()
)
