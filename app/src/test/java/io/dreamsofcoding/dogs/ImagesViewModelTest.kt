package io.dreamsofcoding.dogs

import io.dreamsofcoding.dogs.features.images.ImagesViewModel
import io.dreamsofcoding.dogs.model.ApiResult
import io.dreamsofcoding.dogs.model.DogImage
import io.dreamsofcoding.dogs.ui.common.UiState
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImagesViewModelTest {

    private val repository = mockk<DogRepository>(relaxed = true)
    private val dispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ImagesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = ImagesViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadImages error results in UiState Error with exception`() = runTest {
        val breed = "retriever"
        val ex = RuntimeException("no network")
        coEvery { repository.getBreedImages(breed) } returns ApiResult.Error(ex)

        viewModel.loadImages(breed)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Error)
        assertEquals(ex, (state as UiState.Error).error)
    }

    @Test
    fun `selectHero sets the selectedHeroImage`() {
        val img = DogImage(
            url = "https://images.dog.ceo/breeds/hound-afghan/n02088094_1003.jpg",
            localPath = "",
            breed = "hound"
        )
        assertNull(viewModel.selectedHeroImage)
        viewModel.selectHero(img)
        assertEquals(img, viewModel.selectedHeroImage)
    }

    @Test
    fun `loadImages returns at most 10 images`() = runTest {
        val breed = "hound"
        val allImages = (1..12).map { i ->
            DogImage(
                url = "https://images.dog.ceo/breeds/pug/img$i.jpg",
                localPath = "",
                breed = breed
            )
        }
        coEvery { repository.getBreedImages(breed) } returns ApiResult.Success(allImages)

        viewModel.loadImages(breed)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        val returned = (state as UiState.Success).data
        assertEquals(10, returned.size)

        assertTrue(allImages.containsAll(returned))
    }


    @Test
    fun `refreshImages with loaded images does not hit repository again`() = runTest {
        val breed = "hound"
        val imageUrls = listOf<String>(
            "https://images.dog.ceo/breeds/hound-afghan/n02088094_1003.jpg",
            "https://images.dog.ceo/breeds/hound-afghan/n02088094_10263.jpg",
            "https://images.dog.ceo/breeds/hound-afghan/n02088094_10715.jpg",
            "https://images.dog.ceo/breeds/hound-afghan/n02088094_10822.jpg",
            "https://images.dog.ceo/breeds/hound-afghan/n02088094_1128.jpg",
            "https://images.dog.ceo/breeds/hound-afghan/n02088094_1145.jpg",
            "https://images.dog.ceo/breeds/hound-afghan/n02088094_115.jpg",
            "https://images.dog.ceo/breeds/hound-afghan/n02088094_1150.jpg",
            "https://images.dog.ceo/breeds/hound-afghan/n02088094_11570.jpg",
            "https://images.dog.ceo/breeds/hound-afghan/n02088094_11584.jpg",
            "https://images.dog.ceo/breeds/hound-afghan/n02088094_1186.jpg",
        )

        val images = imageUrls.map { url ->
            DogImage(
                url = url,
                localPath = "",
                breed = breed
            )
        }
        coEvery { repository.getBreedImages(breed) } returns ApiResult.Success(images)

        viewModel.loadImages(breed)
        dispatcher.scheduler.advanceUntilIdle()
        clearMocks(repository)

        viewModel.refreshImages()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        assertEquals(images, (state as UiState.Success).data)

        coVerify(exactly = 0) { repository.getBreedImages(any()) }
    }

    @Test
    fun `loadImages returns images and updates state and display name`() = runTest {
        val breed = "hound"
        val images = listOf(
            DogImage(
                url = "https://images.dog.ceo/breeds/hound-afghan/n02088094_1003.jpg",
                localPath = "",
                breed = breed
            ),
            DogImage(
                url = "https://images.dog.ceo/breeds/hound-afghan/n02088094_10822.jpg",
                localPath = "",
                breed = breed
            )
        )
        coEvery { repository.getBreedImages(breed) } returns ApiResult.Success(images)
        viewModel.loadImages(breed)

        assertEquals("Beagle", viewModel.breedDisplayName.value)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        assertEquals(images, (state as UiState.Success).data)
    }
}