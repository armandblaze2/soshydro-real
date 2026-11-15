package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

val STATION_GROUPS = listOf(
    "TotalEnergies",
    "PetroGabon",
    "Engen",
    "OLA Energy",
    "Gab'Oil"
)

data class StationData(
    val id: String = "0",
    val nomStation: String = STATION_GROUPS.first(),
    val lieuDit: String = "",
    val telephone: String = "",
    val estOuvert: Boolean = true,
    val prixEssence: String = "850",
    val dispoEssence: Boolean = true,
    val prixGazoil: String = "750",
    val dispoGazoil: Boolean = true,
    val prixPetrole: String = "650",
    val dispoPetrole: Boolean = true,
    val prixGaz12kg: String = "6000",
    val dispoGaz12kg: Boolean = true,
    val prixGaz35kg: String = "14000",
    val dispoGaz35kg: Boolean = true
)

data class DetaillantData(
    val id: String = "0",
    val nomBoutique: String = "",
    val nomGerant: String = "",
    val quartier: String = "",
    val telephone: String = "",
    val prix6kg: String = "5000",
    val prix12kg: String = "18000",
    val enStock: Boolean = true
)

data class GpsCaptureResult(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float
)

@JsonClass(generateAdapter = true)
data class AuthRequest(
    val action: String,
    val telephone: String
)

@JsonClass(generateAdapter = true)
data class UpdateStationRequest(
    val action: String = "mettre_a_jour_station",
    @Json(name = "station_id") val stationId: String,
    @Json(name = "nom_station") val nomStation: String,
    @Json(name = "lieu_dit") val lieuDit: String,
    val telephone: String,
    val latitude: String,
    val longitude: String,
    @Json(name = "est_ouvert") val estOuvert: String,
    @Json(name = "prix_essence") val prixEssence: String,
    @Json(name = "dispo_essence") val dispoEssence: String,
    @Json(name = "prix_gazoil") val prixGazoil: String,
    @Json(name = "dispo_gazoil") val dispoGazoil: String,
    @Json(name = "prix_petrole") val prixPetrole: String,
    @Json(name = "dispo_petrole") val dispoPetrole: String,
    @Json(name = "prix_gaz_12kg") val prixGaz12kg: String,
    @Json(name = "dispo_gaz_12kg") val dispoGaz12kg: String,
    @Json(name = "prix_gaz_35kg") val prixGaz35kg: String,
    @Json(name = "dispo_gaz_35kg") val dispoGaz35kg: String
)

@JsonClass(generateAdapter = true)
data class UpdateDetaillantRequest(
    val action: String = "maj_detaillant_gaz",
    @Json(name = "detaillant_id") val detaillantId: String,
    @Json(name = "nom_boutique") val nomBoutique: String,
    @Json(name = "nom_gerant") val nomGerant: String,
    val quartier: String,
    val telephone: String,
    val latitude: String,
    val longitude: String,
    @Json(name = "prix_6kg") val prix6kg: String,
    @Json(name = "prix_12kg") val prix12kg: String,
    @Json(name = "en_stock") val enStock: String
)

sealed class AuthLookupResult<out T> {
    data class Found<T>(val data: T) : AuthLookupResult<T>()
    data object NotFound : AuthLookupResult<Nothing>()
}

sealed class NetworkResult<out T> {
    data class Success<T>(val value: T) : NetworkResult<T>()
    data class Error(val message: String) : NetworkResult<Nothing>()
}
