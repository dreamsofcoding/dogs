@file:OptIn(ExperimentalMaterial3Api::class)

package io.dreamsofcoding.dogs.features.images

import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.dreamsofcoding.dogs.R
import io.dreamsofcoding.dogs.model.DogImage
import io.dreamsofcoding.dogs.ui.common.ErrorScreen
import io.dreamsofcoding.dogs.ui.common.LoadingScreen
import io.dreamsofcoding.dogs.ui.common.MultiDeviceAndModePreview
import io.dreamsofcoding.dogs.ui.common.UiError
import io.dreamsofcoding.dogs.ui.common.UiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ImagesScreen(
    breedName: String,
    viewModel: ImagesViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val breedDisplayName by viewModel.breedDisplayName.collectAsState()

    val gridState = rememberLazyStaggeredGridState()

    val cellConfiguration = if (LocalConfiguration.current.orientation == ORIENTATION_LANDSCAPE) {
        StaggeredGridCells.Adaptive(minSize = 175.dp)
    } else StaggeredGridCells.Fixed(2)

    LaunchedEffect(breedName) {
        viewModel.loadImages(breedName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(breedDisplayName.replaceFirstChar { it.uppercase() }) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshImages() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh images")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is UiState.Loading ->
                    LoadingScreen(
                        message = stringResource(R.string.images_loading_images),
                        modifier = Modifier
                            .padding(paddingValues)
                            .testTag(stringResource(R.string.images_loading_screen_test_tag))
                    )

                is UiState.Error -> {
                    ErrorScreen(error = (uiState as UiState.Error).error)
                }

                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag(stringResource(R.string.images_screen_empty_data_test_tag)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.images_empty))
                        }
                    } else {
                        LazyVerticalStaggeredGrid(
                            state = gridState,
                            columns = cellConfiguration,
                            contentPadding = PaddingValues(
                                top = 4.dp,
                                bottom = 8.dp,
                                start = 8.dp,
                                end = 8.dp
                            ),
                            verticalItemSpacing = 8.dp,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 308.dp)
                                .testTag(stringResource(R.string.images_screen_grid_test_tag))
                        ) {
                            items(
                                items = state.data,
                                key = { it.url }
                            ) { image ->
                                val isHero = image.url == viewModel.selectedHeroImage?.url
                                ImagesItem(
                                    image = image,
                                    isSelected = isHero,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .testTag(stringResource(R.string.images_screen_images_item_test_tag)),
                                    onClick = { viewModel.selectHero(image) }
                                )
                            }
                        }

                        HeroBanner(
                            image = viewModel.selectedHeroImage ?: state.data.first(),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(horizontal = 8.dp)
                                .testTag(stringResource(R.string.images_screen_hero_banner_test_tag))
                        )
                    }
                }
            }
        }
    }
}




@MultiDeviceAndModePreview
@Composable
fun ImagesScreen_LoadingPreview() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Breed") },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        LoadingScreen(
            message = stringResource(R.string.images_loading_images),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}


@MultiDeviceAndModePreview
@Composable
fun ImagesScreen_ErrorPreview() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Breed") },
                navigationIcon = { /*…*/ },
                actions = { /*…*/ }
            )
        }
    ) { padding ->
        ErrorScreen(
            error = UiError.NoNetwork,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}


@MultiDeviceAndModePreview
@Composable
fun ImagesScreen_SuccessPreview() {
    val images = listOf(
        DogImage("https://images.dog.ceo/breeds/pug/DSCF7495-2.jpg","", "pug"),
        DogImage("https://images.dog.ceo/breeds/pug/n02110958_10193.jpg","", "pug"),
        DogImage("https://images.dog.ceo/breeds/pug/n02110958_12275.jpg","", "pug"),
        DogImage("https://images.dog.ceo/breeds/pug/n02110958_13455.jpg","", "pug")
    )

    val hero = images.first()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pug") },
                navigationIcon = { /*…*/ },
                actions = { /*…*/ }
            )
        }
    ) { padding ->
        val orientation = LocalConfiguration.current.orientation
        val cells = if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            StaggeredGridCells.Adaptive(minSize = 175.dp)
        else StaggeredGridCells.Fixed(2)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // staggered grid
            LazyVerticalStaggeredGrid(
                columns = cells,
                contentPadding = PaddingValues(top = 308.dp, start = 8.dp, end = 8.dp, bottom = 8.dp),
                verticalItemSpacing = 8.dp,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(images, key = { it.url }) { img ->
                    ImagesItem(
                        image = img,
                        isSelected = img.url == hero.url,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {}
                }
            }

            // hero banner
            HeroBanner(
                image = hero,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 8.dp)
            )
        }
    }
}