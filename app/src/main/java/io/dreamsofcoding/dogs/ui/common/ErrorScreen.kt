package io.dreamsofcoding.dogs.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.dreamsofcoding.dogs.R
import io.dreamsofcoding.dogs.ui.theme.DogsTheme

@Composable
fun ErrorScreen(
    modifier: Modifier = Modifier,
    error: UiError? = null,
    onRetry: (() -> Unit)? = null,
    title: String = stringResource(R.string.oops_something_went_wrong),
    icon: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = stringResource(R.string.error_icon_content_description),
            modifier = Modifier
                .size(64.dp)
                .testTag(stringResource(R.string.error_icon_test_tag)),
            tint = MaterialTheme.colorScheme.error
        )
    }
) {

    val errorRes = when (error) {
        UiError.NoNetwork -> R.string.network_exception
        UiError.InvalidBreed -> R.string.breed_not_found_exception
        UiError.ServerError -> R.string.server_exception
        else -> R.string.unknown_exception
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(stringResource(R.string.error_screen_test_tag)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 400.dp)
        ) {

            AnimatedVisibility(
                visible = true,
                enter = scaleIn() + fadeIn()
            ) {
                icon()
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag(stringResource(R.string.error_title_test_tag))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(errorRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag(stringResource(R.string.error_message_test_tag))
            )

            AnimatedVisibility(visible = onRetry != null) {
                Column {
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onRetry?.invoke() },
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .testTag(stringResource(R.string.retry_button_test_tag)),
                        contentPadding = PaddingValues(
                            horizontal = 24.dp,
                            vertical = 12.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .testTag(stringResource(R.string.retry_icon_test_Tag)),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.try_again))
                    }
                }
            }
        }
    }
}


/**
 * Preview of ErrorScreen showing a network error, without a retry button.
 */
@MultiDeviceAndModePreview
@Composable
fun ErrorScreenNetworkNoRetryPreview() {
    DogsTheme {
        ErrorScreen(
            error = UiError.NoNetwork,
            onRetry = null,
            modifier = Modifier
        )
    }
}

/**
 * Preview of ErrorScreen showing a server error, with the retry button.
 */
@MultiDeviceAndModePreview
@Composable
fun ErrorScreenServerWithRetryPreview() {
    DogsTheme {
        ErrorScreen(
            error = UiError.ServerError,
            onRetry = { /* no-op */ },
            modifier = Modifier
        )
    }
}