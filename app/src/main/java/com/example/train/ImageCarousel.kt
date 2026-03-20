package com.example.train

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ImageCarousel() {
    val images = listOf(
        R.drawable.workout_image1,
        R.drawable.workout_image2,
        R.drawable.workout_image3
    )

    // Using a large number for infinite effect, starting at a middle point
    val startIndex = Int.MAX_VALUE / 2
    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { Int.MAX_VALUE })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState) {
        while (isActive) {
            delay(5000) // Wait 5 seconds
            if (!pagerState.isScrollInProgress) {
                // FIX: Add animationSpec to make it slide smoothly
                pagerState.animateScrollToPage(
                    page = pagerState.currentPage + 1,
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 1000, // 1 second sliding transition
                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                    )
                )
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(200.dp)
        ) { page ->
            val index = page % images.size
            Image(
                painter = painterResource(id = images[index]),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            images.indices.forEach { index ->
                val isSelected = pagerState.currentPage % images.size == index
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(if (isSelected) 10.dp else 6.dp)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                        .clickable {
                            coroutineScope.launch {
                                val currentIdx = pagerState.currentPage % images.size
                                pagerState.animateScrollToPage(pagerState.currentPage + (index - currentIdx))
                            }
                        }
                )
            }
        }
    }
}
