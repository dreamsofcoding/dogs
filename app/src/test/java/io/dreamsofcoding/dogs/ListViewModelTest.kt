package io.dreamsofcoding.dogs

import io.dreamsofcoding.dogs.DogRepository
import io.dreamsofcoding.dogs.features.list.ListViewModel
import io.dreamsofcoding.dogs.model.ApiResult
import io.dreamsofcoding.dogs.model.DogBreed
import io.dreamsofcoding.dogs.model.DogImage
import io.dreamsofcoding.dogs.ui.common.UiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModelTest {
    private val repository = mockk<DogRepository>(relaxed = true)
    private lateinit var viewModel: ListViewModel
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        viewModel = ListViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadBreeds returns Success when repository returns data`() = runTest {
        val data = listOf(DogBreed("beagle", "Beagle", emptyList()))
        coEvery { repository.getBreeds() } returns ApiResult.Success(data)

        viewModel.loadBreeds()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is UiState.Success)
        val stateData = (viewModel.uiState.value as UiState.Success).data
        assertEquals(data, stateData)
    }

    @Test
    fun `loadBreeds returns Error when repository returns error`() = runTest {
        val ex = RuntimeException("Network failure")
        coEvery { repository.getBreeds() } returns ApiResult.Error(ex)

        viewModel.loadBreeds()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is UiState.Error)
    }

    @Test
    fun `retry returns a success result`() = runTest {
        coEvery { repository.getBreeds() } returns ApiResult.Success(emptyList())

        viewModel.retry()
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.getBreeds() }
    }

    @Test
    fun `updateSearchQuery updates the state flow`() {
        val query = "bulldog"
        viewModel.updateSearchQuery(query)
        assertEquals(query, viewModel.searchQuery.value)
    }

    @Test
    fun `loadBreedImage - success updates breedImages`() = runTest {
        val breedName = "bulldog"
        val imageUrl = "https://dog.ceo/breeds/bulldog-boston/n02096585_12716.jpg"
        val breedImage = DogImage(
            url = imageUrl,
            localPath = "",
            breed = "bulldog"
        )
        coEvery { repository.getBreedImages(breedName, 1) } returns ApiResult.Success(listOf(breedImage))
        viewModel.loadBreedImage(breedName)
        advanceUntilIdle()

        val imagesMap = viewModel.breedImages.value
        assertEquals(1, imagesMap.size)
        assertTrue(imagesMap.containsKey(breedName))
        assertEquals(imageUrl, imagesMap[breedName])
    }

    @Test
    fun `loadBreedImage - error does not update breedImages`() = runTest {
        val breedName = "labrador"
        coEvery { repository.getBreedImages(breedName, 1) } returns ApiResult.Error(Exception("network"))

        viewModel.loadBreedImage(breedName)
        advanceUntilIdle()

        assertTrue(viewModel.breedImages.value.isEmpty())
    }
}
