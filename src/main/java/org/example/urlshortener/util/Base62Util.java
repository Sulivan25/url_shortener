package org.example.urlshortener.util;


public   class Base62Util {

    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = 62;

    public static String encode(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Value must be non-negative");
        }

        if (value == 0) {
            return String.valueOf(BASE62.charAt(0));
        }

        StringBuilder result = new StringBuilder();
        while (value > 0) {
            int remainder = (int) (value % BASE);
            result.append(BASE62.charAt(remainder));
            value /= BASE;
        }

        return result.reverse().toString();
    }

    public static long decode(String str) {
        long result = 0;
        for (char c : str.toCharArray()) {
            int index = BASE62.indexOf(c);
            if (index == -1) {
                throw new IllegalArgumentException("Invalid Base62 character: " + c);
            }
            result = result * BASE + index;
        }
        return result;
    }
}

