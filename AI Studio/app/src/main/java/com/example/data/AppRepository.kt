package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(
    private val userDao: UserDao,
    private val vehicleDao: VehicleDao,
    private val chatDao: ChatDao,
    private val garageDao: GarageDao,
    private val partDao: PartDao
) {
    // Flows
    val userProfile: Flow<UserProfile?> = userDao.getUserProfile()
    val allVehicles: Flow<List<Vehicle>> = vehicleDao.getAllVehicles()
    val chatHistory: Flow<List<DiagnosticMessage>> = chatDao.getAllMessages()
    val nearbyGarages: Flow<List<Garage>> = garageDao.getNearbyGarages()
    val allParts: Flow<List<PartPrice>> = partDao.getAllParts()

    // Profile Actions
    suspend fun saveProfile(profile: UserProfile) = userDao.insertProfile(profile)
    suspend fun setSignedIn(signedIn: Boolean) = userDao.setSignedIn(signedIn)
    suspend fun updateSelectedVehicle(vehicleId: Int?) = userDao.updateSelectedVehicle(vehicleId)
    suspend fun clearProfile() = userDao.deleteProfile()

    // Vehicle Actions
    suspend fun addVehicle(vehicle: Vehicle): Int {
        val id = vehicleDao.insertVehicle(vehicle)
        return id.toInt()
    }
    suspend fun getVehicleById(id: Int) = vehicleDao.getVehicleById(id)
    suspend fun getLatestVehicle() = vehicleDao.getLatestVehicle()
    suspend fun deleteVehicle(vehicle: Vehicle) = vehicleDao.deleteVehicle(vehicle)

    // Chat Actions
    suspend fun sendMessage(message: DiagnosticMessage) = chatDao.insertMessage(message)
    suspend fun clearHistory() = chatDao.clearChatHistory()

    // Search Parts
    fun searchParts(query: String): Flow<List<PartPrice>> = partDao.searchParts(query)

    // Seeding DB
    suspend fun seedDatabase() {
        // Seed Garages if empty
        val currentGarages = nearbyGarages.firstOrNull() ?: emptyList()
        if (currentGarages.isEmpty()) {
            val garagesToSeed = listOf(
                Garage(
                    name = "Apex Auto & Transmission Care",
                    address = "142 Mechanics Way, City Center",
                    latitude = 37.7749,
                    longitude = -122.4194,
                    rating = 4.8f,
                    distanceKm = 0.8,
                    contactNo = "+1 (555) 019-2831",
                    specialty = "Engine repair, Gearbox transmission, Hybrid tunes",
                    priceTier = "$$"
                ),
                Garage(
                    name = "Brembo Brake & Alignment World",
                    address = "59 Rotary Blvd, Downtown East",
                    latitude = 37.7833,
                    longitude = -122.4167,
                    rating = 4.6f,
                    distanceKm = 1.4,
                    contactNo = "+1 (555) 014-9902",
                    specialty = "Brake pads, Wheel alignment, High spec tuning",
                    priceTier = "$$$"
                ),
                Garage(
                    name = "Tesla Tech & EV Repair Hub",
                    address = "360 Volts Station, Silicon Drive",
                    latitude = 37.7699,
                    longitude = -122.4468,
                    rating = 4.9f,
                    distanceKm = 2.1,
                    contactNo = "+1 (555) 018-4721",
                    specialty = "Electric vehicles, Battery diagnostic, Software fix",
                    priceTier = "$$$"
                ),
                Garage(
                    name = "Budget Quick-Fix Garage",
                    address = "12 Alleyway Alley, Industrial Area",
                    latitude = 37.7550,
                    longitude = -122.4350,
                    rating = 4.1f,
                    distanceKm = 3.5,
                    contactNo = "+1 (555) 011-2093",
                    specialty = "Premium oil changes, Spark plugs, Battery jumpstarts",
                    priceTier = "$"
                ),
                Garage(
                    name = "Pro Climates AC & Suspension Labs",
                    address = "901 Cool Breeze Lane, Heights District",
                    latitude = 37.7925,
                    longitude = -122.4012,
                    rating = 4.5f,
                    distanceKm = 4.2,
                    contactNo = "+1 (555) 013-8822",
                    specialty = "Air Conditioning, Shocks & Struts, Power steering",
                    priceTier = "$$"
                )
            )
            garageDao.insertGarages(garagesToSeed)
        }

        // Seed Parts if empty
        val currentParts = allParts.firstOrNull() ?: emptyList()
        if (currentParts.isEmpty()) {
            val partsToSeed = listOf(
                PartPrice(
                    partName = "Brembo Ceramic Performance Brake Pads",
                    category = "Brakes",
                    compatibleCars = "Civic, Mustang, Model 3, 3 Series, C-Class, i20",
                    lowCostShopName = "Brakes-R-Us Wholesale Store",
                    lowCostShopPrice = 39.99,
                    regularPrice = 65.00
                ),
                PartPrice(
                    partName = "Bosch Direct Engine Air Filter",
                    category = "Engine",
                    compatibleCars = "Civic, Fortuner, i20, Carens, C-Class, 3 Series",
                    lowCostShopName = "Auto Parts Depot Online",
                    lowCostShopPrice = 14.50,
                    regularPrice = 24.90
                ),
                PartPrice(
                    partName = "NGK Laser Iridium Spark Plugs (Set of 4)",
                    category = "Electrical",
                    compatibleCars = "Civic, i20, Carens, Mustang, C-Class, 3 Series",
                    lowCostShopName = "Spark Plug Direct Distributor",
                    lowCostShopPrice = 32.00,
                    regularPrice = 52.00
                ),
                PartPrice(
                    partName = "Castrol EDGE 5W-30 Full Synthetic Oil (5 Qt)",
                    category = "Engine",
                    compatibleCars = "Fortuner, Civic, Mustang, i20, Carens, 3 Series",
                    lowCostShopName = "Lubricant Mega Warehouse",
                    lowCostShopPrice = 26.80,
                    regularPrice = 45.00
                ),
                PartPrice(
                    partName = "Michelin Pilot Sport 4S High Performance Tire",
                    category = "Suspension",
                    compatibleCars = "Mustang, Model 3, Civic, 3 Series, C-Class",
                    lowCostShopName = "Tire King Discount Outlet",
                    lowCostShopPrice = 169.50,
                    regularPrice = 225.00
                ),
                PartPrice(
                    partName = "Denso HVAC Cabin AC Filter",
                    category = "Electrical",
                    compatibleCars = "Model 3, Civic, Carens, i20, C-Class, 3 Series, Fortuner",
                    lowCostShopName = "FreshAir Filter Shop",
                    lowCostShopPrice = 11.20,
                    regularPrice = 19.00
                ),
                PartPrice(
                    partName = "ACDelco 12V Gold Heavy-Duty Battery",
                    category = "Electrical",
                    compatibleCars = "Civic, i20, Carens, Mustang, Fortuner, C-Class, 3 Series",
                    lowCostShopName = "Direct Current Outlet",
                    lowCostShopPrice = 89.00,
                    regularPrice = 135.00
                )
            )
            partDao.insertParts(partsToSeed)
        }

        // Seed some default car mock selections if empty
        val currentVehicles = allVehicles.firstOrNull() ?: emptyList()
        if (currentVehicles.isEmpty()) {
            val defaultVehicle = Vehicle(
                company = "Hyundai",
                model = "i20 N-Line",
                year = "2023",
                variant = "Performance Edition (Top End)",
                companyLogo = "hyundai"
            )
            vehicleDao.insertVehicle(defaultVehicle)
        }
    }
}
