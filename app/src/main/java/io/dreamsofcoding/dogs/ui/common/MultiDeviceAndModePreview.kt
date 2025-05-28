package io.dreamsofcoding.dogs.ui.common

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

/**
 * Shows this composable on Pixel 3a, Pixel 7 and Pixel Tablet,
 * each in Light & Dark mode at API 35.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)


@Preview(
    name         = "Pixel 3a • Light • API 35",
    device       = Devices.PIXEL_3A,
    showBackground = true,
    apiLevel     = 35
)

@Preview(
    name         = "Pixel 7 • Light • API 35",
    device       = Devices.PIXEL_7_PRO,
    showBackground = true,
    apiLevel     = 35
)

@Preview(
    name         = "Pixel Tablet • Light • API 35",
    device       = Devices.PIXEL_TABLET,
    showBackground = true,
    apiLevel     = 35
)

@Preview(
    name         = "Pixel 3a • Dark • API 35",
    device       = Devices.PIXEL_3A,
    uiMode       = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    apiLevel     = 35
)

@Preview(
    name         = "Pixel 7 • Dark • API 35",
    device       = Devices.PIXEL_7_PRO,
    uiMode       = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    apiLevel     = 35
)

@Preview(
    name         = "Pixel Tablet • Dark • API 35",
    device       = Devices.PIXEL_TABLET,
    uiMode       = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    apiLevel     = 35
)
annotation class MultiDeviceAndModePreview