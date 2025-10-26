package com.ereaderapp.android.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    // Para elementos pequeños como chips, badges
    extraSmall = RoundedCornerShape(4.dp),

    // Para botones pequeños, inputs
    small = RoundedCornerShape(8.dp),

    // Para cards, la mayoría de componentes (matching web's 8-12px)
    medium = RoundedCornerShape(12.dp),

    // Para modales, bottom sheets
    large = RoundedCornerShape(16.dp),

    // Para full-screen dialogs, sheets desde abajo
    extraLarge = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
)

// Shapes adicionales para casos específicos
object CustomShapes {
    val BookCover = RoundedCornerShape(8.dp)
    val BookCoverLarge = RoundedCornerShape(12.dp)
    val ProfilePicture = RoundedCornerShape(50) // Circular
    val BottomSheet = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    val Dialog = RoundedCornerShape(16.dp)
    val SearchBar = RoundedCornerShape(24.dp)
}




