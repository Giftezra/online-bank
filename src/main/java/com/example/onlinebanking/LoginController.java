package com.example.onlinebanking;

import at.favre.lib.crypto.bcrypt.BCrypt;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Random;
import java.util.ResourceBundle;
/**This is the class that handles the user login to their bank accounts. this class will implement several methods and will gain access to the database
 * and get the data*/
public class LoginController implements Initializable {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton, joinUs;
    @FXML
    private Label commentLabel;


    private int user_id;
    private String accountName;
    private long accountNumber, sortcode;
    private double balance = 3000.0;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        commentLabel.setText("Thanks for coming on today, if you have not registered, click the button below to join us ....");
        // Set an event listener for the login button
        loginButton.setOnAction(event -> {
            loginToUserDb();
        });


    }

    /*This is the alert method to handle all alert to the user*/
    public void alert(String title, String header_title){
        // Call the alert class and pass the values
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header_title);
        alert.show();
    }


    /*This method will return the user id*/
    private int getUserId(){
        return user_id;
    }

    public void setUser_id(int user_id) {this.user_id = user_id;}


    /*This method will confirm if the passwords are a match
     * @param password, confirmPassword
     * @return boolean
     * */
    private boolean isAuthenticated(String password, String userPassword){
        int WORKLOAD = 12;
        // Check if the confirmation password matched the hashed password
        return BCrypt.verifyer().verify(password.toCharArray(), userPassword).verified;
    }

    /* This method makes sure the user meets the password criteria for passwords not lesser than 8 digits
     * and must contain other alphabets*/
    private boolean isPasswordSecure (String password){
        return password.length() >= 8 && password.matches(".*[a-zA-Z]+.*");
    }

     /**This method will handle the user login. it will use the inputed username to search the database for that specific username,
     * and if found, it will then make sure the password hashed and salted in the database is the same password the user nas entered.
     * if this matches it will then send the user id to the next page using the passUserId  method
     */
    private void loginToUserDb(){
        // Get the fields
        String username = usernameField.getText();
        String password = passwordField.getText();
        String query = "SELECT * FROM user WHERE username = ?"; // Select query
        /* Check if the fields are empty and if the password meets the security criteria*/
        if (username.isBlank() || password.isBlank()) return;
        if (!isPasswordSecure(password)){
            passwordField.setStyle("-fx-border-color:red");
            return;
        }
        /* Use a try resource to open the connection to get the database url and the prepared statement.
        * set the inputted username and get the result using the result set object*/
        try (Connection connection = DriverManager.getConnection(DatabaseManager.USERDATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(query);){
            statement.setString(1, username);
            /*
            *If the result set is not empty, get the user id and the user password. then authenticate the users password using the isauthenticated
            * method which hashes and salts the password and returns a boolean
            */
            ResultSet result = statement.executeQuery(); // Get the result of the query
            if (result.next()){
                setUser_id(result.getInt("id"));
                String userPassword = result.getString("password");
                if (!isAuthenticated(password, userPassword)){
                    alert("Security issues", "Incorrect password entered. try again");
                    passwordField.clear();
                    passwordField.setStyle("-fx-border-color:red");
                }else {
                    toMainPage();
                }
            }else {
                alert("Invalid", "You entered an invalid user name");
                usernameField.clear();
                passwordField.clear();
            }
        }catch (SQLException e){
            System.err.println("Error with user login " + e.getMessage());
        }
    }


    /*This method sets the user id in the main controller. it will first get the controller*/
    public void toMainPage(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
            Parent root = loader.load();
            MainController mainController = loader.getController();
            mainController.setUserId(getUserId());
            // Set the scene to the main page
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style/main.css").toExternalForm());
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        }catch (IOException e){
            System.err.println(e.getMessage());
        }
    }



    /* This  method will take the user back to the registration page if they do not have an account*/
    @FXML
    public void register () {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("registration.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            String css = getClass().getResource("/style/register.css").toExternalForm();
            scene.getStylesheets().add(css);
            Stage stage = (Stage) joinUs.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
