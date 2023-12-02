package com.example.onlinebanking;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Objects;
import java.util.ResourceBundle;

/** The controller for the main fxml from where the user would navigate to other part of the app.
 * the controller calls has several methods and implements the initialize interface to load data upon page load up*/
public class MainController implements Initializable {
    private int user_id; // User id field
    /* Initialize all fxml fields*/
    @FXML
    private Button transferButton, transactionButton, logoutButton, depositButton;
    @FXML
    private Label welcomeLabel, nameLabel, balanceLabel, accountLabel, sortCodeLabel;
    private static final int UPDATE_INTERVAL_SECONDS = 1;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        /* Set a timeline and a new key frame object. call the updateUserProfile inner class which extends a thread class
         * to implement its start method inside the timeline every 1 sec refresh*/
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(UPDATE_INTERVAL_SECONDS), event -> {
                    UpdateUserProfile userProfile = new UpdateUserProfile();
                    userProfile.start();
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE); // Set the timeLine to continue indefinitely
        timeline.play();
    }

    public void setUserId(int user_id) {this.user_id = user_id;}

    private int getUser_id() {
        return user_id;
    }


    /*This method will take the user to the deposit page */
    @FXML
    private void depositPage (){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("deposit.fxml"));
            Parent root = loader.load();
            DepositController depositController = loader.getController();
            depositController.setUserId(getUser_id()); // Pass the user id to the deposit controller
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style/deposit.css").toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        }catch (IOException e){
            System.err.println("Error transferring to the transactions page " + e.getMessage());
        }
    }

    /* Log the user out by*/
    @FXML
    private void logout (){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style/login.css").toExternalForm());
            Stage stage = (Stage) logoutButton.getScene().getWindow(); // Set the scene to the current window
            stage.setScene(scene);
            stage.show();
        }catch (IOException e){
            System.err.println("Error transferring to the login page " + e.getStackTrace());
        }
    }

    /*This method will take the user to the transfer page where they can perform transactions to other bank users.
    * this method will get the controller for the transfer class and pass the user id to it  */
    @FXML
    private void transferPage (){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("transfer.fxml"));
            Parent root = loader.load();
            TransferController transferController = loader.getController(); // Get the object reference
            transferController.setUser_id(getUser_id());// Parse the user id
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/transfer.css")).toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        }catch (IOException e){
            System.err.println("Error transferring to the transfer page " + e.getStackTrace());
        }
    }

    /*This method will take the user to the transactions page */
    @FXML
    private void transactionPage (){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("transaction.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) transactionButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        }catch (IOException e){
            System.err.println("Error transferring to the transactions page " + e.getMessage());
        }
    }



    /**This class will have different methods to get several data from the user and account database
     * it implements the runnable interface and overrides its run method.
     * it has no argument, only methods
     * */
    private class UpdateUserProfile extends Thread{
        /* This method returns a string with the users full name.
         * it uses a try resource to open the connection to the database, prepare the user statement too.
         * @return fullname
         * */
        private String getFullName (){
            String query = " SELECT * FROM user WHERE id = ?"; // Query statement
            try (Connection connection = DriverManager.getConnection(DatabaseManager.USERDATABASE_URL);
                 PreparedStatement statement = connection.prepareStatement(query)){
                 statement.setInt(1, getUser_id()); // Pass the user id
                 ResultSet resultSet = statement.executeQuery(); // Get the result from the query
                /* Check if the result is empty before storing the return fields from the result*/
                if (resultSet.next()){
                    String firstname = resultSet.getString("firstname");
                    String lastname = resultSet.getString("lastname");
                    return firstname + " " + lastname;
                }
            }catch (SQLException e){
                System.err.println("Error while getting the user full name " + e.getMessage());
            }
            return null;
        }

        /* Method is used to get the user sort code from the database and returns the data*/
        private long getSortCode (){
            /* Select sort code where the id is same as the user id*/
            String query = "SELECT sortCode FROM user WHERE id = ?";

            /* Uses a try resources to open the db connection and the prepared statement. pass the query*/
            try (Connection connection = DriverManager.getConnection(DatabaseManager.USERDATABASE_URL);
                PreparedStatement statement = connection.prepareStatement(query);){
                statement.setInt(1, getUser_id());
                /* Get the result by calling the statement.executeQuery method omn the ResultSet */
                ResultSet result = statement.executeQuery();
                /* If the result finds a user with the id the user table, return the sort code */
                if (result.next()){
                   return result.getLong("sortCode");
                }
            }catch (SQLException e){
                /* Catch sql errors and simply log the message to the console*/
                System.err.println("Error while fetching the user account number " + e.getMessage());
            }
            return 0;
        }

        /* Get the user balance where the id matches with the user id*/
        private double getBalance (){
            /* Select balance from the user table where the id matches the user id*/
            String query = "SELECT balance FROM user WHERE id = ?";
            /* Use a try resource to open the db connection alongside the prepared statement so that the connection closes after implementation*/
            try (Connection connection = DriverManager.getConnection(DatabaseManager.USERDATABASE_URL);
                 PreparedStatement statement = connection.prepareStatement(query);){
                statement.setInt(1, getUser_id());
                /* Call the statement.executeQuery method on the resultSet object*/
                ResultSet result = statement.executeQuery();
                /* If the result finds a user with the foreign key in the account table, get the balance*/
                if (result.next()){
                    return result.getDouble("balance");
                }
            }catch (SQLException e){
                System.err.println("Error while fetching the user balance " + e.getMessage());
            }
            return 0;
        }

        private long getAccountNumber(){
            /* Select account name from the account table where the user id (foreign key) is the same as the user id*/
            String query = "SELECT accountNumber FROM user WHERE id = ?"; //
            // Try resource to open the connection and create a statement
            try (Connection connection = DriverManager.getConnection(DatabaseManager.USERDATABASE_URL);
                 PreparedStatement statement = connection.prepareStatement(query);){
                statement.setInt(1, getUser_id());
                ResultSet result = statement.executeQuery();
                /* If the result finds a user with the foreign key in the account table, get the account number */
                if (result.next()){
                    return result.getLong("accountNumber");
                }
            }catch (SQLException e){
                System.err.println("Error while fetching the user account number " + e.getMessage());
            }
            return 0;
        }

        /* The overridden run method implements the platform method run later to set the labels with the return values
        * from the method in its class */
        @Override
        public void run() {
            /* This code update the javafx GUI on the javafx thread*/
          Platform.runLater(() ->{
              welcomeLabel.setText("Welcome ");
              nameLabel.setText(getFullName());
              balanceLabel.setText("Balance: " + getBalance());
              sortCodeLabel.setText("Sort code: " + getSortCode());
              accountLabel.setText("Account number: " + getAccountNumber());
          });

        }
    }
}
