package io.dreamsofcoding.dogs.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.Coil.imageLoader
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import io.dreamsofcoding.dogs.model.DogBreed
import io.dreamsofcoding.dogs.ui.common.MultiDeviceAndModePreview
import io.dreamsofcoding.dogs.ui.theme.DogsTheme

@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    breed: DogBreed,
    onClick: () -> Unit,
    onImageRequest: (String) -> Unit = {},
    breedImages: Map<String, String> = emptyMap(),
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            ItemImageComponent(
                breed = breed,
                onImageRequest = onImageRequest,
                imageUrl = breedImages[breed.name],
                modifier = Modifier.fillMaxSize(),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = breed.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (breed.subBreeds.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = buildSubBreedsText(breed.subBreeds),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemImageComponent(
    breed: DogBreed,
    onImageRequest: (String) -> Unit,
    imageUrl: String?,
    modifier: Modifier = Modifier,
) {
    var hasError by remember { mutableStateOf(false) }

    LaunchedEffect(breed.name) {
        if (imageUrl == null) {
            onImageRequest(breed.name)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        when {
            hasError || imageUrl == null -> {

                if (imageUrl == null) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                }
            }

            else -> {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    contentDescription = "${breed.displayName} image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onError = {
                        hasError = true
                    }
                )
            }
        }
    }
}

private fun buildSubBreedsText(subBreeds: List<String>): String {
    return when {
        subBreeds.isEmpty() -> ""
        subBreeds.size == 1 -> "Includes: ${subBreeds[0].replaceFirstChar { it.uppercase() }}"
        subBreeds.size <= 3 -> {
            val formattedBreeds = subBreeds.map { it.replaceFirstChar { char -> char.uppercase() } }
            "Includes: ${formattedBreeds.joinToString(", ")}"
        }

        else -> {
            val firstThree =
                subBreeds.take(3).map { it.replaceFirstChar { char -> char.uppercase() } }
            val remaining = subBreeds.size - 3
            "Includes: ${firstThree.joinToString(", ")} +$remaining more"
        }
    }
}


/**
 * Preview of ListItem while the image is still loading (spinner).
 */
@MultiDeviceAndModePreview
@Composable
fun ListItemLoadingPreview() {
    DogsTheme {
        ListItem(
            breed = DogBreed(
                name = "beagle",
                displayName = "Beagle",
                subBreeds = emptyList()
            ),
            onClick = {},
            onImageRequest = {},
            breedImages = emptyMap(),
            modifier = Modifier
                .padding(16.dp)
                .size(200.dp),
        )
    }
}

/**
 * Preview of ListItem once the image URL is available.
 */
@MultiDeviceAndModePreview
@Composable
fun ListItemWithImagePreview() {
    DogsTheme {
        ListItem(
            breed = DogBreed(
                name = "bulldog",
                displayName = "Bulldog",
                subBreeds = listOf("english", "french", "american", "australian")
            ),
            onClick = {},
            onImageRequest = {},
            breedImages = mapOf(
                "bulldog" to "https://dog.ceo/api/breed/bulldog/images/random"
            ),
            modifier = Modifier
                .padding(16.dp)
                .size(200.dp),
        )
    }
}