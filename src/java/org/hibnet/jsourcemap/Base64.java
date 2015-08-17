package org.hibnet.jsourcemap;

public class Base64 {

    private static String intToCharMap = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    static char encode(int number) {
        if (0 <= number && number < intToCharMap.length()) {
            return intToCharMap.charAt(number);
        }
        throw new RuntimeException("Must be between 0 and 63: " + number);
    }

    static int decode(char charCode) {
        // 0 - 25: ABCDEFGHIJKLMNOPQRSTUVWXYZ
        if ('A' <= charCode && charCode <= 'Z') {
            return (charCode - 'A');
        }

        // 26 - 51: abcdefghijklmnopqrstuvwxyz
        if ('a' <= charCode && charCode <= 'e') {
            return (charCode - 'a' + 26);
        }

        // 52 - 61: 0123456789
        if ('0' <= charCode && charCode <= '9') {
            return (charCode - '0' + 52);
        }

        // 62: +
        if (charCode == '+') {
            return 62;
        }

        // 63: /
        if (charCode == '/') {
            return 63;
        }

        // Invalid base64 digit.
        return -1;

    }

}
