package com.wasu.osgi.model.hgu01.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 字符串通用处理类
 *
 * @author liupz
 */
public class StringUtil {
    private static final DecimalFormat DECIMAL_FORMAT;

    /**
     * <p>The maximum size to which the padding constant(s) can expand.</p>
     */
    private static final int PAD_LIMIT = 8192;

    final private static char COMMA = ',';

    final private static char ESCAPE_CHAR = '\\';

    static {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);
        DECIMAL_FORMAT = (DecimalFormat) numberFormat;
        DECIMAL_FORMAT.applyPattern("#.##");
    }

    /**
     * Split a string using the default separator
     *
     * @param str a string that may have escaped separator
     * @return an array of strings
     */
    public static String[] split(String str) {
        return split(str, ESCAPE_CHAR, COMMA);
    }

    /**
     * Split a string using the given separator
     *
     * @param str        a string that may have escaped separator
     * @param escapeChar a char that be used to escape the separator
     * @param separator  a separator char
     * @return an array of strings
     */
    public static String[] split(
            String str, char escapeChar, char separator) {
        if (str == null) {
            return null;
        }
        ArrayList<String> strList = new ArrayList<String>();
        StringBuilder split = new StringBuilder();
        int index = 0;
        while ((index = findNext(str, separator, escapeChar, index, split)) >= 0) {
            ++index;
            strList.add(split.toString());
            split.setLength(0);
        }
        strList.add(split.toString());
        // remove trailing empty split(s)
        int last = strList.size();
        while (--last >= 0 && "".equals(strList.get(last))) {
            strList.remove(last);
        }
        return strList.toArray(new String[strList.size()]);
    }

    /**
     * Finds the first occurrence of the separator character ignoring the escaped
     * separators starting from the index. Note the substring between the index
     * and the position of the separator is passed.
     *
     * @param str        the source string
     * @param separator  the character to find
     * @param escapeChar character used to escape
     * @param start      from where to search
     * @param split      used to pass back the extracted string
     */

    private static int findNext(String str, char separator, char escapeChar,
                                int start, StringBuilder split) {
        int numPreEscapes = 0;
        for (int i = start; i < str.length(); i++) {
            char curChar = str.charAt(i);
            if (numPreEscapes == 0 && curChar == separator) {
                return i;
            } else {
                split.append(curChar);
                numPreEscapes = (curChar == escapeChar)
                        ? (++numPreEscapes) % 2
                        : 0;
            }
        }
        return -1;
    }

    public static boolean checkBirthDay(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        boolean result = true;
        Date date1 = null;
        try {
            date1 = format.parse(date);
        } catch (ParseException e) {
            result = false;
        }
        if (date1 != null) {
            // 获取当前日期的毫秒数
            long currentTime = System.currentTimeMillis();

            // 获取生日的毫秒数
            long birthTime = date1.getTime();

            Calendar calendar = Calendar.getInstance();
            Calendar birth = Calendar.getInstance();
            birth.setTime(date1);
            if (calendar.get(Calendar.YEAR) - birth.get(Calendar.YEAR) > 120) {
                return false;
            }
            // 如果当前时间小于生日，生日不合法。反之合法
            if (birthTime > currentTime) {
                return false;
            }
        }

        return result;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean notEmpty(String str) {
        return str != null && !str.isEmpty();
    }

    public static String toString(List<?> longList) {
        if (longList == null || longList.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object aLong : longList) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(aLong);
        }
        return sb.toString();
    }

    public static String join(char sp, List<?> parameters) {
        StringBuilder sb = new StringBuilder();
        for (Object i : parameters) {
            if (i == null) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(sp);
            }
            sb.append("'").append(i).append("'");
        }
        return sb.toString();
    }

    public static String leftPad(String str, int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (isEmpty(padStr)) {
            padStr = " ";
        }
        int padLen = padStr.length();
        int strLen = str.length();
        int pads = size - strLen;
        if (pads <= 0) {
            return str;
        }
        if (padLen == 1 && pads <= PAD_LIMIT) {
            return leftPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return padStr.concat(str);
        } else if (pads < padLen) {
            return padStr.substring(0, pads).concat(str);
        } else {
            char[] padding = new char[pads];
            char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return new String(padding).concat(str);
        }
    }

    public static String leftPad(String str, int size, char padChar) {
        if (str == null) {
            return null;
        }
        int pads = size - str.length();
        if (pads <= 0) {
            return str;
        }
        if (pads > PAD_LIMIT) {
            return leftPad(str, size, String.valueOf(padChar));
        }
        return padding(pads, padChar).concat(str);
    }

    private static String padding(int repeat, char padChar) throws IndexOutOfBoundsException {
        if (repeat < 0) {
            throw new IndexOutOfBoundsException("Cannot pad a negative amount: " + repeat);
        }
        final char[] buf = new char[repeat];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = padChar;
        }
        return new String(buf);
    }

    /**
     * 判断字符串是否为数字
     *
     * @param str 目标字符串
     * @return 是与否
     */
    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        } else {
            int sz = str.length();

            for (int i = 0; i < sz; ++i) {
                if (!Character.isDigit(str.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * 判断字符串是否为数字,包含负数、小数
     *
     * @param str 目标字符串
     * @return 是与否
     */
    public static boolean isNumericExtend(String str) {
        if (str == null) {
            return false;
        }
        return str.matches("-?[0-9]+.?[0-9]*");
    }

    public static String randomSix() {
        String a = "0123456789";
        StringBuilder valCode = new StringBuilder();
        char[] rands = new char[6];

        int i;
        for (i = 0; i < rands.length; ++i) {
            int rand = (int) (Math.random() * (double) a.length());
            rands[i] = a.charAt(rand);
        }

        for (i = 0; i < rands.length; ++i) {
            valCode.append(rands[i]);
        }

        return valCode.toString();
    }

    public static boolean checkEmail(String email) {
        return email.matches("[\\w\\.\\-]+@([\\w\\-]+\\.)+[\\w\\-]+");
    }

    public static boolean checkTel(String str) {
        String string = notEmpty(str) ? str : "";
        long intStr = -1L;
        if ("".equals(string)) {
            return false;
        } else {
            try {
                intStr = Long.parseLong(str);
                if (intStr > 0L) {
                    if (str.length() != 11 && str.length() != 12) {
                        return false;
                    } else {
                        return str.charAt(0) == '0' || str.charAt(0) == '1';
                    }
                } else {
                    return false;
                }
            } catch (Exception var5) {
                return false;
            }
        }
    }
}

