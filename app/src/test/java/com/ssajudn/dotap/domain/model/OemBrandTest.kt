package com.ssajudn.dotap.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class OemBrandTest {

    @Test
    fun fromManufacturer_detectsXiaomiAndSubBrands() {
        assertEquals(OemBrand.XIAOMI, OemBrand.fromManufacturer("Xiaomi"))
        assertEquals(OemBrand.XIAOMI, OemBrand.fromManufacturer("Redmi"))
        assertEquals(OemBrand.XIAOMI, OemBrand.fromManufacturer("POCO"))
    }

    @Test
    fun fromManufacturer_detectsSamsung() {
        assertEquals(OemBrand.SAMSUNG, OemBrand.fromManufacturer("samsung"))
        assertEquals(OemBrand.SAMSUNG, OemBrand.fromManufacturer("SAMSUNG"))
    }

    @Test
    fun fromManufacturer_detectsOppoAndSubBrands() {
        assertEquals(OemBrand.OPPO, OemBrand.fromManufacturer("OPPO"))
        assertEquals(OemBrand.OPPO, OemBrand.fromManufacturer("Realme"))
        assertEquals(OemBrand.OPPO, OemBrand.fromManufacturer("OnePlus"))
    }

    @Test
    fun fromManufacturer_detectsVivoAndSubBrands() {
        assertEquals(OemBrand.VIVO, OemBrand.fromManufacturer("vivo"))
        assertEquals(OemBrand.VIVO, OemBrand.fromManufacturer("iQOO"))
    }

    @Test
    fun fromManufacturer_detectsHuaweiAndSubBrands() {
        assertEquals(OemBrand.HUAWEI, OemBrand.fromManufacturer("HUAWEI"))
        assertEquals(OemBrand.HUAWEI, OemBrand.fromManufacturer("Honor"))
    }

    @Test
    fun fromManufacturer_fallsBackToOtherForGoogleOrUnknown() {
        assertEquals(OemBrand.OTHER, OemBrand.fromManufacturer("Google"))
        assertEquals(OemBrand.OTHER, OemBrand.fromManufacturer("Motorola"))
        assertEquals(OemBrand.OTHER, OemBrand.fromManufacturer(""))
    }

    @Test
    fun webHelpUrl_returnsCorrectUrls() {
        assertEquals("https://dontkillmyapp.com/xiaomi", OemBrand.XIAOMI.webHelpUrl)
        assertEquals("https://dontkillmyapp.com/samsung", OemBrand.SAMSUNG.webHelpUrl)
        assertEquals("https://dontkillmyapp.com/oppo", OemBrand.OPPO.webHelpUrl)
        assertEquals("https://dontkillmyapp.com/vivo", OemBrand.VIVO.webHelpUrl)
        assertEquals("https://dontkillmyapp.com/huawei", OemBrand.HUAWEI.webHelpUrl)
        assertEquals("https://dontkillmyapp.com", OemBrand.OTHER.webHelpUrl)
    }
}
