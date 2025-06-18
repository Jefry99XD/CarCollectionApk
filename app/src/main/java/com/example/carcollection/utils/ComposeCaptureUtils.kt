package com.example.carcollection.utils

import android.content.Context
import android.view.View
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import com.example.carcollection.data.local.Car
import com.example.carcollection.presentation.main.components.CarCard

fun createCarCardView(context: Context, car: Car): View {
    val composeView = ComposeView(context).apply {
        setContent {
            MaterialTheme {
                CarCard(
                    car = car,
                    onEdit = {},
                    onDelete = {},
                    onClick = {},
                    allTags = listOf()

                )
            }
        }

        // Medimos y colocamos el Composable
        measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        layout(0, 0, measuredWidth, measuredHeight)
    }

    return composeView
}