package io.dreamsofcoding.dogs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.dreamsofcoding.dogs.ui.common.Lottie
import io.dreamsofcoding.dogs.ui.common.MultiDeviceAndModePreview
import io.dreamsofcoding.dogs.ui.theme.DogsTheme
import kotlinx.coroutines.delay

private const val SPLASH_DELAY_MS = 3000L

@Composable
fun SplashScreen(
    navToBreedsScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }


    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = stringResource(R.string.splash_fade_label)
    )

    LaunchedEffect(Unit) {
        isVisible = true
        delay(SPLASH_DELAY_MS)
        navToBreedsScreen()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(stringResource(R.string.splash_screen_test_tag)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alpha)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag(stringResource(R.string.splash_title_test_tag))
            )

            Spacer(modifier = Modifier.height(32.dp))

            Lottie(
                rawFile = R.raw.splash,
                isPlaying = isVisible,
                iterations = Int.MAX_VALUE,
                modifier = Modifier
                    .size(300.dp)
                    .testTag(stringResource(R.string.splash_animation_test_tag))
            )

            Spacer(modifier = Modifier.height(48.dp))


            Text(
                text = stringResource(R.string.loading_splash_screen_text),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.testTag(stringResource(R.string.splash_loading_text_test_tag))
            )
        }
    }
}

@MultiDeviceAndModePreview
@Composable
fun SplashScreenMultiPreview() {
    DogsTheme {
        SplashScreen(navToBreedsScreen = {}, modifier = Modifier)
    }
}