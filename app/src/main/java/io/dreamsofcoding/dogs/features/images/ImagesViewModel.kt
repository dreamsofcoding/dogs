package io.dreamsofcoding.dogs.features.images

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.dreamsofcoding.dogs.DogRepository
import io.dreamsofcoding.dogs.model.ApiResult
import io.dreamsofcoding.dogs.model.DogImage
import io.dreamsofcoding.dogs.ui.common.UiState
import io.dreamsofcoding.dogs.ui.common.mapToUiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ImagesViewModel @Inject constructor(
    private val repository: DogRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<DogImage>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<DogImage>>> = _uiState.asStateFlow()

    private val _breedDisplayName = MutableStateFlow("")
    val breedDisplayName: StateFlow<String> = _breedDisplayName.asStateFlow()

    private var allImages: List<DogImage> = emptyList()

    var selectedHeroImage by mutableStateOf<DogImage?>(null)

    fun loadImages(breedName: String) {
        _breedDisplayName.value = breedName.replaceFirstChar { it.uppercase() }
        Timber.d("Loading images for breed: $breedName")
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            when (val result = repository.getBreedImages(breedName)) {
                is ApiResult.Success -> {
                    allImages = result.data
                    Timber.d("Successfully loaded ${result.data.size} images for breed: $breedName")
                    _uiState.value = UiState.Success(getRandomImages())
                }
                is ApiResult.Error -> {
                    val errorMessage = mapToUiError(result.exception)
                    Timber.e(result.exception, "Failed to load images for breed: $breedName")
                    _uiState.value = UiState.Error(errorMessage, result.exception)
                }
            }
        }
    }

    fun refreshImages() {
        _uiState.value = UiState.Loading
        Timber.d("Refreshing images for breed: ${_breedDisplayName.value}")
        if (allImages.isNotEmpty()) {
            _uiState.value = UiState.Success(getRandomImages())
        } else {
            loadImages(_breedDisplayName.value)
        }
    }

    private fun getRandomImages(): List<DogImage> {
        return if (allImages.size <= 10) {
            allImages
        } else {
            allImages.shuffled().take(10)
        }
    }

    fun selectHero(image: DogImage) { selectedHeroImage = image }

}