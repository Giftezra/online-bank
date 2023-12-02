package com.example.onlinebanking;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import at.favre.lib.crypto.bcrypt.BCrypt;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.security.PrivateKey;
import java.sql.*;
import java.util.Objects;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/** This is the registration class which will handle the user registration fxml.
 * This class will implement the methods, field, database and any interface with relation to the user registration.
 * implements the initialize interface
 * */
public class RegistrationController implements Initializable{

    private long accountNumber, sortCode;
    private String username, accountName;
    ObservableList<UserDetails> userList;

    /* Get the fxml fields from the registration.fxml*/
    @FXML
    private Button registerButton;
    @FXML
    private TextField firstName, lastName, address1Field, address2Field, postCodeField, emailField, phoneField;
    @FXML
    private PasswordField passwordField, confirmPassword;
    @FXML
    private DatePicker dob;

    /* Since this method runs immediately the page loads, initialize the observable user list to a fx collection arraylist.
    * Call the database helper object and create a user table.
    * Set an even handle on the register button and call the insertIntoUserTable method when the button is clicked
    * @param url
    * @param resource bundle */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userList = FXCollections.observableArrayList();
        DatabaseManager db = new DatabaseManager();
        db.createUserTable();

        registerButton.setOnAction(event -> {
            insertIntoUserTable();
        });
    }


    /* This is the alert method which will throw an alert for any error found inside any method
     * when called
     * @params title, title header
     * */
    private void alert(String title, String title_header){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(title_header);
        alert.setTitle(title);
        alert.showAndWait();
    }

    /* This method combines the first name and the last name with a string literal in between */
    private String accountName (String firstname, String lastname){
        return firstname + " " + lastname;
    }

    /*This method will generate a  username for the user , using their firstname and a few randomly generated integers
     * it will call the Random object and use its random.nextInt method along with the String builder object to create and compile
     * a set of generated random numbers.
     * @params first name
     * */
    private void createUsername (String firstname) {
        if (firstname.isBlank())return;
        Random random = new Random();
        StringBuilder userBuilder = new StringBuilder();
        int userInt = 0;
        /* Append the first name, then runs a for loop 3 times to make sure it is 3 while generating random numbers appending
        * it to the string builder everytime*/
        userBuilder.append(firstname);
        for (int i = 0; i < 3; i++) {
            userInt = random.nextInt(10);
            userBuilder.append(userInt);
        }
        setUserName (userBuilder.toString());// Set the username
    }


    /*Returns the username*/
    public String getUsername() {
        return username;
    }

    /* Hash the password param using the Bcrypt.withDefault method to hash the password string and a workload variable.
    * salt the password
    * returns a hashed value for the param*/
    private String hashPassword(String password) {
        int WORKLOAD = 12; // Hashing workload
        // Return the bcrypt hashed password
        return BCrypt.withDefaults().hashToString(WORKLOAD, password.toCharArray());
    }


    /* Insert the fields entered by the user into the user table*/
    private void insertIntoUserTable () {
        /* Get the user fields*/
        String firstname = firstName.getText();
        String lastname = lastName.getText();
        String address1 = address1Field.getText();
        String address2 = address2Field.getText();
        String postcode = postCodeField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String password = passwordField.getText();
        String confirmPass = confirmPassword.getText();
        /* Create a username by passing the users first name variable from the field.
        * checks if the password matches throws an alert and returns, checks if the phone field is numeric and returns if not,
        * checks if the password is secure and throws an alert and returns if not,
        * checks if some fields are blank, throws alert and returns so the user could try again */
        createUsername(firstname);
        if (!Objects.equals(password, confirmPass)) {
            alert("Security", "Passwords do not match...");
            return;
        }
        if (!isNumeric(phone)){
            phoneField.setStyle("-fx-border-color:red");
            return;
        }
        if (!isPasswordSecure(password)) {
            alert("Security", "Passwords must be 8 digits or longer and contain alphabets");
            passwordField.setStyle("-fx-border-color:red"); // Set the border color to red if not correcr
            return;};

        if (firstname.isBlank() && lastname.isBlank() && password.isBlank() && dob.getValue() == null){
            alert("Incomplete data", "This user has failed to enter one or more important details");
            return;
        }
        /* Pass the password as an argument to the hashPassword method and saves the return value.
        * Gets the date value for the users dob and converts it to java.util */
        String hashedPassword = hashPassword(password);
        java.util.Date utilDate = java.sql.Date.valueOf(dob.getValue());

        /* Calls the UserDetails object and pass the arguments.
        * uses the user reference of the user details to call the generateSortCode method generateAccountNumber and getBalance method.
        * call the saveToDatabase and pass the required arguments */
        UserDetails user = new UserDetails(firstname, lastname, getUsername(), new Date(utilDate.getTime()));
        long sortcode = user.generateSortCode(); long accountNumber = user.generateAccountNumber();
        user.setSortCode(sortcode); user.setAccountNumber(accountNumber);
        double balance = user.getBalance();
        user.saveUserDetails(
                firstname, lastname, email, phone, hashedPassword, accountName(firstname, lastname),
                address1, address2, postcode, getUsername(), new Date(utilDate.getTime()),
                accountNumber, sortcode, balance
        );
        /* Adds the user to a list of users and takes the user to the next page if the user has been saved*/
        userList.add(user);
        alert("Registered", "Hello " + firstname + "\nYour username is " + getUsername() + " Keep this safe !!");
        nextPage();
    }

    /* This method makes sure the user meets the password criteria for passwords not lesser than 8 digits
    * and must contain other alphabets*/
    private boolean isPasswordSecure (String password){
        return password.length() >= 8 && password.matches(".*[a-zA-Z]+.*");
    }

    /* This method will set the user field so only numeric data types are accepted*/
    private boolean isNumeric(String text) {
        // Check if the given text is numeric (contains only digits)
        return Pattern.matches("\\d*", text);
    }

    /* This is the next page that will take the user to the next page */
    private void nextPage (){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            String css = getClass().getResource("/style/login.css").toExternalForm();
            scene.getStylesheets().add(css);
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void setUserName(String username){
        this.username = username;
    }
}