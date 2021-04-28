package com.neostra.util;

import android.support.annotation.NonNull;

/**
 * @author njmsir
 * Created by njmsir on 2019/5/23.
 */
public class Utils {

    /**
     * 16进制转ASCII
     */
    public static String toStringHex1(@NonNull String s) {
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "ASCII");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

    public static int getHexHightBit(int num) {
        return (num & 0xff00) >> 8;
    }


    public static int getHexLowBit(int num) {
        return num & 0xff;
    }
}
