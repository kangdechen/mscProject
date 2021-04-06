package com.kangde.myapplication.Bean;

public class Tags {


    int tagid;

    int recId;
    String context;



    public Tags() {
        super();
        // TODO Auto-generated constructor stub
    }
    public Tags(int tagid, int recId, String context) {
        super();
        this.tagid = tagid;
        this.recId = recId;
        this.context = context;
    }
    public int getTagid() {
        return tagid;
    }
    public void setTagid(int tagid) {
        this.tagid = tagid;
    }
    public int getRecId() {
        return recId;
    }
    public void setRecId(int recId) {
        this.recId = recId;
    }
    public String getContext() {
        return context;
    }
    public void setContext(String context) {
        this.context = context;
    }
}
