package io.github.fuyuz.svgicon.core

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ColorExtensionsTest {

    // ===========================================
    // withAlpha Tests
    // ===========================================

    @Test
    fun withAlphaReturnsColorWithNewAlpha() {
        val color = Color.Red.withAlpha(0.5f)
        assertEquals(0.5f, color.alpha, 0.01f)
        assertEquals(Color.Red.red, color.red, 0.01f)
        assertEquals(Color.Red.green, color.green, 0.01f)
        assertEquals(Color.Red.blue, color.blue, 0.01f)
    }

    @Test
    fun withAlphaZero() {
        val color = Color.Blue.withAlpha(0f)
        assertEquals(0f, color.alpha, 0.01f)
    }

    @Test
    fun withAlphaOne() {
        val color = Color.Green.withAlpha(1f)
        assertEquals(1f, color.alpha, 0.01f)
    }

    // ===========================================
    // alpha (multiplier) Tests
    // ===========================================

    @Test
    fun alphaMultiplierHalvesAlpha() {
        val original = Color.Red  // alpha = 1.0
        val result = original.alpha(0.5f)
        assertEquals(0.5f, result.alpha, 0.01f)
    }

    @Test
    fun alphaMultiplierOnPartiallyTransparent() {
        val original = Color.Blue.copy(alpha = 0.8f)
        val result = original.alpha(0.5f)
        assertEquals(0.4f, result.alpha, 0.01f)
    }

    @Test
    fun alphaMultiplierZeroMakesTransparent() {
        val result = Color.Green.alpha(0f)
        assertEquals(0f, result.alpha, 0.01f)
    }

    // ===========================================
    // toSvgColor Tests - 3-digit hex
    // ===========================================

    @Test
    fun toSvgColorParses3DigitHexWithHash() {
        val color = "#F00".toSvgColor()
        assertEquals(1f, color.red, 0.01f)
        assertEquals(0f, color.green, 0.01f)
        assertEquals(0f, color.blue, 0.01f)
    }

    @Test
    fun toSvgColorParses3DigitHexWithoutHash() {
        val color = "0F0".toSvgColor()
        assertEquals(0f, color.red, 0.01f)
        assertEquals(1f, color.green, 0.01f)
        assertEquals(0f, color.blue, 0.01f)
    }

    @Test
    fun toSvgColorParses3DigitHexBlue() {
        val color = "#00F".toSvgColor()
        assertEquals(0f, color.red, 0.01f)
        assertEquals(0f, color.green, 0.01f)
        assertEquals(1f, color.blue, 0.01f)
    }

    @Test
    fun toSvgColorParses3DigitHexWhite() {
        val color = "#FFF".toSvgColor()
        assertEquals(1f, color.red, 0.01f)
        assertEquals(1f, color.green, 0.01f)
        assertEquals(1f, color.blue, 0.01f)
    }

    @Test
    fun toSvgColorParses3DigitHexGray() {
        val color = "#888".toSvgColor()
        // 0x88 = 136, 136/255 ≈ 0.533
        assertEquals(136f / 255f, color.red, 0.01f)
        assertEquals(136f / 255f, color.green, 0.01f)
        assertEquals(136f / 255f, color.blue, 0.01f)
    }

    // ===========================================
    // toSvgColor Tests - 4-digit hex (ARGB)
    // ===========================================

    @Test
    fun toSvgColorParses4DigitHex() {
        val color = "#8F00".toSvgColor()  // A=0x88, R=0xFF, G=0x00, B=0x00
        assertEquals(136f / 255f, color.alpha, 0.01f)
        assertEquals(1f, color.red, 0.01f)
        assertEquals(0f, color.green, 0.01f)
        assertEquals(0f, color.blue, 0.01f)
    }

    @Test
    fun toSvgColorParses4DigitHexFullyOpaque() {
        val color = "#F0FF".toSvgColor()  // A=0xFF, R=0x00, G=0xFF, B=0xFF
        assertEquals(1f, color.alpha, 0.01f)
        assertEquals(0f, color.red, 0.01f)
        assertEquals(1f, color.green, 0.01f)
        assertEquals(1f, color.blue, 0.01f)
    }

    // ===========================================
    // toSvgColor Tests - 6-digit hex
    // ===========================================

    @Test
    fun toSvgColorParses6DigitHexRed() {
        val color = "#FF0000".toSvgColor()
        assertEquals(Color.Red, color)
    }

    @Test
    fun toSvgColorParses6DigitHexGreen() {
        val color = "#00FF00".toSvgColor()
        assertEquals(Color.Green, color)
    }

    @Test
    fun toSvgColorParses6DigitHexBlue() {
        val color = "#0000FF".toSvgColor()
        assertEquals(Color.Blue, color)
    }

    @Test
    fun toSvgColorParses6DigitHexWithoutHash() {
        val color = "FF00FF".toSvgColor()  // Magenta
        assertEquals(1f, color.red, 0.01f)
        assertEquals(0f, color.green, 0.01f)
        assertEquals(1f, color.blue, 0.01f)
    }

    @Test
    fun toSvgColorParses6DigitHexCustomColor() {
        val color = "#3B82F6".toSvgColor()  // Primary blue
        assertEquals(0x3B / 255f, color.red, 0.01f)
        assertEquals(0x82 / 255f, color.green, 0.01f)
        assertEquals(0xF6 / 255f, color.blue, 0.01f)
    }

    // ===========================================
    // toSvgColor Tests - 8-digit hex (AARRGGBB)
    // ===========================================

    @Test
    fun toSvgColorParses8DigitHex() {
        val color = "#80FF0000".toSvgColor()  // 50% transparent red
        assertEquals(0x80 / 255f, color.alpha, 0.01f)
        assertEquals(1f, color.red, 0.01f)
        assertEquals(0f, color.green, 0.01f)
        assertEquals(0f, color.blue, 0.01f)
    }

    @Test
    fun toSvgColorParses8DigitHexFullyOpaque() {
        val color = "#FF00FF00".toSvgColor()  // Fully opaque green
        assertEquals(1f, color.alpha, 0.01f)
        assertEquals(0f, color.red, 0.01f)
        assertEquals(1f, color.green, 0.01f)
        assertEquals(0f, color.blue, 0.01f)
    }

    @Test
    fun toSvgColorParses8DigitHexTransparent() {
        val color = "#000000FF".toSvgColor()  // Fully transparent blue
        assertEquals(0f, color.alpha, 0.01f)
        assertEquals(0f, color.red, 0.01f)
        assertEquals(0f, color.green, 0.01f)
        assertEquals(1f, color.blue, 0.01f)
    }

    // ===========================================
    // toSvgColor Tests - Error cases
    // ===========================================

    @Test
    fun toSvgColorThrowsOnInvalidLength() {
        assertFailsWith<IllegalArgumentException> {
            "#12345".toSvgColor()  // 5 digits - invalid
        }
    }

    @Test
    fun toSvgColorThrowsOnEmptyString() {
        assertFailsWith<IllegalArgumentException> {
            "".toSvgColor()
        }
    }

    @Test
    fun toSvgColorThrowsOnSingleCharacter() {
        assertFailsWith<IllegalArgumentException> {
            "F".toSvgColor()
        }
    }

    @Test
    fun toSvgColorThrowsOnTwoCharacters() {
        assertFailsWith<IllegalArgumentException> {
            "FF".toSvgColor()
        }
    }

    // ===========================================
    // SvgColors Object Tests
    // ===========================================

    @Test
    fun svgColorsTransparentIsTransparent() {
        assertEquals(Color.Transparent, SvgColors.Transparent)
    }

    @Test
    fun svgColorsCurrentColorIsUnspecified() {
        assertEquals(Color.Unspecified, SvgColors.CurrentColor)
    }

    @Test
    fun svgColorsPrimaryIsCorrect() {
        assertEquals(Color(0xFF3B82F6), SvgColors.Primary)
    }

    @Test
    fun svgColorsSuccessIsCorrect() {
        assertEquals(Color(0xFF22C55E), SvgColors.Success)
    }

    @Test
    fun svgColorsWarningIsCorrect() {
        assertEquals(Color(0xFFF59E0B), SvgColors.Warning)
    }

    @Test
    fun svgColorsErrorIsCorrect() {
        assertEquals(Color(0xFFEF4444), SvgColors.Error)
    }

    @Test
    fun svgColorsInfoIsCorrect() {
        assertEquals(Color(0xFF0EA5E9), SvgColors.Info)
    }

    @Test
    fun svgColorsGrayScaleProgression() {
        // Verify grays get progressively darker
        val grays = listOf(
            SvgColors.Gray50,
            SvgColors.Gray100,
            SvgColors.Gray200,
            SvgColors.Gray300,
            SvgColors.Gray400,
            SvgColors.Gray500,
            SvgColors.Gray600,
            SvgColors.Gray700,
            SvgColors.Gray800,
            SvgColors.Gray900
        )
        for (i in 0 until grays.size - 1) {
            // Each gray should be lighter (higher luminance) than the next
            val current = grays[i]
            val next = grays[i + 1]
            val currentLuminance = current.red + current.green + current.blue
            val nextLuminance = next.red + next.green + next.blue
            kotlin.test.assertTrue(
                currentLuminance > nextLuminance,
                "Gray${(i + 1) * 100 - 50} should be lighter than Gray${(i + 2) * 100 - 50}"
            )
        }
    }
}
