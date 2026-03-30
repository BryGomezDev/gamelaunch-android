package com.gamelaunch.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeepLRequest(
    @Json(name = "text") val text: List<String>,
    @Json(name = "target_lang") val targetLang: String
)

@JsonClass(generateAdapter = true)
data class DeepLResponse(
    @Json(name = "translations") val translations: List<DeepLTranslation>
)

@JsonClass(generateAdapter = true)
data class DeepLTranslation(
    @Json(name = "text") val text: String,
    @Json(name = "detected_source_language") val detectedSourceLanguage: String
)
