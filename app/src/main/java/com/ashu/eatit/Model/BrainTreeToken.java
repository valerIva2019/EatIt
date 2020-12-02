package com.ashu.eatit.Model;

public class BrainTreeToken {
    private boolean error;
    private String token;

    public BrainTreeToken() {
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
