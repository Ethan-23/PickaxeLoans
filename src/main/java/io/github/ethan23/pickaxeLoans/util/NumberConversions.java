package io.github.ethan23.pickaxeLoans.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NumberConversions {

    private static List<Double> balanceAmounts = List.of(1000000000000.0, 1000000000.0, 1000000.0, 1000.0);
    private static List<String> balanceSuffix = List.of("t", "b", "m", "k");

    public static String timeFromMillis(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;

        String timeString;
        if (hours > 0) {
            timeString = String.format("%dh %02dm %02ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            timeString = String.format("%dm %02ds", minutes, seconds);
        } else {
            timeString = String.format("%ds", seconds);
        }
        return timeString;
    }

    public static double round(double value) {
        int places = 2;
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.DOWN);
        return bd.doubleValue();
    }

    public static boolean hasSuffix (String amount){
        String suffix = amount.substring(amount.length() - 1);
        return balanceSuffix.contains(suffix);
    }

    public static double suffixToNumber(String amount) {
        String suffix = amount.substring(amount.length() - 1);
        double number;

        try {
            number = Double.parseDouble(amount.substring(0, amount.length() - 1));
        }catch (NumberFormatException e) {
            return -1;
        }
        return balanceAmounts.get(balanceSuffix.indexOf(suffix)) * number;
    }

    public static String formattedNumberDisplay(double value){
        return new DecimalFormat("#,###.##").format(value);
    }

}
