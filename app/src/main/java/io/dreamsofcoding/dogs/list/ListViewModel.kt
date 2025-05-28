package io.dreamsofcoding.dogs.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.dreamsofcoding.dogs.DogRepository
import io.dreamsofcoding.dogs.DogsException
import io.dreamsofcoding.dogs.model.ApiResult
import io.dreamsofcoding.dogs.model.DogBreed
import io.dreamsofcoding.dogs.ui.common.UiError
import io.dreamsofcoding.dogs.ui.common.UiState
import io.dreamsofcoding.dogs.ui.common.mapToUiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val repository: DogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<DogBreed>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<DogBreed>>> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _breedImages = MutableStateFlow<Map<String, String>>(emptyMap())
    val breedImages: StateFlow<Map<String, String>> = _breedImages.asStateFlow()

    init {
        Timber.d("ListViewModel initialized")
        loadBreeds()
    }

    fun loadBreeds() {
        Timber.d("Loading dog breeds")
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            when (val result = repository.getBreeds()) {
                is ApiResult.Success -> {
                    Timber.d("Successfully loaded ${result.data.size} breeds")
                    _uiState.value = UiState.Success(result.data)
                }

                is ApiResult.Error -> {
                    val errorMessage = mapToUiError(result.exception)
                    Timber.e(result.exception, "Failed to load breeds")
                    _uiState.value = UiState.Error(errorMessage, result.exception)
                }
            }
        }
    }

    fun retry() {
        loadBreeds()
    }

    fun updateSearchQuery(query: String) {
        Timber.d("Search query updated: $query")
        _searchQuery.value = query
    }

    fun loadBreedImage(breedName: String) {
        if (_breedImages.value.containsKey(breedName)) {
            return
        }

        viewModelScope.launch {
            Timber.d("Loading image for breed: $breedName")
            when (val result = repository.getBreedImages(breedName)) {
                is ApiResult.Success -> {
                    val images = result.data
                    if (images.isNotEmpty()) {
                        val imageUrl = images.random().url
                        _breedImages.value = _breedImages.value + (breedName to imageUrl)
                        Timber.d("Successfully loaded image for $breedName")
                    }
                }

                is ApiResult.Error -> {
                    Timber.e(result.exception, "Failed to load image for breed: $breedName")
                }
            }
        }
    }
}