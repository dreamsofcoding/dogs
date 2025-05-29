@file:OptIn(ExperimentalMaterial3Api::class)

package io.dreamsofcoding.dogs.features.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.dreamsofcoding.dogs.R
import io.dreamsofcoding.dogs.model.DogBreed
import io.dreamsofcoding.dogs.ui.common.ErrorScreen
import io.dreamsofcoding.dogs.ui.common.LoadingScreen
import io.dreamsofcoding.dogs.ui.common.MultiDeviceAndModePreview
import io.dreamsofcoding.dogs.ui.common.UiError
import io.dreamsofcoding.dogs.ui.common.UiState
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    onBreedClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val breedImages by viewModel.breedImages.collectAsStateWithLifecycle()
    var isSearchVisible by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.dog_breeds)) },
                    actions = {
                        IconButton(
                            onClick = {
                                isSearchVisible = !isSearchVisible
                                if (!isSearchVisible) {
                                    viewModel.updateSearchQuery("")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSearchVisible) Icons.Default.Clear else Icons.Default.Search,
                                contentDescription = stringResource(R.string.search_bar_visibility_content_description))
                        }
                    },
                    modifier = Modifier.testTag(stringResource(R.string.list_screen_topbar_test_tag))
                )

                if (isSearchVisible) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = viewModel::updateSearchQuery,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag(stringResource(R.string.search_field_test_tag)),
                        placeholder = { Text(stringResource(R.string.search_dog_breeds_dots)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.search)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.updateSearchQuery("") },
                                    modifier = Modifier.testTag(stringResource(R.string.clear_search_button_test_tag))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.clear_search)
                                    )
                                }
                            }
                        },
                        singleLine = true
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->

        when (uiState) {
            is UiState.Loading -> {
                LoadingScreen(
                    message = stringResource(R.string.loading_dog_breeds),
                    modifier = Modifier
                        .padding(paddingValues)
                        .testTag(stringResource(R.string.list_loading_screen_test_tag))
                )
            }

            is UiState.Success -> {
                ListContent(
                    breeds = (uiState as UiState.Success<List<DogBreed>>).data,
                    searchQuery = searchQuery,
                    breedImages = breedImages,
                    onBreedClick = onBreedClick,
                    onImageRequest = viewModel::loadBreedImage,
                    modifier = Modifier.padding(paddingValues),
                )
            }

            is UiState.Error -> {
                ErrorScreen(
                    onRetry = viewModel::retry,
                    modifier = Modifier
                        .padding(paddingValues)
                        .testTag(stringResource(R.string.list_error_screen_test_tag)),
                    error = (uiState as UiState.Error).error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListContent(
    breeds: List<DogBreed>,
    searchQuery: String,
    breedImages: Map<String, String>,
    onBreedClick: (String) -> Unit,
    onImageRequest: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyStaggeredGridState()
    val coroutineScope = rememberCoroutineScope()

    val filteredBreeds = remember(breeds, searchQuery) {
        if (searchQuery.isBlank()) {
            breeds
        } else {
            breeds.filter { breed ->
                breed.displayName.contains(searchQuery, ignoreCase = true) ||
                        breed.subBreeds.any { it.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    val groupedBreeds = remember(filteredBreeds) {
        filteredBreeds
            .groupBy { it.displayName.first().uppercaseChar() }
            .toSortedMap()
    }

    val alphabetList = remember(groupedBreeds) {
        groupedBreeds.keys.toList()
    }

    val letterToIndexMap = remember(groupedBreeds) {
        var currentIndex = 0
        val map = mutableMapOf<Char, Int>()

        groupedBreeds.forEach { (letter, breedsInGroup) ->
            map[letter] = currentIndex
            currentIndex += 1 + breedsInGroup.size
        }

        map
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalStaggeredGrid(
            state = gridState,
            columns = StaggeredGridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(start = 8.dp, end = 50.dp, top = 8.dp, bottom = 8.dp),
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag(stringResource(R.string.list_grid_test_tag))
        ) {
            groupedBreeds.forEach { (letter, breedsInGroup) ->
                item(
                    key = "header_$letter",
                    span = StaggeredGridItemSpan.FullLine
                ) {
                    Text(
                        text = letter.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .testTag(
                                stringResource(
                                    R.string.section_header_test_tag,
                                    letter
                                )
                            )
                    )
                }

                items(
                    items = breedsInGroup,
                    key = { "${letter}_${it.name}" }
                ) { breed ->
                    ListItem(
                        breed = breed,
                        onClick = {
                            Timber.d("Item clicked: ${breed.name}")
                            onBreedClick(breed.name)
                        },
                        onImageRequest = onImageRequest,
                        breedImages = breedImages,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(
                                stringResource(
                                    R.string.list_item_test_tag,
                                    breed.name
                                )
                            ),
                    )
                }
            }

            if (filteredBreeds.isEmpty() && searchQuery.isNotBlank()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                            .testTag(stringResource(R.string.list_empty_search_results_test_Tag)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No breeds found matching \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (alphabetList.isNotEmpty() && searchQuery.isBlank()) {
            AlphabetSidebar(
                letters = alphabetList,
                onLetterClick = { letter ->
                    letterToIndexMap[letter]?.let { index ->
                        coroutineScope.launch {
                            gridState.animateScrollToItem(index)
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .testTag(stringResource(R.string.alphabet_sidebar_test_tag))
            )
        }
    }
}





@MultiDeviceAndModePreview
@Composable
fun ListScreen_LoadingPreview() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dog_breeds)) },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LoadingScreen(
            message = stringResource(R.string.loading_dog_breeds),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag(stringResource(R.string.list_loading_screen_test_tag))
        )
    }
}

@MultiDeviceAndModePreview
@Composable
fun ListScreen_ErrorPreview() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dog_breeds)) },
                actions = { /* no-op */ }
            )
        }
    ) { padding ->
        ErrorScreen(
            onRetry = { /* no-op */ },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag(stringResource(R.string.list_error_screen_test_tag)),
            error = UiError.ServerError
        )
    }
}


@MultiDeviceAndModePreview
@Composable
fun ListScreen_SuccessPreview() {
    val breeds = listOf(
        DogBreed("beagle", "Beagle", emptyList()),
        DogBreed("bulldog", "Bulldog", listOf("Boston")),
        DogBreed("chihuahua", "Chihuahua", emptyList())
    )

    val images = mapOf(
        "beagle" to "https://images.dog.ceo/breeds/beagle/n02088364_11136.jpg",
        "bulldog" to "https://images.dog.ceo/breeds/bulldog-boston/n02096585_12716.jpg"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dog_breeds)) },
                actions = { /* no-op */ }
            )
        }
    ) { padding ->
        ListContent(
            breeds = breeds,
            searchQuery = "",
            breedImages = images,
            onBreedClick = {},
            onImageRequest = {},
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}


@MultiDeviceAndModePreview
@Composable
fun ListScreen_SearchPreview() {
    val breeds = listOf(
        DogBreed("beagle", "Beagle", emptyList()),
        DogBreed("bulldog", "Bulldog", listOf("Boston")),
        DogBreed("chihuahua", "Chihuahua", emptyList())
    )

    var searchVisible by remember { mutableStateOf(true) }
    var query by remember { mutableStateOf("b") }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.dog_breeds)) },
                    actions = {
                        IconButton(onClick = { searchVisible = !searchVisible; if (!searchVisible) query = "" }) {
                            Icon(
                                imageVector = if (searchVisible) Icons.Default.Clear else Icons.Default.Search,
                                contentDescription = null
                            )
                        }
                    }
                )
                if (searchVisible) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag(stringResource(R.string.search_field_test_tag)),
                        placeholder = { Text(stringResource(R.string.search_dog_breeds_dots)) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                    )
                }
            }
        }
    ) { padding ->
        ListContent(
            breeds = breeds,
            searchQuery = query,
            breedImages = emptyMap(),
            onBreedClick = {},
            onImageRequest = {},
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}