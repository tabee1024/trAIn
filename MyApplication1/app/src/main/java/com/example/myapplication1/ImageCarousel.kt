package com.example.myapplication1

import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun ImageCarousel() {
    val images = listOf(
        R.drawable.workout_image1,
        R.drawable.workout_image2,
        R.drawable.workout_image3
    )

    // Use a very large page count to simulate infinite scrolling
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { Int.MAX_VALUE })
    val coroutineScope = rememberCoroutineScope()

    // Auto-slide effect that resumes from the current page
    LaunchedEffect(pagerState) {
        while (isActive) {
            if (!pagerState.isScrollInProgress) {
                delay(7000) // every 3 seconds
                val nextPage = pagerState.currentPage + 1
                pagerState.animateScrollToPage(nextPage)
            } else {
                // If user is swiping, wait a bit before checking again
                delay(500)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) { page ->
            val imageIndex = page % images.size
            Image(
                painter = painterResource(id = images[imageIndex]),
                contentDescription = "Carousel Image $imageIndex",
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Clickable indicator dots
        Row {
            repeat(images.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(if (pagerState.currentPage % images.size == index) 12.dp else 8.dp)
                        .background(
                            if (pagerState.currentPage % images.size == index) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            shape = CircleShape
                        )
                        .clickable {
                            coroutineScope.launch {
                                // Jump directly to the tapped image
                                val targetPage = pagerState.currentPage - (pagerState.currentPage % images.size) + index
                                pagerState.animateScrollToPage(targetPage)
                            }
                        }
                )
            }
        }
    }
}
