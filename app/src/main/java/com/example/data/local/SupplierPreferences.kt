package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.supplierDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "soshydro_supplier_prefs"
)

class SupplierPreferences(private val context: Context) {

    private object Keys {
        val LAST_STATION_PHONE = stringPreferencesKey("last_station_phone")
        val LAST_DETAILLANT_PHONE = stringPreferencesKey("last_detaillant_phone")
    }

    val lastStationPhone: Flow<String?> = context.supplierDataStore.data.map { prefs ->
        prefs[Keys.LAST_STATION_PHONE]?.takeIf { it.isNotBlank() }
    }

    val lastDetaillantPhone: Flow<String?> = context.supplierDataStore.data.map { prefs ->
        prefs[Keys.LAST_DETAILLANT_PHONE]?.takeIf { it.isNotBlank() }
    }

    suspend fun saveLastStationPhone(phone: String) {
        val cleaned = phone.trim()
        if (cleaned.isNotEmpty()) {
            context.supplierDataStore.edit { prefs ->
                prefs[Keys.LAST_STATION_PHONE] = cleaned
            }
        }
    }

    suspend fun saveLastDetaillantPhone(phone: String) {
        val cleaned = phone.trim()
        if (cleaned.isNotEmpty()) {
            context.supplierDataStore.edit { prefs ->
                prefs[Keys.LAST_DETAILLANT_PHONE] = cleaned
            }
        }
    }
}
