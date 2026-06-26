package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profiles WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Query("UPDATE user_profiles SET selectedVehicleId = :vehicleId WHERE id = 1")
    suspend fun updateSelectedVehicle(vehicleId: Int?)

    @Query("UPDATE user_profiles SET isSignedIn = :signedIn WHERE id = 1")
    suspend fun setSignedIn(signedIn: Boolean)

    @Query("DELETE FROM user_profiles WHERE id = 1")
    suspend fun deleteProfile()
}

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY id DESC")
    fun getAllVehicles(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getVehicleById(id: Int): Vehicle?

    @Query("SELECT * FROM vehicles ORDER BY id DESC LIMIT 1")
    suspend fun getLatestVehicle(): Vehicle?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle): Long

    @Delete
    suspend fun deleteVehicle(vehicle: Vehicle)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM diagnostic_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<DiagnosticMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: DiagnosticMessage)

    @Query("DELETE FROM diagnostic_messages")
    suspend fun clearChatHistory()
}

@Dao
interface GarageDao {
    @Query("SELECT * FROM garages ORDER BY distanceKm ASC")
    fun getNearbyGarages(): Flow<List<Garage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGarages(garages: List<Garage>)
}

@Dao
interface PartDao {
    @Query("SELECT * FROM part_prices ORDER BY partName ASC")
    fun getAllParts(): Flow<List<PartPrice>>

    @Query("SELECT * FROM part_prices WHERE partName LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchParts(query: String): Flow<List<PartPrice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParts(parts: List<PartPrice>)
}
