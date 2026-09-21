package com.chatapp.chat.presentation.chat_detail.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun MessageImageGrid(
    imageUrls: List<String>,
    onImageClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (imageUrls.isEmpty()) return

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        imageUrls.forEachIndexed { index, url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onImageClick(index) }
            )
        }
    }
}
