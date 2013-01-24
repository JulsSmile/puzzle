package me.elvishew.puzzle;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class Utils {
    public static final String STRING_ONE_ZERO = "0";
    public static final String STRING_TWO_ZERO = "00";
    public static final String STRING_EMPTY = "";
    public static final String TIME_ZERO = "00:00:0";

    public static final long SIZE_1K = 1024;
    public static final long SIZE_1M = 1024 * SIZE_1K;
    public static final long SIZE_1G = 1024 * SIZE_1M;

    public static final String UNIT_B = "B";
    public static final String UNIT_K = "K";
    public static final String UNIT_M = "M";
    public static final String UNIT_G = "G";

    public static int level2Square(int level) {
        if (level < Constants.MIN_GAME_LEVEL || level > Constants.MAX_GAME_LEVEL) {
            return Constants.MIN_GAME_SQUARE;
        }
        return level - Constants.MIN_GAME_LEVEL + Constants.MIN_GAME_SQUARE;
    }

    public static String long2Time(long time) {
//        time /= 100; already divided.
        long mils = time % 10;
        String strMil = String.valueOf(mils);

        time /= 10;

        long seconds = time % 60;
        String strSec = long2XXString(seconds);

        long minutes = time / 60;
        String strMin = long2XXString(minutes);


        return strMin + ":" + strSec + ":" + strMil;
    }

    private static String long2XXString(long in) {
        return (in < 10 ? STRING_ONE_ZERO : STRING_EMPTY) + String.valueOf(in);
    }

    /*private static String long2XXXString(long in) {
        String preZero;
        if (in < 10) {
            preZero = STRING_TWO_ZERO; 
        } else if (in < 100) {
            preZero = STRING_ONE_ZERO;
        } else {
            preZero = STRING_EMPTY;
        }
        return preZero + String.valueOf(in);
    }*/

    public static String long2Date(long date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
        
    }

    public static int score2Star(int score) {
        // TODO make a rule to transfer.
        return (score + 999) / 1000;
    }

    public static int timeSteps2Score(int level, long time, int steps) {
        int timeScore = 0;
        int stepsScore = 0;
        switch (level) {
            case 0:
                timeScore = (int) (1 / (time / 5600000d + 0.000125d));
                stepsScore = (int) (1 / (steps / 420000d+ 0.0005d));
                break;
            case 1:
                timeScore = (int) (1 / (time / 16800000d + 0.000125d));
                stepsScore = (int) (1 / (steps / 1260000d+ 0.0005d));
                break;
            case 2:
                timeScore = (int) (1 / (time / 50400000d + 0.000125d));
                stepsScore = (int) (1 / (steps / 3780000d+ 0.0005d));
                break;
            default : {
                //TODO error
                break;
            }
        }
        return timeScore + stepsScore;
    }

    /**
     * Formet a size to <b>0B</b>, <b>1.23MB</b> etc.
     * 
     * @param size the size to be formated
     * @return the formated string
     */
    public static String formetFileSize(long size) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString;
        if (size < SIZE_1K) {
            if (size == 0) {
                fileSizeString = '0' + UNIT_B;
            } else {
                fileSizeString = df.format((double) size) + UNIT_B;
            }
        } else if (size < SIZE_1M) {
            fileSizeString = df.format((double) size / SIZE_1K) + UNIT_K;
        } else if (size < SIZE_1G) {
            fileSizeString = df.format((double) size / SIZE_1M) + UNIT_M;
        } else {
            fileSizeString = df.format((double) size / SIZE_1G) + UNIT_G;
        }
        return fileSizeString;
    }

}
