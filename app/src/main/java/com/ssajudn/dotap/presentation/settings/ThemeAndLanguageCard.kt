package com.ssajudn.dotap.presentation.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssajudn.dotap.R
import com.ssajudn.dotap.domain.model.AppLanguage
import com.ssajudn.dotap.domain.model.ThemeMode
import com.ssajudn.dotap.presentation.common.DotapCard
import com.ssajudn.dotap.presentation.common.DotapDivider

private val THEME_OPTIONS = listOf(
    ThemeMode.SYSTEM to R.string.theme_system,
    ThemeMode.LIGHT to R.string.theme_light,
    ThemeMode.DARK to R.string.theme_dark,
)

private val LANGUAGE_OPTIONS = listOf(
    AppLanguage.SYSTEM to R.string.language_system,
    AppLanguage.ENGLISH to R.string.language_english,
    AppLanguage.INDONESIAN to R.string.language_indonesian,
)

@Composable
internal fun ThemeAndLanguageCard(
    themeMode: ThemeMode,
    appLanguage: AppLanguage,
    onEvent: (SettingsUiEvent) -> Unit,
) {
    DotapCard {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = stringResource(R.string.settings_theme_label), style = MaterialTheme.typography.titleSmall)
            OptionChipRow(
                options = THEME_OPTIONS,
                selected = themeMode,
                onSelected = { onEvent(SettingsUiEvent.ThemeModeSelected(it)) },
            )
            DotapDivider()
            Text(text = stringResource(R.string.settings_language_label), style = MaterialTheme.typography.titleSmall)
            OptionChipRow(
                options = LANGUAGE_OPTIONS,
                selected = appLanguage,
                onSelected = { lang -> onEvent(SettingsUiEvent.LanguageSelected(lang)) },
            )
        }
    }
}

@Composable
private fun <T> OptionChipRow(
    options: List<Pair<T, Int>>,
    selected: T,
    onSelected: (T) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (value, labelRes) ->
            FilterChip(
                selected = value == selected,
                onClick = { onSelected(value) },
                label = { Text(text = stringResource(labelRes), maxLines = 1) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
