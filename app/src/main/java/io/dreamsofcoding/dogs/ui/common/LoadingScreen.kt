package io.dreamsofcoding.dogs.ui.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.dreamsofcoding.dogs.R
import io.dreamsofcoding.dogs.ui.theme.DogsTheme

@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    message: String = stringResource(R.string.loading_full),
    progress: Float? = null,
    showPulsingDot: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(stringResource(R.string.loading_screen_test_tag)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (progress != null) {

                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag(stringResource(R.string.progress_indicator_test_tag)),
                    color = MaterialTheme.colorScheme.primary,
                    strokeCap = StrokeCap.Round
                )
            } else {

                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .testTag(stringResource(R.string.progress_indicator_test_tag)),
                    color = MaterialTheme.colorScheme.primary,
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedLoadingText(
                text = message,
                showPulsingDot = showPulsingDot
            )
        }
    }
}

@Composable
private fun AnimatedLoadingText(
    text: String,
    showPulsingDot: Boolean
) {
    val dotCount by rememberInfiniteTransition(label = stringResource(R.string.loading)).animateValue(
        initialValue = 0,
        targetValue = 3,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Restart
        ),
        label = stringResource(R.string.dots)
    )

    Text(
        text = if (showPulsingDot) {
            text + ".".repeat(dotCount + 1)
        } else {
            text
        },
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.testTag(stringResource(R.string.loading_message_test_tag))
    )
}

/**
 * Preview of the indeterminate loading state (pulsing dots).
 */
@MultiDeviceAndModePreview
@Composable
fun LoadingScreenIndeterminatePreview() {
    DogsTheme {
        LoadingScreen(
            message = "Loading items",
            progress = null,
            showPulsingDot = true,
            modifier = Modifier
        )
    }
}

/**
 * Preview of the determinate loading state (50% progress, no pulsing dot).
 */
@MultiDeviceAndModePreview
@Composable
fun LoadingScreenDeterminatePreview() {
    DogsTheme {
        LoadingScreen(
            message = "Uploading data",
            progress = 0.5f,
            showPulsingDot = false,
            modifier = Modifier
        )
    }
}