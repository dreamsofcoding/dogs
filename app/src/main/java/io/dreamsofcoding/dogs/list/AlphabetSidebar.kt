package io.dreamsofcoding.dogs.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.dreamsofcoding.dogs.ui.common.MultiDeviceAndModePreview
import io.dreamsofcoding.dogs.ui.theme.DogsTheme
import kotlinx.coroutines.delay
import kotlin.toString

@Composable
fun AlphabetSidebar(
    letters: List<Char>,
    onLetterClick: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedLetter by remember { mutableStateOf<Char?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var sidebarSize by remember { mutableStateOf(IntSize.Zero) }
    var indicatorOffset by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current

    LaunchedEffect(selectedLetter) {
        if (selectedLetter != null && !isDragging) {
            delay(500)
            selectedLetter = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(50.dp)
            .padding(end = 8.dp, top = 16.dp, bottom = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    sidebarSize = coordinates.size
                }
                .pointerInput(letters) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            indicatorOffset = offset.y
                            val letterIndex = (indicatorOffset / (sidebarSize.height / letters.size)).toInt()
                                .coerceIn(0, letters.size - 1)
                            selectedLetter = letters[letterIndex]
                            onLetterClick(letters[letterIndex])
                        },
                        onDrag = { change, offset ->
                            indicatorOffset += offset.y
                            indicatorOffset = indicatorOffset.coerceIn(0f, sidebarSize.height.toFloat())
                            val letterIndex = (indicatorOffset / (sidebarSize.height / letters.size)).toInt()
                                .coerceIn(0, letters.size - 1)
                            val newLetter = letters[letterIndex]
                            if (newLetter != selectedLetter) {
                                selectedLetter = newLetter
                                onLetterClick(newLetter)
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                        }
                    )
                }
        ) {
            letters.forEachIndexed { index, letter ->
                val isSelected = selectedLetter == letter

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected && !isDragging) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            } else {
                                Color.Transparent
                            }
                        )
                        .clickable {
                            selectedLetter = letter
                            onLetterClick(letter)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter.toString(),
                        fontSize = 18.sp,
                        fontWeight = if (isSelected && !isDragging) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected && !isDragging) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (isDragging && selectedLetter != null) {
            Box(
                modifier = Modifier
                    .offset(x = (-60).dp, y = with(density) { indicatorOffset.toDp() - 24.dp })
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedLetter.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}


/**
 * Preview of the alphabet sidebar with letters A–Z.
 */
@MultiDeviceAndModePreview
@Composable
fun AlphabetSidebarPreview() {
    DogsTheme {
        AlphabetSidebar(
            letters = ('A'..'Z').toList(),
            onLetterClick = { /* no-op */ },
            modifier = Modifier
        )
    }
}