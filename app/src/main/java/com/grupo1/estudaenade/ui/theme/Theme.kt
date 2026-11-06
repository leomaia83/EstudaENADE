package com.grupo1.estudaenade.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/*
 * Sistema de cores em 3 camadas (Refactoring UI):
 *  - Primária: azul educacional #2F4DB8 (minoria dos pixels: ações e destaques)
 *  - Neutros: cinza levemente azulado (≈90% da interface)
 *  - Apoio: verde (acerto), vermelho (erro), âmbar (atenção), teal (secundária)
 * Contraste de texto verificado para WCAG AA (≥ 4.5:1) em claro e escuro (RNF06).
 */

private val Claro = lightColorScheme(
    primary = Color(0xFF2F4DB8), onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE3FF), onPrimaryContainer = Color(0xFF0B1A5C),
    secondary = Color(0xFF00796B), onSecondary = Color.White,
    secondaryContainer = Color(0xFFCDEDE8), onSecondaryContainer = Color(0xFF00382F),
    tertiary = Color(0xFF8A5300), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE6C2), onTertiaryContainer = Color(0xFF2E1A00),
    background = Color(0xFFF6F7FB), onBackground = Color(0xFF181B24),
    surface = Color.White, onSurface = Color(0xFF181B24),
    surfaceVariant = Color(0xFFE7E9F2), onSurfaceVariant = Color(0xFF444A5E),
    surfaceContainer = Color(0xFFF0F2F8), surfaceContainerHigh = Color(0xFFE9ECF4),
    surfaceContainerLow = Color(0xFFF8F9FC),
    outline = Color(0xFF767C90), outlineVariant = Color(0xFFC6CAD8),
    error = Color(0xFFC62828), onError = Color.White,
    errorContainer = Color(0xFFFBE4E4), onErrorContainer = Color(0xFF5F1010)
)

private val Escuro = darkColorScheme(
    primary = Color(0xFFB8C4FF), onPrimary = Color(0xFF14257A),
    primaryContainer = Color(0xFF2B3E99), onPrimaryContainer = Color(0xFFDDE3FF),
    secondary = Color(0xFF7FD6C8), onSecondary = Color(0xFF00382F),
    secondaryContainer = Color(0xFF00504A), onSecondaryContainer = Color(0xFFCDEDE8),
    tertiary = Color(0xFFFFB95C), onTertiary = Color(0xFF4A2800),
    tertiaryContainer = Color(0xFF6A3F00), onTertiaryContainer = Color(0xFFFFE6C2),
    background = Color(0xFF11131A), onBackground = Color(0xFFE3E5EE),
    surface = Color(0xFF181B24), onSurface = Color(0xFFE3E5EE),
    surfaceVariant = Color(0xFF2A2E3B), onSurfaceVariant = Color(0xFFC5C8D6),
    surfaceContainer = Color(0xFF1E2230), surfaceContainerHigh = Color(0xFF262A38),
    surfaceContainerLow = Color(0xFF161922),
    outline = Color(0xFF8F94A6), outlineVariant = Color(0xFF444A5E),
    error = Color(0xFFFF8A80), onError = Color(0xFF5F1010),
    errorContainer = Color(0xFF7A1C1C), onErrorContainer = Color(0xFFFFDAD6)
)

enum class TipoStatus { SUCESSO, ERRO, ALERTA, INFO, NEUTRO }

data class ParCor(val fundo: Color, val texto: Color)

/** Cores semânticas fixas no app inteiro: verde = acerto, vermelho = erro, âmbar = atenção, cinza = neutro. */
@Composable
fun corStatus(tipo: TipoStatus): ParCor {
    val escuro = isSystemInDarkTheme()
    return when (tipo) {
        TipoStatus.SUCESSO -> if (escuro) ParCor(Color(0xFF1B3D22), Color(0xFF9EE2A6)) else ParCor(Color(0xFFE3F4E4), Color(0xFF1B5E20))
        TipoStatus.ERRO -> if (escuro) ParCor(Color(0xFF4A1A1A), Color(0xFFFFB4AB)) else ParCor(Color(0xFFFBE4E4), Color(0xFFA31515))
        TipoStatus.ALERTA -> if (escuro) ParCor(Color(0xFF4A3300), Color(0xFFFFD27A)) else ParCor(Color(0xFFFFF1D6), Color(0xFF7A4A00))
        TipoStatus.INFO -> if (escuro) ParCor(Color(0xFF22306B), Color(0xFFCBD3FF)) else ParCor(Color(0xFFE4E8FF), Color(0xFF1F348F))
        TipoStatus.NEUTRO -> if (escuro) ParCor(Color(0xFF2A2E3B), Color(0xFFC5C8D6)) else ParCor(Color(0xFFECEEF4), Color(0xFF444A5E))
    }
}

private val Tipografia = Typography().let { t ->
    t.copy(
        headlineSmall = t.headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = t.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = t.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = TextStyle(fontSize = 15.sp, lineHeight = 22.sp)
    )
}

@Composable
fun EstudaEnadeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Escuro else Claro,
        typography = Tipografia,
        content = content
    )
}
