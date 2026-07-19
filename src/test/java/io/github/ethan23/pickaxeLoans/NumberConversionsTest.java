package io.github.ethan23.pickaxeLoans;

import io.github.ethan23.pickaxeLoans.util.NumberConversions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;

import static org.junit.jupiter.api.Assertions.*;

public class NumberConversionsTest {

    private static final char GROUP = DecimalFormatSymbols.getInstance().getGroupingSeparator();
    private static final char DECIMAL = DecimalFormatSymbols.getInstance().getDecimalSeparator();

    @Test
    void timeFromMillis_zero_isZeroSeconds() {
        assertEquals("0s", NumberConversions.timeFromMillis(0));
    }

    @Test
    void timeFromMillis_secondsOnly() {
        assertEquals("45s", NumberConversions.timeFromMillis(45_000));
    }

    @Test
    void timeFromMillis_minutesPadSeconds() {
        assertEquals("1m 05s", NumberConversions.timeFromMillis(65_000));
    }

    @Test
    void timeFromMillis_exactHour() {
        assertEquals("1h 00m 00s", NumberConversions.timeFromMillis(3_600_000));
    }

    @Test
    void timeFromMillis_hoursMinutesSeconds() {
        assertEquals("2h 02m 05s", NumberConversions.timeFromMillis(7_325_000));
    }

    @Test
    void timeFromMillis_subSecond_truncatesToZero() {
        assertEquals("0s", NumberConversions.timeFromMillis(999));
    }

    @Test
    void round_truncatesToTwoDecimals() {
        assertEquals(12.34, NumberConversions.round(12.349));
    }

    @Test
    void round_doesNotRoundUp() {
        assertEquals(0.99, NumberConversions.round(0.999));
    }

    @Test
    void round_negative_truncatesTowardZero() {
        assertEquals(-1.23, NumberConversions.round(-1.239));
    }

    @Test
    void round_wholeNumber_isUnchanged() {
        assertEquals(100.0, NumberConversions.round(100.0));
    }

    @Test
    void hasSuffix_recognisesAllSuffixes() {
        assertTrue(NumberConversions.hasSuffix("10k"));
        assertTrue(NumberConversions.hasSuffix("10m"));
        assertTrue(NumberConversions.hasSuffix("10b"));
        assertTrue(NumberConversions.hasSuffix("10t"));
    }

    @Test
    void hasSuffix_plainNumber_isFalse() {
        assertFalse(NumberConversions.hasSuffix("1000"));
    }

    @Test
    void hasSuffix_isCaseSensitive() {
        assertFalse(NumberConversions.hasSuffix("10K"));
    }

    @Test
    void suffixToNumber_thousands() {
        assertEquals(1_000.0, NumberConversions.suffixToNumber("1k"));
    }

    @Test
    void suffixToNumber_decimalMultiplier() {
        assertEquals(1_500.0, NumberConversions.suffixToNumber("1.5k"));
    }

    @Test
    void suffixToNumber_millions() {
        assertEquals(2_000_000.0, NumberConversions.suffixToNumber("2m"));
    }

    @Test
    void suffixToNumber_billions() {
        assertEquals(3_000_000_000.0, NumberConversions.suffixToNumber("3b"));
    }

    @Test
    void suffixToNumber_trillions() {
        assertEquals(4_000_000_000_000.0, NumberConversions.suffixToNumber("4t"));
    }

    @Test
    void suffixToNumber_invalidNumber_returnsMinusOne() {
        assertEquals(-1.0, NumberConversions.suffixToNumber("abck"));
    }

    @Test
    void formattedNumberDisplay_bigDecimal_groupsThousands() {
        String expected = "1" + GROUP + "234" + GROUP + "567" + DECIMAL + "89";
        assertEquals(expected, NumberConversions.formattedNumberDisplay(new BigDecimal("1234567.89")));
    }

    @Test
    void formattedNumberDisplay_double_groupsThousands() {
        String expected = "1" + GROUP + "000";
        assertEquals(expected, NumberConversions.formattedNumberDisplay(1000.0));
    }

    @Test
    void formattedNumberDisplay_dropsTrailingZeroDecimals() {
        assertEquals("500", NumberConversions.formattedNumberDisplay(new BigDecimal("500.00")));
    }
}
