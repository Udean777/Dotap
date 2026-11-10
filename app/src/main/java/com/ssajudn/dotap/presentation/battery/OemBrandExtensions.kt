package com.ssajudn.dotap.presentation.battery

import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.OemBrand

fun OemBrand.labelResId(): Int = when (this) {
    OemBrand.XIAOMI -> R.string.oem_brand_xiaomi
    OemBrand.SAMSUNG -> R.string.oem_brand_samsung
    OemBrand.OPPO -> R.string.oem_brand_oppo
    OemBrand.VIVO -> R.string.oem_brand_vivo
    OemBrand.HUAWEI -> R.string.oem_brand_huawei
    OemBrand.OTHER -> R.string.oem_brand_other
}

fun OemBrand.stepsResId(): Int = when (this) {
    OemBrand.XIAOMI -> R.string.battery_steps_xiaomi
    OemBrand.SAMSUNG -> R.string.battery_steps_samsung
    OemBrand.OPPO -> R.string.battery_steps_oppo
    OemBrand.VIVO -> R.string.battery_steps_vivo
    OemBrand.HUAWEI -> R.string.battery_steps_huawei
    OemBrand.OTHER -> R.string.battery_steps_other
}
