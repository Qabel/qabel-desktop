package de.qabel.desktop.daemon.drop;

import com.google.gson.Gson;

import java.io.Serializable;

public class TextMessage implements Serializable {
    private static final long serialVersionUID = 250166272283694134L;
    private String msg;

    public TextMessage(String msg) {
        this.msg = msg;
    }

    public String getText() {
        return msg;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static TextMessage fromJson(String json) {
        return new Gson().fromJson(json, TextMessage.class);
    }
}
