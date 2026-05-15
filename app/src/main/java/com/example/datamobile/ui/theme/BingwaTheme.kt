package com.example.datamobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun BingwaTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = BingwaPrimary,
            secondary = BingwaSecondary,
            background = BingwaBackground,
            surface = BingwaSurface,
            error = BingwaError
        ),
        content = content
    )
}
