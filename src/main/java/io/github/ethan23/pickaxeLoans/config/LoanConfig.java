package io.github.ethan23.pickaxeLoans.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Immutable snapshot of config.yml, read once on enable.
 *
 * <p>Everything outside {@link #fromFileConfiguration} is plain Java: the
 * rest of the plugin (and the tests) depend only on this object, never on
 * Bukkit's configuration API, so the service layer stays testable without
 * a server.
 *
 * <p>Values are validated on load rather than trusted: out-of-range values
 * are clamped and inverted min/max pairs fall back to the defaults, each
 * with a console warning. A bad config file degrades loudly instead of
 * taking the loan market offline.
 *
 * <p>Loans snapshot their deal terms at creation, so a config change only
 * affects listings created after the restart.
 */
public class LoanConfig {

    private final int maxListingsPerPlayer;
    private final long listingExpiryMillis;
    private final long upfrontCostMin, upfrontCostMax;
    private final long durationMinutesMin, durationMinutesMax;
    private final int xpTaxMin, xpTaxMax;
    private final int energyTaxMin, energyTaxMax;

    private static final int DEFAULT_MAX_LISTINGS = 3;
    private static final long DEFAULT_LISTING_EXPIRY_MINUTES = 60;
    private static final long DEFAULT_COST_MIN = 0, DEFAULT_COST_MAX = 1_000_000_000L;
    private static final long DEFAULT_DURATION_MIN = 10, DEFAULT_DURATION_MAX = 60;
    private static final int DEFAULT_TAX_MIN = 0, DEFAULT_TAX_MAX = 100;

    public LoanConfig(int maxListingsPerPlayer, long listingExpiryMillis, long upfrontCostMin, long upfrontCostMax, long durationMinutesMin, long durationMinutesMax, int xpTaxMin, int xpTaxMax, int energyTaxMin, int energyTaxMax) {
        this.maxListingsPerPlayer = maxListingsPerPlayer;
        this.listingExpiryMillis = listingExpiryMillis;
        this.upfrontCostMin = upfrontCostMin;
        this.upfrontCostMax = upfrontCostMax;
        this.durationMinutesMin = durationMinutesMin;
        this.durationMinutesMax = durationMinutesMax;
        this.xpTaxMin = xpTaxMin;
        this.xpTaxMax = xpTaxMax;
        this.energyTaxMin = energyTaxMin;
        this.energyTaxMax = energyTaxMax;
    }

    /**
     * Clamps a value into its allowed range, warning when the file's value had to be repaired.
     */
    private static int clampedInt(String key, int value, int lo, int hi, Logger logger) {
        if (value >= lo && value <= hi) {
            return value;
        }
        int fixed = Math.max(lo, Math.min(hi, value));
        logger.warning("config.yml: " + key + "=" + value + " is outside " + lo + "-" + hi + ", using " + fixed);
        return fixed;
    }

    /**
     * Clamps a value into its allowed range, warning when the file's value had to be repaired.
     */
    private static long clampedLong(String key, long value, long lo, long hi, Logger logger) {
        if (value >= lo && value <= hi) {
            return value;
        }
        long fixed = Math.max(lo, Math.min(hi, value));
        logger.warning("config.yml: " + key + "=" + value + " is outside " + lo + "-" + hi + ", using " + fixed);
        return fixed;
    }

    /**
     * Validates that a min/max pair is ordered. Clamping cannot repair an
     * inverted pair, so the whole pair falls back to the defaults instead.
     *
     * @return {@code [min, max]}, either the given pair or the defaults
     */
    private static long[] orderedRange(String key, long min, long max, long defMin, long defMax, Logger logger) {
        if (min <= max) {
            return new long[]{min, max};
        }
        logger.warning("config.yml: " + key + " min " + min + " is greater than max " + max
                + ", using defaults " + defMin + "-" + defMax);
        return new long[]{defMin, defMax};
    }

    /**
     * Reads and validates the plugin's configuration.
     *
     * <p>Missing keys fall back to the defaults baked into the jar's
     * config.yml. Present-but-invalid values are repaired: each value is
     * clamped into its allowed range first, then each min/max pair is
     * checked for ordering and reset to the defaults if inverted.
     *
     * <p>Hard caps enforced here regardless of the file's contents: taxes
     * never exceed 100% (a higher tax would make borrowers earn negative
     * rewards), and the listing limit never exceeds 45 (the active-loans
     * menu has 45 slots and no pagination).
     *
     * @param config the plugin's loaded configuration
     * @param logger used to warn about repaired values
     * @return the validated, immutable configuration
     */
    public static LoanConfig fromFileConfiguration(FileConfiguration config, Logger logger) {
        int maxListings = clampedInt("loans.max-listings-per-player",
                config.getInt("loans.max-listings-per-player", DEFAULT_MAX_LISTINGS), 1, 45, logger);

        long expiryMinutes = clampedLong("loans.listing-expiry-minutes",
                config.getLong("loans.listing-expiry-minutes", DEFAULT_LISTING_EXPIRY_MINUTES),
                1, TimeUnit.DAYS.toMinutes(7), logger);

        long[] cost = orderedRange("limits.upfront-cost",
                clampedLong("limits.upfront-cost.min", config.getLong("limits.upfront-cost.min", DEFAULT_COST_MIN), 0, Long.MAX_VALUE, logger),
                clampedLong("limits.upfront-cost.max", config.getLong("limits.upfront-cost.max", DEFAULT_COST_MAX), 0, Long.MAX_VALUE, logger),
                DEFAULT_COST_MIN, DEFAULT_COST_MAX, logger);

        long[] duration = orderedRange("limits.duration-minutes",
                clampedLong("limits.duration-minutes.min", config.getLong("limits.duration-minutes.min", DEFAULT_DURATION_MIN), 1, Long.MAX_VALUE, logger),
                clampedLong("limits.duration-minutes.max", config.getLong("limits.duration-minutes.max", DEFAULT_DURATION_MAX), 1, Long.MAX_VALUE, logger),
                DEFAULT_DURATION_MIN, DEFAULT_DURATION_MAX, logger);

        long[] xpTax = orderedRange("limits.xp-tax-percent",
                clampedInt("limits.xp-tax-percent.min", config.getInt("limits.xp-tax-percent.min", DEFAULT_TAX_MIN), 0, 100, logger),
                clampedInt("limits.xp-tax-percent.max", config.getInt("limits.xp-tax-percent.max", DEFAULT_TAX_MAX), 0, 100, logger),
                DEFAULT_TAX_MIN, DEFAULT_TAX_MAX, logger);

        long[] energyTax = orderedRange("limits.energy-tax-percent",
                clampedInt("limits.energy-tax-percent.min", config.getInt("limits.energy-tax-percent.min", DEFAULT_TAX_MIN), 0, 100, logger),
                clampedInt("limits.energy-tax-percent.max", config.getInt("limits.energy-tax-percent.max", DEFAULT_TAX_MAX), 0, 100, logger),
                DEFAULT_TAX_MIN, DEFAULT_TAX_MAX, logger);

        return new LoanConfig(maxListings, TimeUnit.MINUTES.toMillis(expiryMinutes),
                cost[0], cost[1], duration[0], duration[1],
                (int) xpTax[0], (int) xpTax[1], (int) energyTax[0], (int) energyTax[1]);
    }

    public int getMaxListingsPerPlayer() {
        return maxListingsPerPlayer;
    }

    public long getListingExpiryMillis() {
        return listingExpiryMillis;
    }

    public long getUpfrontCostMin() {
        return upfrontCostMin;
    }

    public long getUpfrontCostMax() {
        return upfrontCostMax;
    }

    public long getDurationMinutesMin() {
        return durationMinutesMin;
    }

    public long getDurationMinutesMax() {
        return durationMinutesMax;
    }

    public int getXpTaxMin() {
        return xpTaxMin;
    }

    public int getXpTaxMax() {
        return xpTaxMax;
    }

    public int getEnergyTaxMin() {
        return energyTaxMin;
    }

    public int getEnergyTaxMax() {
        return energyTaxMax;
    }
}
