package com.mclr.mini.recordatorios;

/**
 * Created by mini on 22/05/16.
 */
public class Recordatorio {
    private int mId;
    private String mContent;
    private int mImportant;

    public Recordatorio(int id, String content, int important) {
        mId = id;
        mImportant = important;
        mContent = content;
    }
    public int getId() {
        return mId;
    }
    public void setId(int id) {
        mId = id;
    }
    public int getImportant() {
        return mImportant;
    }
    public void setImportant(int important) {
        mImportant = important;
    }
    public String getContent() {
        return mContent;
    }
    public void setContent(String content) {
        mContent = content;
    }
}
