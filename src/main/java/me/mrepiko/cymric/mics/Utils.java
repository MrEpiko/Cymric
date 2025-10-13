package me.mrepiko.cymric.mics;

import me.mrepiko.cymric.placeholders.PlaceholderMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Utils {

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String allowedChars = "QWERTYUIOPLKJHGFDSAZXCVBNMqwertyuioplkjhgfdsazxcvbnm1234567890_";

    private static final Pattern ENV_PATTERN = Pattern.compile("\\$\\{([^:}]+)(?::([^}]*))?}");
    private static final Map<String, String> DOT_ENV = new HashMap<>();

    static {
        try (Stream<String> stream = Files.lines(Paths.get(".env"))) {
            stream.map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .forEach(line -> {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            DOT_ENV.put(parts[0], parts[1]);
                        }
                    });
        } catch (IOException ignored) { }
    }

    /**
     * Generates a unique component ID by appending a random 32-character string to the provided component ID.
     *
     * @param componentId The base component ID to which the random string will be appended.
     * @return String Unique component ID.
     */
    public static String generateUniqueComponentId(String componentId) {
        StringBuilder sb = new StringBuilder(32);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < 32; i++) {
            int index = random.nextInt(BASE62.length());
            sb.append(BASE62.charAt(index));
        }
        return componentId + ":" + sb;
    }

    /**
     * Sanitizes a component ID by removing everything after the first colon.
     *
     * @param componentId The component ID to sanitize.
     * @return String Sanitized component ID.
     */
    public static String getSanitizedComponentId(String componentId) {
        return componentId.split(":")[0];
    }

    /**
     * Resolves environment variables in the input string. Environment variables are denoted by the syntax ${VAR_NAME} or ${VAR_NAME:default_value}.
     * If the environment variable is not set, the default value (if provided) will be used.
     *
     * @param input The input string containing environment variable placeholders.
     * @return The input string with environment variables resolved.
     */
    public static String resolveEnv(String input) {
        if (input == null) return null;

        Matcher matcher = ENV_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            String defaultValue = matcher.group(2);

            String value = DOT_ENV.getOrDefault(key, System.getenv(key));
            if (value == null) {
                value = defaultValue;
            }

            matcher.appendReplacement(sb, value != null ? Matcher.quoteReplacement(value) : "");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Checks if a file exists at the specified file path.
     *
     * @param filePath The path to the file to check.
     * @return boolean True if the file exists, false otherwise.
     */
    public static boolean isFileExists(String filePath) {
        return (new File(filePath).exists());
    }

    /**
     * Returns the current ThreadLocalRandom instance.
     *
     * @return ThreadLocalRandom The current ThreadLocalRandom instance.
     */
    public static ThreadLocalRandom getRandom() {
        return ThreadLocalRandom.current();
    }

    /**
     * Truncates a string to a specified maximum length, appending a truncation indicator if the string exceeds the maximum length.
     *
     * @param input               The input string to truncate.
     * @param maxLength           The maximum length of the string after truncation.
     * @param truncationIndicator The string to append if truncation occurs.
     * @return String The truncated string, or the original string if it does not exceed the maximum length.
     */
    public static String truncateString(@NotNull String input, int maxLength, @NotNull String truncationIndicator) {
        if (input.length() <= maxLength) {
            return input;
        }
        if (maxLength <= truncationIndicator.length()) {
            return truncationIndicator;
        }
        return input.substring(0, maxLength - truncationIndicator.length()) + truncationIndicator;
    }

    /**
     * Returns the current time in seconds since the epoch (January 1, 1970).
     *
     * @return long The current time in seconds.
     */
    public static long getCurrentTimeSeconds() {
        return System.currentTimeMillis() / 1000L;
    }

    /**
     * Sorts a map by its keys in ascending order and returns a new LinkedHashMap with the sorted entries.
     *
     * @param map The map to sort.
     * @return Map<K, V> A new LinkedHashMap containing the sorted entries of the original map.
     */
    public static <K extends Comparable<? super K>, V> Map<K, V> sortByKey(@NotNull Map<K, V> map) {
        Map<K, V> sortedMap = new LinkedHashMap<>();
        map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEachOrdered(entry -> sortedMap.put(entry.getKey(), entry.getValue()));
        return sortedMap;
    }

    /**
     * Returns a File object for the specified file path, creating it if it does not exist.
     *
     * @param filePath The path to the file or directory.
     * @param dir      Whether the path is for a directory (true) or a file (false).
     * @return File The File object for the specified path.
     * @throws IOException If an I/O error occurs while creating the file or directory.
     */
    public static File getAndCreateIfNotExists(String filePath, boolean dir) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            if (dir) {
                file.mkdirs();
            } else {
                file.createNewFile();
            }
        } else {
            if (dir && !file.isDirectory()) {
                throw new IOException("File at " + filePath + " already exists and is not a directory.");
            } else if (!dir && file.isDirectory()) {
                throw new IOException("Directory at " + filePath + " already exists and is not a file.");
            }
        }
        return file;
    }

    /**
     * Checks if the given class is a primitive type or a common wrapper type (String, Integer, Long, Double, Float, Boolean).
     *
     * @param type The class to check.
     * @return True if the class is a primitive type or a common wrapper type, false otherwise.
     */
    public static boolean isPrimitiveOrString(Class<?> type) {
        return type.isPrimitive()
                || type == String.class
                || type == Integer.class
                || type == Long.class
                || type == Double.class
                || type == Float.class
                || type == Boolean.class;
    }

    /**
     * Checks if the given class is an enum type.
     *
     * @param type The class to check.
     * @return True if the class is an enum type, false otherwise.
     */
    public static boolean isEnum(Class<?> type) {
        return type.isEnum();
    }

    /**
     * Applies placeholders to a string using the provided PlaceholderMap.
     * If the map is null, it returns the original text.
     *
     * @param map  The PlaceholderMap to use for applying placeholders.
     * @param text The text to apply placeholders to.
     * @return The text with placeholders applied, or the original text if the map is null.
     */
    public static String applyPlaceholders(@Nullable PlaceholderMap map, @Nullable String text) {
        if (text == null) {
            return "";
        }
        return (map != null) ? map.applyPlaceholders(text) : text;
    }

    /**
     * Applies placeholders to each item in a list of strings using the provided PlaceholderMap.
     *
     * @param map   The PlaceholderMap to use for applying placeholders.
     * @param items The list of strings to apply placeholders to.
     */
    public static void applyPlaceholders(@Nullable PlaceholderMap map, @Nullable List<String> items) {
        if (items == null) {
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            if (item == null) {
                items.set(i, "");
                continue;
            }
            if (map != null) {
                items.set(i, map.applyPlaceholders(item));
            }
        }
    }

    /**
     * Returns the name of a month based on its number (1-12).
     *
     * @param monthNumber The number of the month (1 for January, 2 for February, etc.).
     * @return The name of the month corresponding to the given number, or "Unknown" if the number is not valid (not between 1 and 12).
     */
    public static String getMonthByNumber(int monthNumber) {
        switch (monthNumber) {
            case 1 -> {
                return "January";
            }
            case 2 -> {
                return "February";
            }
            case 3 -> {
                return "March";
            }
            case 4 -> {
                return "April";
            }
            case 5 -> {
                return "May";
            }
            case 6 -> {
                return "June";
            }
            case 7 -> {
                return "July";
            }
            case 8 -> {
                return "August";
            }
            case 9 -> {
                return "September";
            }
            case 10 -> {
                return "October";
            }
            case 11 -> {
                return "November";
            }
            case 12 -> {
                return "December";
            }
            default -> {
                return "Unknown";
            }
        }
    }

    /**
     * Returns an adapted emoji string by removing angle brackets.
     *
     * @param rawEmoji The raw emoji string, which may contain angle brackets.
     * @return The adapted emoji string without angle brackets.
     */
    public static String getAdaptedEmoji(String rawEmoji) {
        return rawEmoji.replace("<", "").replace(">", "");
    }

    public static String formatToTimeString(int amountSeconds) {
        return formatToTimeString(amountSeconds, false, false);
    }

    public static String formatToTimeString(int secondsAmount, boolean boldValues) {
        return formatToTimeString(secondsAmount, boldValues, false);
    }

    /**
     * Formats a given amount of seconds into a human-readable time string.
     *
     * @param secondsAmount The amount of seconds to format.
     * @param boldValues    Whether to format the values in bold.
     * @param shortValues   Whether to use short forms for time units (e.g., "d" for days, "h" for hours).
     * @return A formatted string representing the time in days, hours, minutes, and seconds.
     */
    public static String formatToTimeString(int secondsAmount, boolean boldValues, boolean shortValues) {
        String secondStr = (shortValues) ? "sec" : " seconds";
        String singularSecondStr = (shortValues) ? "sec" : " second";
        if (secondsAmount == 0) return (boldValues) ? "**0" + secondStr + "**" : "0" + secondStr;

        int days = secondsAmount / (24 * 3600);
        int hours = (secondsAmount % (24 * 3600)) / 3600;
        int minutes = ((secondsAmount % (24 * 3600)) % 3600) / 60;
        int seconds = ((secondsAmount % (24 * 3600)) % 3600) % 60;

        String dayStr = (shortValues) ? "d" : " days";
        String hourStr = (shortValues) ? "h" : " hours";
        String minuteStr = (shortValues) ? "min" : " minutes";

        String singularDayStr = (shortValues) ? "d" : " day";
        String singularHourStr = (shortValues) ? "h" : " hour";
        String singularMinuteStr = (shortValues) ? "min" : " minute";

        if (days == 0) {
            if (hours == 0) {
                if (minutes == 0) {
                    if (seconds == 1) return (boldValues) ? "**1" + singularSecondStr + "**" : "1" + singularSecondStr;
                    else return String.format((boldValues) ? "**%d" + secondStr + "**" : "%d" + secondStr, seconds);
                } else {
                    if (seconds == 1)
                        return String.format((boldValues) ? "**%d" + minuteStr + "** and **1" + singularSecondStr + "**" : "%d" + minuteStr + " and 1" + singularSecondStr, minutes);
                    else
                        return String.format((boldValues) ? "**%d" + minuteStr + "** and **%d" + secondStr + "**" : "%d" + minuteStr + " and %d" + secondStr, minutes, seconds);
                }
            } else {
                if (minutes == 0) {
                    if (seconds == 1)
                        return String.format((boldValues) ? "**%d" + hourStr + "** and **1" + singularSecondStr + "**" : "%d" + hourStr + " and 1" + singularSecondStr, hours);
                    else
                        return String.format((boldValues) ? "**%d" + hourStr + "** and **%d" + secondStr + "**" : "%d" + hourStr + " and %d" + secondStr, hours, seconds);
                } else {
                    if (seconds == 1)
                        return String.format((boldValues) ? "**%d" + hourStr + "**, **%d" + minuteStr + "**, and **1" + singularSecondStr + "**" : "%d" + hourStr + ", %d" + minuteStr + ", and 1" + singularSecondStr, hours, minutes);
                    else
                        return String.format((boldValues) ? "**%d" + hourStr + "**, **%d" + minuteStr + "** and **%d" + secondStr + "**" : "%d" + hourStr + ", %d" + minuteStr + " and %d" + secondStr, hours, minutes, seconds);
                }
            }
        } else {
            if (days == 1) {
                if (hours == 0 && minutes == 0 && seconds == 0)
                    return (boldValues) ? "**1" + singularDayStr + "**" : "1" + singularDayStr;
                else if (hours == 1 && minutes == 0 && seconds == 0)
                    return (boldValues) ? "**1" + singularDayStr + "** and **1" + singularHourStr + "**" : "1" + singularDayStr + " and 1" + singularHourStr;
                else if (hours == 0 && minutes == 1 && seconds == 0)
                    return (boldValues) ? "**1" + singularDayStr + "** and **1" + singularMinuteStr + "**" : "1" + singularDayStr + " and 1" + singularMinuteStr;
                else if (hours == 0 && minutes == 0 && seconds == 1)
                    return (boldValues) ? "**1" + singularDayStr + "** and **1" + singularSecondStr + "**" : "1" + singularDayStr + " and 1" + singularSecondStr;
                else if (hours == 1 && minutes == 1 && seconds == 0)
                    return (boldValues) ? "**1" + singularDayStr + "**, **1" + singularHourStr + "** and **1" + singularMinuteStr + "**" : "1" + singularDayStr + ", 1" + singularHourStr + " and 1" + singularMinuteStr;
                else if (hours == 1 && minutes == 0 && seconds == 1)
                    return (boldValues) ? "**1" + singularDayStr + "**, **1" + singularHourStr + "** and **1" + singularSecondStr + "**" : "1" + singularDayStr + ", 1" + singularHourStr + " and 1" + singularSecondStr;
                else if (hours == 0 && minutes == 1 && seconds == 1)
                    return (boldValues) ? "**1" + singularDayStr + "**, **1" + singularMinuteStr + "** and **1" + singularSecondStr + "**" : "1" + singularDayStr + ", 1" + singularMinuteStr + " and 1" + singularSecondStr;
                else
                    return String.format((boldValues) ? "**1" + singularDayStr + "**, **%d" + hourStr + "**, **%d" + minuteStr + "** and **%d" + secondStr + "**" : "1" + singularDayStr + ", %d" + hourStr + ", %d" + minuteStr + " and %d" + secondStr, hours, minutes, seconds);
            } else {
                if (hours == 0 && minutes == 0 && seconds == 0)
                    return String.format((boldValues) ? "**%d" + dayStr + "**" : "%d" + dayStr, days);
                else if (hours == 1 && minutes == 0 && seconds == 0)
                    return String.format((boldValues) ? "**%d" + dayStr + "** and **1" + singularHourStr + "**" : "%d" + dayStr + " and 1" + singularHourStr, days);
                else if (hours == 0 && minutes == 1 && seconds == 0)
                    return String.format((boldValues) ? "**%d" + dayStr + "** and **1" + singularMinuteStr + "**" : "%d" + dayStr + " and 1" + singularMinuteStr, days);
                else if (hours == 0 && minutes == 0 && seconds == 1)
                    return String.format((boldValues) ? "**%d" + dayStr + "** and **1" + singularSecondStr + "**" : "%d" + dayStr + " and 1" + singularSecondStr, days);
                else if (hours == 1 && minutes == 1 && seconds == 0)
                    return String.format((boldValues) ? "**%d" + dayStr + "**, **1" + singularHourStr + "** and **1" + singularMinuteStr + "**" : "%d" + dayStr + ", 1" + singularHourStr + " and 1" + singularMinuteStr, days);
                else if (hours == 1 && minutes == 0 && seconds == 1)
                    return String.format((boldValues) ? "**%d" + dayStr + "**, **1" + singularHourStr + "** and **1" + singularSecondStr + "**" : "%d" + dayStr + ", 1" + singularHourStr + " and 1" + singularSecondStr, days);
                else if (hours == 0 && minutes == 1 && seconds == 1)
                    return String.format((boldValues) ? "**%d" + dayStr + "**, **1" + singularMinuteStr + "** and **1" + singularSecondStr + "**" : "%d" + dayStr + ", 1" + singularMinuteStr + " and 1" + singularSecondStr, days);
                else
                    return String.format((boldValues) ? "**%d" + dayStr + "**, **%d" + hourStr + "**, **%d" + minuteStr + "** and **%d" + secondStr + "**" : "%d" + dayStr + ", %d" + hourStr + ", %d" + minuteStr + " and %d" + secondStr, days, hours, minutes, seconds);
            }
        }
    }

    /**
     * Converts a string representation of time (e.g., "2d 3h 4m 5s") into total seconds.
     *
     * @param input The string input representing time.
     * @return The total number of seconds represented by the input string.
     */
    public static long convertToSeconds(String input) {
        int totalSeconds = 0;

        for (String part : input.split(" ")) {
            char unit = part.charAt(part.length() - 1);
            int value;
            try {
                value = Integer.parseInt(part.substring(0, part.length() - 1));
            } catch (NumberFormatException e) {
                continue;
            }
            switch (unit) {
                case 'd' -> totalSeconds += value * 24 * 60 * 60;
                case 'h' -> totalSeconds += value * 60 * 60;
                case 'm' -> totalSeconds += value * 60;
                case 's' -> totalSeconds += value;
            }
        }
        return totalSeconds;
    }

    /**
     * Converts a Unix timestamp in seconds to an OffsetDateTime object in UTC.
     *
     * @param secondsTimestamp The Unix timestamp in seconds.
     * @return An OffsetDateTime object representing the timestamp in UTC.
     */
    public static OffsetDateTime convertSecondsTimestampToOffset(long secondsTimestamp) {
        Instant instant = Instant.ofEpochSecond(secondsTimestamp);
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    /**
     * Formats a list of objects into a string, separating them with the specified separator.
     *
     * @param list      The list of objects to format.
     * @param separator The separator to use between the objects.
     * @return A formatted string containing the objects separated by the specified separator.
     */
    public static String formatList(List<Object> list, String separator) {
        StringBuilder output = new StringBuilder();
        for (Object s : list) output.append(s.toString()).append(separator);
        if (!output.isEmpty()) {
            output.setLength(output.length() - separator.length());
        }
        return output.toString().trim();
    }

    /**
     * Formats a BigDecimal value into a human-readable string with appropriate suffixes (e.g., k, Million, Billion).
     *
     * @param value The BigDecimal value to format.
     * @return A formatted string representing the value with appropriate suffixes.
     */
    public static String formatBigDecimal(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        String[] suffixes = {"", "k", " Million", " Billion", " Trillion", " Quad"};
        int index = 0;
        BigDecimal divisor = new BigDecimal("1000");
        while (value.compareTo(divisor) >= 0 && index < suffixes.length - 1) {
            value = value.divide(divisor);
            index++;
        }
        String formattedValue = value.toPlainString();

        if (index == 0) {
            return formattedValue;
        }

        DecimalFormat df = new DecimalFormat("#,###.#");
        return df.format(new BigDecimal(formattedValue)) + suffixes[index];
    }

    /**
     * Formats a BigDecimal value with commas as thousands separators.
     *
     * @param value The BigDecimal value to format.
     * @return A formatted string representing the value with commas as thousands separators.
     */
    public static String formatCommas(BigDecimal value) {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ROOT);
        decimalFormat.applyPattern("#,###.##");
        return decimalFormat.format(value);
    }

    /**
     * Capitalizes the first letter of every word in a string.
     *
     * @param input The input string to capitalize.
     * @return A new string with the first letter of each word capitalized.
     */
    public static String capitalizeEveryWord(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : input.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }

        return result.toString();
    }

    /**
     * Removes all non-alphabetic characters from a string, keeping only letters and digits.
     *
     * @param input The input string to filter.
     * @return A new string containing only alphabetic characters and digits.
     */
    public static String keepAlphabetic(String input) {
        StringBuilder output = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (allowedChars.contains(String.valueOf(c))) output.append(c);
        }
        return output.toString();
    }

    /**
     * Unescapes HTML entities in a string, converting them to their corresponding characters.
     *
     * @param input The input string containing HTML entities.
     * @return A new string with HTML entities unescaped.
     */
    public static String unescapeHtml3(final String input) {
        StringWriter writer = null;
        int len = input.length();
        int i = 1;
        int st = 0;
        while (true) {
            // Look for '&'
            while (i < len && input.charAt(i - 1) != '&')
                i++;
            if (i >= len)
                break;

            // Found '&', look for ';'
            int j = i;
            while (j < len && j < i + MAX_ESCAPE + 1 && input.charAt(j) != ';')
                j++;
            if (j == len || j < i + MIN_ESCAPE || j == i + MAX_ESCAPE + 1) {
                i++;
                continue;
            }

            // Found escape
            if (input.charAt(i) == '#') {
                // Numeric escape
                int k = i + 1;
                int radix = 10;

                final char firstChar = input.charAt(k);
                if (firstChar == 'x' || firstChar == 'X') {
                    k++;
                    radix = 16;
                }

                try {
                    int entityValue = Integer.parseInt(input.substring(k, j), radix);

                    if (writer == null)
                        writer = new StringWriter(input.length());
                    writer.append(input.substring(st, i - 1));

                    if (entityValue > 0xFFFF) {
                        final char[] chrs = Character.toChars(entityValue);
                        writer.write(chrs[0]);
                        writer.write(chrs[1]);
                    } else {
                        writer.write(entityValue);
                    }

                } catch (NumberFormatException ex) {
                    i++;
                    continue;
                }
            } else {
                // Named escape
                CharSequence value = lookupMap.get(input.substring(i, j));
                if (value == null) {
                    i++;
                    continue;
                }

                if (writer == null)
                    writer = new StringWriter(input.length());
                writer.append(input.substring(st, i - 1));

                writer.append(value);
            }

            // Skip escape
            st = j + 1;
            i = st;
        }

        if (writer != null) {
            writer.append(input.substring(st, len));
            return writer.toString();
        }
        return input;
    }

    private static final String[][] ESCAPES = {
            {"\"", "quot"}, // " - double-quote
            {"&", "amp"}, // & - ampersand
            {"<", "lt"}, // < - less-than
            {">", "gt"}, // > - greater-than

            // Mapping to escape ISO-8859-1 characters to their named HTML 3.x equivalents.
            {"\u00A0", "nbsp"},   // Non-breaking space
            {"\u00A1", "iexcl"},  // Inverted exclamation mark
            {"\u00A2", "cent"},   // Cent sign
            {"\u00A3", "pound"},  // Pound sign
            {"\u00A4", "curren"}, // Currency sign
            {"\u00A5", "yen"},    // Yen sign = yuan sign
            {"\u00A6", "brvbar"}, // Broken bar = broken vertical bar
            {"\u00A7", "sect"},   // Section sign
            {"\u00A8", "uml"},    // Diaeresis = spacing diaeresis
            {"\u00A9", "copy"},   // © - copyright sign
            {"\u00AA", "ordf"},   // Feminine ordinal indicator
            {"\u00AB", "laquo"},  // Left-pointing double angle quotation mark = left pointing guillemet
            {"\u00AC", "not"},    // Not sign
            {"\u00AD", "shy"},    // Soft hyphen = discretionary hyphen
            {"\u00AE", "reg"},    // ® - registered trademark sign
            {"\u00AF", "macr"},   // Macron = spacing macron = overline = APL overbar
            {"\u00B0", "deg"},    // Degree sign
            {"\u00B1", "plusmn"}, // Plus-minus sign = plus-or-minus sign
            {"\u00B2", "sup2"},   // Superscript two = superscript digit two = squared
            {"\u00B3", "sup3"},   // Superscript three = superscript digit three = cubed
            {"\u00B4", "acute"},  // Acute accent = spacing acute
            {"\u00B5", "micro"},  // Micro sign
            {"\u00B6", "para"},   // Pilcrow sign = paragraph sign
            {"\u00B7", "middot"}, // Middle dot = Georgian comma = Greek middle dot
            {"\u00B8", "cedil"},  // Cedilla = spacing cedilla
            {"\u00B9", "sup1"},   // Superscript one = superscript digit one
            {"\u00BA", "ordm"},   // Masculine ordinal indicator
            {"\u00BB", "raquo"},  // Right-pointing double angle quotation mark = right pointing guillemet
            {"\u00BC", "frac14"}, // Vulgar fraction one quarter = fraction one quarter
            {"\u00BD", "frac12"}, // Vulgar fraction one half = fraction one half
            {"\u00BE", "frac34"}, // Vulgar fraction three quarters = fraction three quarters
            {"\u00BF", "iquest"}, // Inverted question mark = turned question mark
            {"\u00C0", "Agrave"}, // А - uppercase A, grave accent
            {"\u00C1", "Aacute"}, // Б - uppercase A, acute accent
            {"\u00C2", "Acirc"},  // В - uppercase A, circumflex accent
            {"\u00C3", "Atilde"}, // Г - uppercase A, tilde
            {"\u00C4", "Auml"},   // Д - uppercase A, umlaut
            {"\u00C5", "Aring"},  // Е - uppercase A, ring
            {"\u00C6", "AElig"},  // Ж - uppercase AE
            {"\u00C7", "Ccedil"}, // З - uppercase C, cedilla
            {"\u00C8", "Egrave"}, // И - uppercase E, grave accent
            {"\u00C9", "Eacute"}, // Й - uppercase E, acute accent
            {"\u00CA", "Ecirc"},  // К - uppercase E, circumflex accent
            {"\u00CB", "Euml"},   // Л - uppercase E, umlaut
            {"\u00CC", "Igrave"}, // М - uppercase I, grave accent
            {"\u00CD", "Iacute"}, // Н - uppercase I, acute accent
            {"\u00CE", "Icirc"},  // О - uppercase I, circumflex accent
            {"\u00CF", "Iuml"},   // П - uppercase I, umlaut
            {"\u00D0", "ETH"},    // Р - uppercase Eth, Icelandic
            {"\u00D1", "Ntilde"}, // С - uppercase N, tilde
            {"\u00D2", "Ograve"}, // Т - uppercase O, grave accent
            {"\u00D3", "Oacute"}, // У - uppercase O, acute accent
            {"\u00D4", "Ocirc"},  // Ф - uppercase O, circumflex accent
            {"\u00D5", "Otilde"}, // Х - uppercase O, tilde
            {"\u00D6", "Ouml"},   // Ц - uppercase O, umlaut
            {"\u00D7", "times"},  // Multiplication sign
            {"\u00D8", "Oslash"}, // Ш - uppercase O, slash
            {"\u00D9", "Ugrave"}, // Щ - uppercase U, grave accent
            {"\u00DA", "Uacute"}, // Ъ - uppercase U, acute accent
            {"\u00DB", "Ucirc"},  // Ы - uppercase U, circumflex accent
            {"\u00DC", "Uuml"},   // Ь - uppercase U, umlaut
            {"\u00DD", "Yacute"}, // Э - uppercase Y, acute accent
            {"\u00DE", "THORN"},  // Ю - uppercase THORN, Icelandic
            {"\u00DF", "szlig"},  // Я - lowercase sharps, German
            {"\u00E0", "agrave"}, // а - lowercase a, grave accent
            {"\u00E1", "aacute"}, // б - lowercase a, acute accent
            {"\u00E2", "acirc"},  // в - lowercase a, circumflex accent
            {"\u00E3", "atilde"}, // г - lowercase a, tilde
            {"\u00E4", "auml"},   // д - lowercase a, umlaut
            {"\u00E5", "aring"},  // е - lowercase a, ring
            {"\u00E6", "aelig"},  // ж - lowercase ae
            {"\u00E7", "ccedil"}, // з - lowercase c, cedilla
            {"\u00E8", "egrave"}, // и - lowercase e, grave accent
            {"\u00E9", "eacute"}, // й - lowercase e, acute accent
            {"\u00EA", "ecirc"},  // к - lowercase e, circumflex accent
            {"\u00EB", "euml"},   // л - lowercase e, umlaut
            {"\u00EC", "igrave"}, // м - lowercase i, grave accent
            {"\u00ED", "iacute"}, // н - lowercase i, acute accent
            {"\u00EE", "icirc"},  // о - lowercase i, circumflex accent
            {"\u00EF", "iuml"},   // п - lowercase i, umlaut
            {"\u00F0", "eth"},    // р - lowercase eth, Icelandic
            {"\u00F1", "ntilde"}, // с - lowercase n, tilde
            {"\u00F2", "ograve"}, // т - lowercase o, grave accent
            {"\u00F3", "oacute"}, // у - lowercase o, acute accent
            {"\u00F4", "ocirc"},  // ф - lowercase o, circumflex accent
            {"\u00F5", "otilde"}, // х - lowercase o, tilde
            {"\u00F6", "ouml"},   // ц - lowercase o, umlaut
            {"\u00F7", "divide"}, // Division sign
            {"\u00F8", "oslash"}, // ш - lowercase o, slash
            {"\u00F9", "ugrave"}, // щ - lowercase u, grave accent
            {"\u00FA", "uacute"}, // ъ - lowercase u, acute accent
            {"\u00FB", "ucirc"},  // ы - lowercase u, circumflex accent
            {"\u00FC", "uuml"},   // ь - lowercase u, umlaut
            {"\u00FD", "yacute"}, // э - lowercase y, acute accent
            {"\u00FE", "thorn"},  // ю - lowercase thorn, Icelandic
            {"\u00FF", "yuml"},   // я - lowercase y, umlaut
    };

    private static final int MIN_ESCAPE = 2;
    private static final int MAX_ESCAPE = 6;

    private static final HashMap<String, CharSequence> lookupMap;

    static {
        lookupMap = new HashMap<>();
        for (final CharSequence[] seq : ESCAPES)
            lookupMap.put(seq[1].toString(), seq[0]);
    }

}
