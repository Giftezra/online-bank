package com.example.onlinebanking;

import java.security.PublicKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    public static final String USERDATABASE_URL = "jdbc:sqlite:src/main/resources/user.db";


    /**This method will create a user table inside the user database, and it will get the fields from the UI and add them to the database
     * throws SQLException */
    protected void createUserTable (){
        /*Now create a connection using a try resource to open and close the connection*/
        try (Connection connection = DriverManager.getConnection(USERDATABASE_URL);
             Statement statement = connection.createStatement();) {
            // Check if the connection is null before creating the statement
            boolean tableCreated = statement.execute("CREATE TABLE IF NOT EXISTS user(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "firstname TEXT NOT NULL," +
                    "lastname TEXT NOT NULL," +
                    "address1 TEXT," +
                    "address2 TEXT," +
                    "postcode TEXT," +
                    "email TEXT,"+
                    "phone TEXT," +
                    "dob DATE NOT NULL," +
                    "password TEXT NOT NULL," +
                    "username TEXT NOT NULL," +
                    "accountName TEXT," +
                    "accountNumber LONG NOT NULL," +
                    "sortCode LONG NOT NULL," +
                    "balance NUMERIC NOT NULL" +
            ")");
            if (tableCreated){
                System.out.println("User table created successfully");
            }else{
                System.out.println("Table already exists");
            }
        }catch (SQLException sql){
            System.err.println(sql.getMessage());
        }
    }

    /** This method creates a table for the beneficiary which will have the user id as a foreign key.
     * the database will also have the beneficiary account details*/
    public void createBeneficiaryTable (){
        try (Connection connection = DriverManager.getConnection(DatabaseManager.USERDATABASE_URL);
             Statement statement = connection.createStatement();){
            boolean tableCreated = statement.execute("CREATE TABLE IF NOT EXISTS beneficiary(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "accountNumber LONG NOT NULL," +
                    "sortCode LONG NOT NULL," +
                    "amount NUMERIC NOT NULL," +
                    "accountName TEXT NOT NULL," +
                    "transactionTime TIMESTAMP NOT NULL," +
                    "FOREIGN KEY (user_id) REFERENCES user(id)" +
                    ")");

            if (tableCreated){
                System.out.println("Beneficiary table created successfully");
            }else {
                System.out.println("Table already exist");
            }
        }catch (SQLException e){
            System.err.println("Error while creating the beneficiary database " + e.getStackTrace());
        }
    }



}

