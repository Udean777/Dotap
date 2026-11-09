package com.ssajudn.dotap.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.dotap.R
import com.ssajudn.dotap.presentation.theme.DotapTheme

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

    OnboardingScreen(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onEvent: (OnboardingUiEvent) -> Unit,
) {
    Scaffold { innerPadding ->
        when (state.step) {
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

@Composable
private fun IntroBody(
    onGetStarted: () -> Unit,
    innerPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        IntroHeader()
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onGetStarted,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.onboarding_get_started))
        }
    }
}

@Composable
private fun IntroHeader() {
    Text(
        text = stringResource(R.string.onboarding_intro_title),
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.onboarding_intro_desc),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
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
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.onboarding_disclosure_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        DisclosureCard(
            title = stringResource(R.string.onboarding_disclosure_purpose_title),
            desc = stringResource(R.string.onboarding_disclosure_purpose_desc),
        )

        DisclosureCard(
            title = stringResource(R.string.onboarding_disclosure_privacy_title),
            desc = stringResource(R.string.onboarding_disclosure_privacy_desc),
        )

        DisclosureActions(
            onAgree = onAgree,
            onNotNow = onNotNow,
        )
    }
}

@Composable
private fun DisclosureActions(
    onAgree: () -> Unit,
    onNotNow: () -> Unit,
) {
    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = onAgree,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(R.string.onboarding_agree_button))
    }
    TextButton(
        onClick = onNotNow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(R.string.onboarding_not_now_button))
    }
}

@Composable
private fun DisclosureCard(
    title: String,
    desc: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IntroPreview() {
    DotapTheme {
        OnboardingScreen(state = OnboardingUiState(step = OnboardingStep.INTRO), onEvent = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun DisclosurePreview() {
    DotapTheme {
        OnboardingScreen(state = OnboardingUiState(step = OnboardingStep.DISCLOSURE), onEvent = {})
    }
}
