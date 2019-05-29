package com.chensi.box.util;

public class BoxNumberUtil {

    public static String getTimeFormatMili(int mili) {
        return getTimeFormat(mili / 1000);
    }

    public static String getTimeFormat(int sec) {
        if (sec < 3600) {
            return sec / 60 + ":" + sec % 60;
        } else {
            return sec / 3600 + ":" + ((sec % 3600) / 60);
        }
    }

    public static String getNumberFormat(long number) {
        String src = String.valueOf(number);
        StringBuilder builder = new StringBuilder();

        int count = (src.length() - 1) / 3 + 1;
        int start = 0;
        int end = src.length() % 3;
        if (end == 0) end = 3;
        for (int i = 0; i < count; i++) {
            builder.append(src.substring(start, end));
            if (i < count - 1) {
                builder.append(',');
                start = end;
                end += 3;
            }
        }

        return builder.toString();
    }

}
