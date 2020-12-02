package com.ashu.eatit.Model;

public class BrainTreeTransaction {
    private boolean success;
    private Transaction transaction;

    public BrainTreeTransaction() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}
