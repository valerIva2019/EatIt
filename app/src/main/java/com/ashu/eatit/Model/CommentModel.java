package com.ashu.eatit.Model;

import java.util.Map;

public class CommentModel {
    private float ratingValue;
    private String name, comment, uid;
    private Map<String, Object> serverTimeStamp;

    public CommentModel() {
    }

    public float getRatingValue() {
        return ratingValue;
    }

    public void setRatingValue(float ratingValue) {
        this.ratingValue = ratingValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Map<String, Object> getServerTimeStamp() {
        return serverTimeStamp;
    }

    public void setServerTimeStamp(Map<String, Object> serverTimeStamp) {
        this.serverTimeStamp = serverTimeStamp;
    }
}
