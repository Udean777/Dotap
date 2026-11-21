package com.ssajudn.dotap.presentation.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ssajudn.dotap.R
import com.ssajudn.dotap.presentation.theme.DotapTheme
import kotlinx.coroutines.launch

private data class IntroSlide(
    val imageRes: Int,
    val titleRes: Int,
    val descRes: Int,
)

private val INTRO_SLIDES = listOf(
    IntroSlide(
        R.drawable.mascot_dotap_cat,
        R.string.onboarding_intro_title,
        R.string.onboarding_intro_desc,
    ),
    IntroSlide(
        R.drawable.mascot_onboarding_gestures,
        R.string.onboarding_slide2_title,
        R.string.onboarding_slide2_desc,
    ),
    IntroSlide(
        R.drawable.mascot_onboarding_privacy,
        R.string.onboarding_slide3_title,
        R.string.onboarding_slide3_desc,
    ),
)

@Composable
internal fun IntroBody(
    onGetStarted: () -> Unit,
    innerPadding: PaddingValues,
) {
    val pagerState = rememberPagerState(pageCount = { INTRO_SLIDES.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == INTRO_SLIDES.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            IntroSlideContent(slide = INTRO_SLIDES[page])
        }
        PagerIndicator(pageCount = INTRO_SLIDES.size, currentPage = pagerState.currentPage)
        Spacer(modifier = Modifier.height(24.dp))
        IntroActionButton(
            isLastPage = isLastPage,
            onClick = {
                if (isLastPage) {
                    onGetStarted()
                } else {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
        )
    }
}

@Composable
private fun IntroSlideContent(slide: IntroSlide) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = slide.imageRes),
            contentDescription = stringResource(slide.titleRes),
            modifier = Modifier.size(200.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = stringResource(slide.titleRes),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(slide.descRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PagerIndicator(
    pageCount: Int,
    currentPage: Int,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(if (selected) 24.dp else 8.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

@Composable
private fun IntroActionButton(
    isLastPage: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
    ) {
        val labelRes = if (isLastPage) R.string.onboarding_get_started else R.string.onboarding_next
        Text(text = stringResource(labelRes), style = MaterialTheme.typography.labelLarge)
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun IntroBodyPreview() {
    DotapTheme {
        IntroBody(onGetStarted = {}, innerPadding = PaddingValues())
    }
}
