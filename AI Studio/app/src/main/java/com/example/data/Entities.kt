package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val email: String,
    val avatarIdentifier: String,
    val isSignedIn: Boolean = false,
    val selectedVehicleId: Int? = null
)

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val company: String,
    val model: String,
    val year: String,
    val variant: String,
    val companyLogo: String // Name of graphic asset or local reference
)

@Entity(tableName = "diagnostic_messages")
data class DiagnosticMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user" or "assistant"
    val text: String,
    val imageUri: String? = null, // Path for multimodal diagnostic image upload
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "garages")
data class Garage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val rating: Float,
    val distanceKm: Double,
    val contactNo: String,
    val specialty: String,
    val priceTier: String // "$", "$$", "$$$"
)

@Entity(tableName = "part_prices")
data class PartPrice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val partName: String,
    val category: String, // e.g. "Brakes", "Engine", "Suspension", "Electrical"
    val compatibleCars: String, // Comma separated compatible models
    val lowCostShopName: String,
    val lowCostShopPrice: Double,
    val regularPrice: Double,
    val isAvailable: Boolean = true
)
