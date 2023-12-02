package com.example.onlinebanking;

import java.sql.Date;
import java.util.Random;
/** This is the user object which will hold the users main details and give a skeletal structure to every user of the
 * platform.
 * it is the main class from where some other classes would extend */
public class User {
    public String firstName;
    public String lastName;
    public String username;
    public Date dob;
    /** This is the constructor
    * @param firstName'
     * @param lastName'
     * @param username'
     * @param dob'
    * */
    public User(String firstName, String lastName, String username, java.sql.Date dob) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.dob = dob;
    }

    public User(){

    }

    // Generate the getters and setter fields

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public java.sql.Date getDob() {
        return dob;
    }

    public void setDob(java.sql.Date dob) {
        this.dob = dob;
    }


}
