package com.ssajudn.dotap.domain.port

interface CustomDotImageStorage {
    suspend fun saveFromUri(uriString: String): String?
}
