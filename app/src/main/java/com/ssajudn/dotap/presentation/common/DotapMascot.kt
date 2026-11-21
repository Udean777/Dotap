package com.ssajudn.dotap.presentation.common

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ssajudn.dotap.R
import com.ssajudn.dotap.presentation.theme.DotapTheme

val ScreenMascotSize = 160.dp
val CardMascotSize = 104.dp

/**
 * Displays the Dotap cat mascot illustration.
 */
@Composable
fun DotapMascot(
    modifier: Modifier = Modifier,
    mascotRes: Int = R.drawable.mascot_dotap_cat,
    contentDescription: String? = stringResource(R.string.mascot_cd_dotap),
) {
    Image(
        painter = painterResource(id = mascotRes),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DotapMascotPreview() {
    DotapTheme {
        DotapMascot(modifier = Modifier.size(120.dp))
    }
}
