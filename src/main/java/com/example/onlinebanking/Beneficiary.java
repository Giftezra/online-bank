package com.example.onlinebanking;

import java.sql.*;

/** This class represents a beneficiary from the beneficiary database.
 * */
public class Beneficiary extends User {
    private String accountName;
    private long accountNumber;
    private long sortCode;
    private double amount;
    private Timestamp timestamp;

    public Beneficiary(String accountName, long accountNumber, long sortCode, double amount) {
        this.accountName = accountName;
        this.accountNumber = accountNumber;
        this.sortCode = sortCode;
        this.amount = amount;
    }

    public Beneficiary(String accountName, long accountNumber, long sortCode) {
        this.accountName = accountName;
        this.accountNumber = accountNumber;
        this.sortCode = sortCode;
    }

    public Beneficiary(String accountName, long accountNumber,
                       long sortCode, double amount, Timestamp transactionTime,
                       Transaction.Transact type) {
        this.accountName = accountName;
        this.accountNumber = accountNumber;
        this.sortCode = sortCode;
        this.timestamp = transactionTime;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public long getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(long accountNumber) {
        this.accountNumber = accountNumber;
    }

    public long getSortCode() {
        return sortCode;
    }

    public void setSortCode(long sortCode) {
        this.sortCode = sortCode;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**This method will save the data gotten from the user field to the database of the beneficiary
     * @param accountName'
     * @param accountNumber'
     * @param sortCode'
     * @param amount'
     * */
    public void saveToBeneficiaryDatabase (int user_id, String accountName, long accountNumber,
                                           long sortCode, double amount, Timestamp transactionTime){
        String query = "INSERT INTO beneficiary (user_id,account-name, account-number, sort-code, amount, transactionTime) VALUES (?, ?, ?, ?, ?, ?)";
        /* First use a try resource to open the connection and the statement object, so it closes the connection after execution*/
        try (Connection connection = DriverManager.getConnection(DatabaseManager.USERDATABASE_URL);
              PreparedStatement statement = connection.prepareStatement(query);){
            statement.setInt(1, user_id);
            statement.setString(2, accountName);
            statement.setLong(3, accountNumber);
            statement.setLong(4, sortCode);
            statement.setDouble(5, amount);
            statement.setTimestamp(6, transactionTime);
            /* Check if the rows has been updated successfully*/
            int rows = statement.executeUpdate();
            if (rows > 0){
                System.out.println("Inserted data correctly inside the beneficiary database");
            }else{
                System.out.println("beneficiary could not be updated");
            }
        }catch (SQLException e){
            System.err.println("Error while connecting to the beneficiary database:  " +e.getStackTrace());
        }
    }
}
