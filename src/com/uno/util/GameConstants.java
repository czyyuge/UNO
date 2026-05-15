package com.uno.util;

public class GameConstants {
    public static final int DEFAULT_PORT = 8888;
    public static final int MAX_PLAYERS = 4;
    public static final int INITIAL_CARDS = 7;

    public static final String COLOR_RED = "RED";
    public static final String COLOR_GREEN = "GREEN";
    public static final String COLOR_BLUE = "BLUE";
    public static final String COLOR_YELLOW = "YELLOW";
    public static final String COLOR_WILD = "WILD";

    public static final String[] COLORS = {COLOR_RED, COLOR_GREEN, COLOR_BLUE, COLOR_YELLOW};

    public static final String TYPE_NUMBER = "NUMBER";
    public static final String TYPE_SKIP = "SKIP";
    public static final String TYPE_REVERSE = "REVERSE";
    public static final String TYPE_DRAW_TWO = "DRAW_TWO";
    public static final String TYPE_WILD = "WILD";
    public static final String TYPE_WILD_DRAW_FOUR = "WILD_DRAW_FOUR";

    public static final String[] SPECIAL_TYPES = {TYPE_SKIP, TYPE_REVERSE, TYPE_DRAW_TWO};

    public static boolean isValidColor(String color) {
        return COLOR_RED.equals(color) || COLOR_GREEN.equals(color)
                || COLOR_BLUE.equals(color) || COLOR_YELLOW.equals(color);
    }

    public static boolean isWildCard(String type) {
        return TYPE_WILD.equals(type) || TYPE_WILD_DRAW_FOUR.equals(type);
    }

    public static boolean isActionCard(String type) {
        return TYPE_SKIP.equals(type) || TYPE_REVERSE.equals(type)
                || TYPE_DRAW_TWO.equals(type) || TYPE_WILD_DRAW_FOUR.equals(type);
    }
}
