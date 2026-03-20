package edu.upc.dmag.beaconLoaderWithCLI;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConvertDuration {
    private final static String regex = "([-+]?)P(?:([-+]?[0-9]+)Y)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)W)?(?:([-+]?[0-9]+)D)?(T(?:([-+]?[0-9]+)H)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)(?:[.,]([0-9]{0,9}))?S)?)?";
    private final static Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
    public static Duration getDuration(String input){
        final Matcher matcher = pattern.matcher(input);
        if(!matcher.matches()){
            throw new IllegalArgumentException("Invalid input: "+input);
        }
        Duration duration = Duration.ZERO;
        if (matcher.group(2) != null && !matcher.group(2).isEmpty()) {
            duration = duration.plus(Duration.ofDays(365L *Integer.parseInt(matcher.group(2))));
        }
        if (matcher.group(3) != null && !matcher.group(3).isEmpty()) {
            duration = duration.plus(Duration.ofDays(30L *Integer.parseInt(matcher.group(3))));
        }
        if (matcher.group(4) != null && !matcher.group(4).isEmpty()) {
            duration = duration.plus(Duration.ofDays(7L *Integer.parseInt(matcher.group(4))));
        }
        if (matcher.group(5) != null && !matcher.group(5).isEmpty()) {
            duration = duration.plus(Duration.ofDays(Integer.parseInt(matcher.group(5))));
        }
        if (matcher.group(7) != null && !matcher.group(7).isEmpty()) {
            duration = duration.plus(Duration.ofHours(Integer.parseInt(matcher.group(7))));
        }
        if (matcher.group(8) != null && !matcher.group(8).isEmpty()) {
            duration = duration.plus(Duration.ofMinutes(Integer.parseInt(matcher.group(8))));
        }
        if (matcher.group(9) != null && !matcher.group(9).isEmpty()) {
            duration = duration.plus(Duration.ofSeconds(Integer.parseInt(matcher.group(9))));
        }

        return duration;
    }
}
