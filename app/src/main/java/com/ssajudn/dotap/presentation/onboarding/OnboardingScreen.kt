package com.ssajudn.dotap.presentation.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.dotap.R
import com.ssajudn.dotap.presentation.common.ExpressiveIconBadge
import com.ssajudn.dotap.presentation.theme.DotapTheme

private const val STEP_ANIM_DURATION_MS = 300

@Composable
fun OnboardingRoute(
    viewModel: OnboardingViewModel,
    onOpenAccessibilitySettings: () -> Unit,
    onNavigateToHome: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                OnboardingUiEffect.OpenAccessibilitySettings -> {
                    onOpenAccessibilitySettings()
                    onNavigateToHome()
                }

                OnboardingUiEffect.NavigateToHome -> onNavigateToHome()
            }
        }
    }
    OnboardingScreen(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onEvent: (OnboardingUiEvent) -> Unit,
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.surface) { innerPadding ->
        AnimatedContent(
            targetState = state.step,
            transitionSpec = { onboardingStepTransitionSpec() },
            label = "OnboardingStepContent",
        ) { step ->
            when (step) {
                OnboardingStep.INTRO -> IntroBody(
                    onGetStarted = { onEvent(OnboardingUiEvent.GetStartedClicked) },
                    innerPadding = innerPadding,
                )

                OnboardingStep.DISCLOSURE -> DisclosureBody(
                    onAgree = { onEvent(OnboardingUiEvent.AgreeClicked) },
                    onNotNow = { onEvent(OnboardingUiEvent.NotNowClicked) },
                    innerPadding = innerPadding,
                )
            }
        }
    }
}

private fun AnimatedContentTransitionScope<OnboardingStep>.onboardingStepTransitionSpec(): ContentTransform {
    val forward = targetState.ordinal > initialState.ordinal
    val enter = slideInHorizontally(
        initialOffsetX = { width -> if (forward) width else -width },
        animationSpec = tween(durationMillis = STEP_ANIM_DURATION_MS, easing = FastOutSlowInEasing),
    ) + fadeIn(animationSpec = tween(durationMillis = STEP_ANIM_DURATION_MS))
    val exit = slideOutHorizontally(
        targetOffsetX = { width -> if (forward) -width else width },
        animationSpec = tween(durationMillis = STEP_ANIM_DURATION_MS, easing = FastOutSlowInEasing),
    ) + fadeOut(animationSpec = tween(durationMillis = STEP_ANIM_DURATION_MS))
    return enter togetherWith exit
}

@Composable
private fun DisclosureBody(
    onAgree: () -> Unit,
    onNotNow: () -> Unit,
    innerPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.onboarding_disclosure_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        DisclosureCards()
        DisclosureActions(onAgree = onAgree, onNotNow = onNotNow)
    }
}

@Composable
private fun DisclosureCards() {
    DisclosureCard(
        title = stringResource(R.string.onboarding_disclosure_purpose_title),
        desc = stringResource(R.string.onboarding_disclosure_purpose_desc),
        iconRes = R.drawable.ic_accessibility,
    )
    DisclosureCard(
        title = stringResource(R.string.onboarding_disclosure_privacy_title),
        desc = stringResource(R.string.onboarding_disclosure_privacy_desc),
        iconRes = R.drawable.ic_shield_check,
    )
}

@Composable
private fun DisclosureActions(
    onAgree: () -> Unit,
    onNotNow: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 8.dp),
    ) {
        Button(
            onClick = onAgree,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            Text(
                text = stringResource(R.string.onboarding_agree_button),
                style = MaterialTheme.typography.labelLarge,
            )
        }
        TextButton(
            onClick = onNotNow,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = stringResource(R.string.onboarding_not_now_button),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun DisclosureCard(
    title: String,
    desc: String,
    @DrawableRes iconRes: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DisclosureCardHeader(title = title, iconRes = iconRes)
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DisclosureCardHeader(
    title: String,
    @DrawableRes iconRes: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ExpressiveIconBadge(
            iconRes = iconRes,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(36.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DisclosurePreview() {
    DotapTheme {
        OnboardingScreen(state = OnboardingUiState(step = OnboardingStep.DISCLOSURE), onEvent = {})
    }
}
