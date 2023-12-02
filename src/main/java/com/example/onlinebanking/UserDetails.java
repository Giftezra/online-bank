package com.example.onlinebanking;

import javafx.collections.ObservableList;

import java.sql.*;
import java.util.Random;

/** This class and all its method represents a user on the system. this class will have two constructors to initialize different
 * users. for example the constructor with just 4 arguments will initialize a user beneficiary from the beneficiary database*/
public class UserDetails extends User{
    // Get the fields
    public String email, phone, password, accountName, address1, address2, postcode;
    public long accountNumber;
    public long sortCode;
    public double balance = 3000.0;
    /*This constructor initialize the users main details*/
    public UserDetails(String firstName, String lastName, String username, java.sql.Date dob) {
        super(firstName, lastName, username, dob);
    }
    /* This constructor initialize the users beneficiary*/


    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}
    public String getPhone() {return phone;}

    public void setPhone(String phone) {this.phone = phone;}

    public String getPassword() {return password;}

    public void setPassword(String password) {this.password = password;}

    public String getAccountName() {return accountName;}

    public void setAccountName(String accountName) {this.accountName = accountName;}

    public String getAddress1() {return address1;}

    public void setAddress1(String address1) {this.address1 = address1;}

    public String getAddress2() {return address2;}

    public void setAddress2(String address2) {this.address2 = address2;}

    public String getPostcode() {return postcode;}

    public void setPostcode(String postcode) {this.postcode = postcode;}

    public long getAccountNumber() {return accountNumber;}

    public void setAccountNumber(long accountNumber) {this.accountNumber = accountNumber;}

    public long getSortCode() {return sortCode;}

    public void setSortCode(long sortCode) {this.sortCode = sortCode;}

    public double getBalance() {return balance;}

    public void setBalance(double balance) {this.balance = balance;}


    /**This method will be used to insert the user details into the database when it is called by
     * setting the values in the parameters to the data in the database
     * @param firstname, lastName, email, phone, password, accountName, address1, address2, postcode,
     *                   username, dob, accountNumber, sortCode, balance
     * */
    public  void saveUserDetails (String firstname, String lastname, String email,
                                 String phone, String password, String accountName,
                                 String address1, String address2, String postcode,
                                 String username, java.sql.Date dob, long accountNumber,long sortCode, double balance) {
        String query = "INSERT INTO user (firstname, lastname, address1, address2, postcode, email," +
                " phone, dob, password, username, accountName, accountNumber, sortCode, balance)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        /* Use a try resource to open the database connection and the prepare statement object
         * call the setter methods and set the methods to their respective arguments*/
        try (Connection connection = DriverManager.getConnection(DatabaseManager.USERDATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(query);) {
            statement.setString(1, firstname);
            statement.setString(2, lastname);
            statement.setString(3, address1);
            statement.setString(4, address2);
            statement.setString(5, postcode);
            statement.setString(6, email);
            statement.setString(7, phone);
            statement.setDate(8, dob);
            statement.setString(9, password);
            statement.setString(10, username);
            statement.setString(11, accountName);
            statement.setLong(12, accountNumber);
            statement.setLong(13, sortCode);
            statement.setDouble(14, balance);
            /* Execute query and check if the table has been inserted correctly*/
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {System.out.println("The user table has been updated successfully");
            }else {System.out.println("User page could not be created ");}
        } catch (SQLException e) {
            System.err.println("Error occurred while adding data to the user database " + e.getMessage());
        }
    }

    /**This method will generate a 6 digit sort code using the generate random figures with bounds between 0 - 9
     * the method uses a String  builder to compile the generated number for the loop size
     */
    public long generateSortCode (){
        Random random = new Random();
        StringBuilder sortString = new StringBuilder();
        // Use a for loop to make sure it iterates 6 times
        for (int i = 0; i < 6; i++) {
            int sort = random.nextInt(9);
            sortString.append(sort);
        }
        return Long.parseLong(sortString.toString());
    }


    /**This method will randomly generate an 8 digit long account number for the user.
     * the method uses a random generator to generate numbers between the bounds of 9 and also
     * uses a string builder to compile the generated numbers
     * finally sets the account number to the generated value
     * */
    public long generateAccountNumber (){
        // Random
        Random random = new Random();
        StringBuilder accountString = new StringBuilder();//
        for (int i = 0; i < 8; i++) { // Use a for loop to make sure the account number has 8 values
            int account = random.nextInt(9); // Create a random int value within the bounds of 0-9
            accountString.append(account);//
        }
        return Long.parseLong(accountString.toString());
    }



    @Override
    public String toString() {
        return "UserDetails{" +
                "email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", password='" + password + '\'' +
                ", accountName='" + accountName + '\'' +
                ", address1='" + address1 + '\'' +
                ", address2='" + address2 + '\'' +
                ", postcode='" + postcode + '\'' +
                ", accountNumber=" + accountNumber +
                ", sortCode=" + sortCode +
                ", balance=" + balance +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", dob='" + this.dob + '\'' +
                '}';
    }
}
