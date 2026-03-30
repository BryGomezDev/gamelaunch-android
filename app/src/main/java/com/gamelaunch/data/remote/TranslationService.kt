package com.gamelaunch.data.remote

import com.gamelaunch.BuildConfig
import com.gamelaunch.data.remote.dto.DeepLRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationService @Inject constructor(
    private val deepLApi: DeepLApi
) {
    val isConfigured: Boolean get() = BuildConfig.DEEPL_API_KEY.isNotBlank()

    suspend fun translateToSpanish(text: String): String? {
        if (!isConfigured) return null
        val response = deepLApi.translate(
            DeepLRequest(text = listOf(text), targetLang = "ES")
        )
        return response.translations.firstOrNull()?.text
    }
}
