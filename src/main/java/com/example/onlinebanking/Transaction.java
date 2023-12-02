package com.example.onlinebanking;

public interface Transaction {
    enum Transact {
        SENT,
        FAILED,
        CANCELLED,
        TRANSFER,
        DEPOSITED,
        WITHDRAWN
    }


    void transferFunds ();
    void deposit();
}
