package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // User Profile Queries
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun observeProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfile)

    // Garment Scan Queries
    @Query("SELECT * FROM garment_scan ORDER BY timestamp DESC")
    fun observeScans(): Flow<List<GarmentScan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveScan(scan: GarmentScan): Long

    @Query("DELETE FROM garment_scan WHERE id = :scanId")
    suspend fun deleteScan(scanId: Long)

    @Query("DELETE FROM garment_scan")
    suspend fun clearScans()
}
