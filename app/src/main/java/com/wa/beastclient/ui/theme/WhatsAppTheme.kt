package com.wa.beastclient.ui.theme

import android.content.res.AssetManager
import android.graphics.Typeface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class WaColors(
    val isDark: Boolean,
    val chatCanvas: Color,
    val appSurface: Color,
    val elevatedBar: Color,
    val outgoingBubble: Color,
    val incomingBubble: Color,
    val accentGreen: Color,
    val brightGreen: Color,
    val blueTick: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val pillActiveBg: Color,
    val pillActiveText: Color,
    val pillInactiveBg: Color,
    val antiDeleteRed: Color,
    val warningAmber: Color,
    val quotedBg: Color,
    val divider: Color,
    val mediaScrim: Color,
    val doodleTintAlpha: Float
)

val WaDarkColors = WaColors(
    isDark = true,
    chatCanvas = Color(0xFF0B141A),
    appSurface = Color(0xFF0B141A),
    elevatedBar = Color(0xFF1F2C34),
    outgoingBubble = Color(0xFF005C4B),
    incomingBubble = Color(0xFF202C33),
    accentGreen = Color(0xFF00A884),
    brightGreen = Color(0xFF25D366),
    blueTick = Color(0xFF53BDEB),
    textPrimary = Color(0xFFE9EDEF),
    textSecondary = Color(0xFF8696A0),
    pillActiveBg = Color(0xFF0A332C),
    pillActiveText = Color(0xFF25D366),
    pillInactiveBg = Color(0xFF202C33),
    antiDeleteRed = Color(0xFFF15C6D),
    warningAmber = Color(0xFFFFB74D),
    quotedBg = Color(0x28000000),
    divider = Color(0xFF1E2A30),
    mediaScrim = Color(0x66000000),
    doodleTintAlpha = 0.06f
)

val WaLightColors = WaColors(
    isDark = false,
    chatCanvas = Color(0xFFEFEAE2),
    appSurface = Color(0xFFFFFFFF),
    elevatedBar = Color(0xFFF0F2F5),
    outgoingBubble = Color(0xFFD9FDD3),
    incomingBubble = Color(0xFFFFFFFF),
    accentGreen = Color(0xFF008069),
    brightGreen = Color(0xFF1DAA61),
    blueTick = Color(0xFF53BDEB),
    textPrimary = Color(0xFF111B21),
    textSecondary = Color(0xFF667781),
    pillActiveBg = Color(0xFFD8FDD2),
    pillActiveText = Color(0xFF15603E),
    pillInactiveBg = Color(0xFFF0F2F5),
    antiDeleteRed = Color(0xFFD32F2F),
    warningAmber = Color(0xFFE65100),
    quotedBg = Color(0x14000000),
    divider = Color(0xFFE9EDEF),
    mediaScrim = Color(0x66000000),
    doodleTintAlpha = 0.40f
)

val LocalWaColors = staticCompositionLocalOf { WaDarkColors }
val LocalWaMonoFamily = staticCompositionLocalOf<FontFamily> { FontFamily.Monospace }

@Composable
fun WhatsAppTheme(
    darkTheme: Boolean,
    assetManager: AssetManager,
    content: @Composable () -> Unit
) {
    val waColors = if (darkTheme) WaDarkColors else WaLightColors
    val sansFamily: FontFamily = remember(assetManager) {
        runCatching {
            FontFamily(Typeface.createFromAsset(assetManager, "fonts/Roboto-Medium.ttf"))
        }.getOrDefault(FontFamily.SansSerif)
    }
    val monoFamily: FontFamily = remember(assetManager) {
        runCatching {
            FontFamily(Typeface.createFromAsset(assetManager, "fonts/RobotoMono-Regular.ttf"))
        }.getOrDefault(FontFamily.Monospace)
    }

    val typography = remember(sansFamily) {
        Typography(
            titleLarge = TextStyle(
                fontFamily = sansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 21.sp,
                lineHeight = 26.sp
            ),
            titleMedium = TextStyle(
                fontFamily = sansFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 17.sp,
                lineHeight = 22.sp
            ),
            bodyLarge = TextStyle(
                fontFamily = sansFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 15.5.sp,
                lineHeight = 20.5.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = sansFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 19.sp
            ),
            labelSmall = TextStyle(
                fontFamily = sansFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontFeatureSettings = "tnum"
            )
        )
    }

    val m3Scheme = if (darkTheme) {
        darkColorScheme(
            primary = waColors.accentGreen,
            onPrimary = Color.White,
            secondary = waColors.brightGreen,
            background = waColors.appSurface,
            surface = waColors.appSurface,
            surfaceVariant = waColors.elevatedBar,
            onBackground = waColors.textPrimary,
            onSurface = waColors.textPrimary,
            onSurfaceVariant = waColors.textSecondary,
            error = waColors.antiDeleteRed
        )
    } else {
        lightColorScheme(
            primary = waColors.accentGreen,
            onPrimary = Color.White,
            secondary = waColors.brightGreen,
            background = waColors.appSurface,
            surface = waColors.appSurface,
            surfaceVariant = waColors.elevatedBar,
            onBackground = waColors.textPrimary,
            onSurface = waColors.textPrimary,
            onSurfaceVariant = waColors.textSecondary,
            error = waColors.antiDeleteRed
        )
    }

    CompositionLocalProvider(
        LocalWaColors provides waColors,
        LocalWaMonoFamily provides sansFamily
    ) {
        MaterialTheme(
            colorScheme = m3Scheme,
            typography = typography,
            content = content
        )
    }
}
