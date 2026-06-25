package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val dao: AppDao) {
    val userProfileFlow: Flow<UserProfile?> = dao.observeProfile()
    val scansFlow: Flow<List<GarmentScan>> = dao.observeScans()

    suspend fun getProfile(): UserProfile? = dao.getProfile()

    suspend fun saveProfile(profile: UserProfile) {
        dao.saveProfile(profile)
    }

    suspend fun saveScan(scan: GarmentScan): Long {
        return dao.saveScan(scan)
    }

    suspend fun deleteScan(scanId: Long) {
        dao.deleteScan(scanId)
    }

    suspend fun clearScans() {
        dao.clearScans()
    }
}
