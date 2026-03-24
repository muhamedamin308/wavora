package com.wavora.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wavora.app.ui.theme.PlaybackAccent

/**
 * @author Muhamed Amin Hassan on 24,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

data class LyricsLine(
    val timestampMs: Long,
    val text: String,
)


@Composable
fun LyricsPanel(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    songTitle: String?,
    artistName: String?,
    lyrics: List<LyricsLine>? = null,           // null = not loaded
    currentLineIndex: Int = -1,
    positionMs: Long = 0L,
) {
    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            when {
                lyrics == null -> LyricsUnavailable(songTitle, artistName)
                lyrics.isEmpty() -> LyricsUnavailable(songTitle, artistName)
                else -> LyricsContent(
                    lines = lyrics,
                    currentLineIndex = currentLineIndex,
                )
            }
        }
    }
}

@Composable
fun LyricsUnavailable(
    songTitle: String?,
    artistName: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Filled.Lyrics, null,
            Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        )
        Spacer(Modifier.height(16.dp))

        Text(
            text = "No lyrics found",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))

        songTitle?.let {
            Text(
                text = buildString {
                    append("\"$songTitle\"")
                    if (artistName != null) append("\nby $artistName")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(12.dp))

        Text(
            text = "Place an .lrc file next to the audio file\nto enable synced lyrics",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun LyricsContent(
    lines: List<LyricsLine>,
    currentLineIndex: Int,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(currentLineIndex) {
        if (currentLineIndex >= 0)
            listState.animateScrollToItem(
                index = (currentLineIndex - 2).coerceAtLeast(0)
            )
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(lines, key = { index, _ -> index }) { index, line ->
            val isCurrent = index == currentLineIndex
            Text(
                text = line.text,
                style = if (isCurrent)
                    MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                else
                    MaterialTheme.typography.bodyMedium,
                color = when {
                    isCurrent -> PlaybackAccent
                    index < currentLineIndex -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                },
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (isCurrent) 1f else 0.85f),
            )
        }
    }
}