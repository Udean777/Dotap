package com.ssajudn.dotap.domain.model

enum class OemBrand(val helpSlug: String) {
    XIAOMI("xiaomi"),
    SAMSUNG("samsung"),
    OPPO("oppo"),
    VIVO("vivo"),
    HUAWEI("huawei"),
    OTHER(""),
    ;

    val webHelpUrl: String
        get() = if (helpSlug.isNotEmpty()) {
            "https://dontkillmyapp.com/$helpSlug"
        } else {
            "https://dontkillmyapp.com"
        }

    companion object {
        private val BRAND_KEYWORDS: List<Pair<String, OemBrand>> = listOf(
            "xiaomi" to XIAOMI,
            "redmi" to XIAOMI,
            "poco" to XIAOMI,
            "samsung" to SAMSUNG,
            "oppo" to OPPO,
            "realme" to OPPO,
            "oneplus" to OPPO,
            "vivo" to VIVO,
            "iqoo" to VIVO,
            "huawei" to HUAWEI,
            "honor" to HUAWEI,
        )

        fun fromManufacturer(manufacturer: String): OemBrand {
            val name = manufacturer.lowercase()
            return BRAND_KEYWORDS.firstOrNull { (keyword, _) ->
                name.contains(keyword)
            }?.second ?: OTHER
        }
    }
}
