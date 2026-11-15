package com.example.data.repository

import com.example.data.model.AuthLookupResult
import com.example.data.model.AuthRequest
import com.example.data.model.DetaillantData
import com.example.data.model.NetworkResult
import com.example.data.model.STATION_GROUPS
import com.example.data.model.StationData
import com.example.data.model.UpdateDetaillantRequest
import com.example.data.model.UpdateStationRequest
import com.example.data.remote.SosHydroApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class FournisseurRepository(
    private val apiService: SosHydroApiService
) {

    suspend fun verifyStationPhone(telephone: String): NetworkResult<AuthLookupResult<StationData>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.auth(
                    AuthRequest(action = "auth_station", telephone = telephone.trim())
                )
                val rawBody = response.body()?.string() ?: response.errorBody()?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext NetworkResult.Error("HTTP ${response.code()} : serveur inaccessible")
                }

                val json = JSONObject(rawBody)
                if (json.optString("status") == "found") {
                    val dataObj = json.optJSONObject("data") ?: JSONObject()
                    NetworkResult.Success(AuthLookupResult.Found(parseStationData(dataObj)))
                } else {
                    NetworkResult.Success(AuthLookupResult.NotFound)
                }
            } catch (e: Exception) {
                NetworkResult.Error(e.localizedMessage ?: "Erreur réseau")
            }
        }

    suspend fun verifyDetaillantPhone(telephone: String): NetworkResult<AuthLookupResult<DetaillantData>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.auth(
                    AuthRequest(action = "auth_detaillant", telephone = telephone.trim())
                )
                val rawBody = response.body()?.string() ?: response.errorBody()?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext NetworkResult.Error("HTTP ${response.code()} : serveur inaccessible")
                }

                val json = JSONObject(rawBody)
                if (json.optString("status") == "found") {
                    val dataObj = json.optJSONObject("data") ?: JSONObject()
                    NetworkResult.Success(AuthLookupResult.Found(parseDetaillantData(dataObj)))
                } else {
                    NetworkResult.Success(AuthLookupResult.NotFound)
                }
            } catch (e: Exception) {
                NetworkResult.Error(e.localizedMessage ?: "Erreur réseau")
            }
        }

    suspend fun submitStation(request: UpdateStationRequest): NetworkResult<String> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateStation(request)
                val rawBody = response.body()?.string() ?: response.errorBody()?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext NetworkResult.Error("HTTP ${response.code()} : erreur serveur")
                }

                val json = JSONObject(rawBody)
                if (json.optString("status") == "success") {
                    val msg = json.optString("message").ifBlank { "Station mise à jour avec succès !" }
                    NetworkResult.Success(msg)
                } else {
                    val msg = json.optString("message").ifBlank { "Erreur serveur" }
                    NetworkResult.Error(msg)
                }
            } catch (e: Exception) {
                NetworkResult.Error(e.localizedMessage ?: "Erreur réseau")
            }
        }

    suspend fun submitDetaillant(request: UpdateDetaillantRequest): NetworkResult<String> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateDetaillant(request)
                val rawBody = response.body()?.string() ?: response.errorBody()?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext NetworkResult.Error("HTTP ${response.code()} : erreur serveur")
                }

                val json = JSONObject(rawBody)
                if (json.optString("status") == "success") {
                    val msg = json.optString("message").ifBlank { "Boutique mise à jour avec succès !" }
                    NetworkResult.Success(msg)
                } else {
                    val msg = json.optString("message").ifBlank { "Erreur serveur" }
                    NetworkResult.Error(msg)
                }
            } catch (e: Exception) {
                NetworkResult.Error(e.localizedMessage ?: "Erreur réseau")
            }
        }

    private fun parseStationData(d: JSONObject): StationData {
        val rawGroup = d.optString("nom_station", "")
        val resolvedGroup = if (STATION_GROUPS.contains(rawGroup)) rawGroup else STATION_GROUPS.first()

        val gaz12Price = when {
            d.has("prix_gaz") && !d.isNull("prix_gaz") -> d.optStringVal("prix_gaz", "6000")
            d.has("prix_gaz_12kg") && !d.isNull("prix_gaz_12kg") -> d.optStringVal("prix_gaz_12kg", "6000")
            else -> "6000"
        }
        val gaz12Dispo = when {
            d.has("dispo_gaz") -> d.optBoolFlag("dispo_gaz", true)
            d.has("dispo_gaz_12kg") -> d.optBoolFlag("dispo_gaz_12kg", true)
            else -> true
        }

        return StationData(
            id = d.optStringVal("id", "0"),
            nomStation = resolvedGroup,
            lieuDit = d.optString("lieu_dit", ""),
            telephone = d.optString("telephone", ""),
            estOuvert = d.optBoolFlag("est_ouvert", true),
            prixEssence = d.optStringVal("prix_essence", "850"),
            dispoEssence = d.optBoolFlag("dispo_essence", true),
            prixGazoil = d.optStringVal("prix_gazoil", "750"),
            dispoGazoil = d.optBoolFlag("dispo_gazoil", true),
            prixPetrole = d.optStringVal("prix_petrole", "650"),
            dispoPetrole = d.optBoolFlag("dispo_petrole", true),
            prixGaz12kg = gaz12Price,
            dispoGaz12kg = gaz12Dispo,
            prixGaz35kg = d.optStringVal("prix_gaz_35kg", "14000"),
            dispoGaz35kg = d.optBoolFlag("dispo_gaz_35kg", true)
        )
    }

    private fun parseDetaillantData(d: JSONObject): DetaillantData {
        val prix6 = when {
            d.has("prix_bouteille_6kg") && !d.isNull("prix_bouteille_6kg") ->
                d.optStringVal("prix_bouteille_6kg", "5000")
            d.has("prix_6kg") && !d.isNull("prix_6kg") ->
                d.optStringVal("prix_6kg", "5000")
            else -> "5000"
        }
        val prix12 = when {
            d.has("prix_bouteille_12kg") && !d.isNull("prix_bouteille_12kg") ->
                d.optStringVal("prix_bouteille_12kg", "18000")
            d.has("prix_12kg") && !d.isNull("prix_12kg") ->
                d.optStringVal("prix_12kg", "18000")
            else -> "18000"
        }

        return DetaillantData(
            id = d.optStringVal("id", "0"),
            nomBoutique = d.optString("nom_boutique", ""),
            nomGerant = d.optString("nom_gerant", ""),
            quartier = d.optString("quartier", ""),
            telephone = d.optString("telephone", ""),
            prix6kg = prix6,
            prix12kg = prix12,
            enStock = d.optBoolFlag("en_stock", true)
        )
    }

    private fun JSONObject.optStringVal(key: String, default: String): String {
        if (!has(key) || isNull(key)) return default
        val raw = opt(key)?.toString()?.trim().orEmpty()
        return if (raw.isEmpty() || raw.equals("null", ignoreCase = true)) default else raw
    }

    private fun JSONObject.optBoolFlag(key: String, default: Boolean): Boolean {
        if (!has(key) || isNull(key)) return default
        return when (val v = opt(key)) {
            is Boolean -> v
            is Number -> v.toInt() == 1
            is String -> v == "1" || v.equals("true", ignoreCase = true)
            else -> default
        }
    }
}
