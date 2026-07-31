package com.kasirpro.app.util

import com.kasirpro.app.data.repository.TransactionItem
import org.json.JSONArray
import org.json.JSONObject

object TransactionItemCodec {

    fun encode(items: List<TransactionItem>): String {
        val jsonArray = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("nama", item.nama)
            obj.put("jumlah", item.jumlah)
            obj.put("harga", item.harga)
            obj.put("varian", item.varianSelected ?: "")
            obj.put("diskon", item.diskon)
            obj.put("satuan", item.satuan)
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    fun decode(raw: String?): List<TransactionItem> {
        if (raw.isNullOrBlank()) return emptyList()
        val trimmed = raw.trim()
        return try {
            if (trimmed.startsWith("[")) {
                val jsonArray = JSONArray(trimmed)
                val list = mutableListOf<TransactionItem>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.optString("id", "")
                    val nama = obj.optString("nama", "")
                    val jumlah = obj.optInt("jumlah", 1)
                    val harga = obj.optDouble("harga", 0.0)
                    val varianRaw = obj.optString("varian", "")
                    val varianSelected = if (varianRaw.isBlank() || varianRaw.equals("null", ignoreCase = true)) null else varianRaw
                    val diskon = obj.optDouble("diskon", 0.0)
                    val satuanRaw = obj.optString("satuan", "Pcs")
                    val satuan = if (satuanRaw.isBlank()) "Pcs" else satuanRaw

                    list.add(
                        TransactionItem(
                            id = id,
                            nama = nama,
                            jumlah = jumlah,
                            harga = harga,
                            varianSelected = varianSelected,
                            diskon = diskon,
                            satuan = satuan
                        )
                    )
                }
                list
            } else {
                val list = mutableListOf<TransactionItem>()
                val lines = trimmed.split(";").filter { it.isNotBlank() }
                for (line in lines) {
                    val parts = line.split(":")
                    if (parts.size >= 4) {
                        val id = parts[0]
                        val nama = parts[1]
                        val jumlah = parts[2].toIntOrNull() ?: 1
                        val harga = parts[3].toDoubleOrNull() ?: 0.0
                        val varianRaw = if (parts.size > 4) parts[4] else null
                        val varianSelected = if (varianRaw.isNullOrBlank() || varianRaw.equals("null", ignoreCase = true)) null else varianRaw
                        val diskon = if (parts.size > 5) parts[5].toDoubleOrNull() ?: 0.0 else 0.0
                        val satuanRaw = if (parts.size > 6) parts[6] else "Pcs"
                        val satuan = if (satuanRaw.isBlank()) "Pcs" else satuanRaw

                        list.add(
                            TransactionItem(
                                id = id,
                                nama = nama,
                                jumlah = jumlah,
                                harga = harga,
                                varianSelected = varianSelected,
                                diskon = diskon,
                                satuan = satuan
                            )
                        )
                    }
                }
                list
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
