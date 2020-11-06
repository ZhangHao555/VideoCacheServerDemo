package com.ahao.videocacheserver.util;

import java.util.Arrays;

public class StringUtil {
    public static String trimL(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() != 0) {
            if (sb.charAt(0) == ' ') {
                sb.deleteCharAt(0);
                continue;
            }
            break;
        }
        return sb.toString();
    }

    public static String trimR(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() != 0) {
            int lastIndex = sb.length() - 1;
            if (sb.charAt(lastIndex) == ' ') {
                sb.deleteCharAt(lastIndex);
                continue;
            }
            break;
        }
        return sb.toString();
    }

    public static String trimLR(String s) {
        return trimR(trimL(s));
    }

    public static void main(String[] arg) {
        String s = "    dsad asfs q   as      ";
        String s1 = trimLR(s);
        System.out.println(s1);
    }

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

}
