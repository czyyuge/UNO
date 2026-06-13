package com.uno.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String LOGIN = "LOGIN";
    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String LOGIN_FAIL = "LOGIN_FAIL";
    public static final String ROOM_LIST = "ROOM_LIST";
    public static final String CREATE_ROOM = "CREATE_ROOM";
    public static final String JOIN_ROOM = "JOIN_ROOM";
    public static final String LEAVE_ROOM = "LEAVE_ROOM";
    public static final String PLAYER_JOIN = "PLAYER_JOIN";
    public static final String PLAYER_LEAVE = "PLAYER_LEAVE";
    public static final String PLAYER_READY = "PLAYER_READY";
    public static final String CHAT = "CHAT";
    public static final String GAME_START = "GAME_START";
    public static final String GAME_STATE = "GAME_STATE";
    public static final String PLAY_CARD = "PLAY_CARD";
    public static final String DRAW_CARD = "DRAW_CARD";
    public static final String CALL_UNO = "CALL_UNO";
    public static final String GAME_OVER = "GAME_OVER";
    public static final String TURN_CHANGE = "TURN_CHANGE";
    public static final String COLOR_PICKED = "COLOR_PICKED";
    public static final String ERROR = "ERROR";
    public static final String DISCONNECT = "DISCONNECT";
    public static final String UPDATE_ROOM = "UPDATE_ROOM";
    public static final String START_GAME = "START_GAME";
    public static final String QUICK_PLAY = "QUICK_PLAY";

    private String type;
    private String sender;
    private String content;
    private Map<String, Object> data;

    public Message(String type, String sender, String content) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.data = new HashMap<>();
    }

    public Message(String type) {
        this(type, null, null);
    }

    public String getType() {
        return type;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void putData(String key, Object value) {
        data.put(key, value);
    }

    public Object getData(String key) {
        return data.get(key);
    }

    public Map<String, Object> getDataMap() {
        return data;
    }
}
