package com.example.onlinebanking;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

/** This class will handle the users deposit and withdrawal options...the class will include the fields from the fxml and implement
 * helper methods to get the user data and update them
 * implements the Transaction interface and the Initializable interface
 * */
public class DepositController implements Transaction, Initializable {
    /* These are the fxml fields*/
    private TextField depositAmountField, withdrawalAmountField;
    private Button depositButton, withdrawalButton;
    private Label depositLabel, withdrawLabel;
    private int userId;
    private  boolean withdrawal_update = false;
    private boolean deposit_update = false;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    /* This is the alert method that will trigger an alert for different reasons in the controller */
    public void alert (String title, String header, String context){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(context);
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    /** This helper method will handle the depositing of funds to the users balance in the database.
     * @param userId'
     * @param amount'
     * @throws SQLException'
     */
    private void updateDeposit (int userId, double amount) throws SQLException{
        /* Update user table balance by adding the specified amount to the user with the given id
         * Open the connection to the database using the database helper class static url.
         * Use the prepared statement object to generate a sql statement*/
        String query = "UPDATE user SET balance = balance + ? WHERE id = ?";
        Connection connection = DriverManager.getConnection(DatabaseManager.USERDATABASE_URL);
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setDouble(1, amount);
        statement.setInt(2, getUserId());
        int updated = statement.executeUpdate();
        if (updated > 0){
            deposit_update = true;
        }

    }

    public void setUserId (int id){
        this.userId = id;
    }

    public int getUserId () {
        return userId;
    }

    @Override
    public void transferFunds() {
        // TODO
    }

    /** This method is an abstract method of the transact interface it will handle the deposit of funds for the user*/
    @FXML
    @Override
    public void deposit() {
        /* Get the field and convert it to double using the parse double method.
        * Use a try catch to wrap the update deposit method*/
        double amount = Double.parseDouble(withdrawalAmountField.getText());
        try {
            updateDeposit(getUserId(), amount);
            /* If deposit successful, alert the user of the success and how much deposited.
            * Create two buttons to close the page and go back to the previous page of perform another transaction*/
            if (deposit_update){
                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Transaction");
                    alert.setHeaderText("Successful transaction");
                    alert.setContentText("Successfully deposited " + amount + "\nWould you like to perform another transaction?");
                    // Create two button types
                    ButtonType yes = new ButtonType("Yes");
                    ButtonType no = new ButtonType("No");
                    alert.getButtonTypes().addAll(yes, no);
                    /* Parse the result of the user choices.
                    * if the users choice is no, close the open stage else if the user wants to perform another transaction then close the alert*/
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == no){
                        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                        stage.close();
                    } else if (result.isPresent() && result.get() == yes) {
                        alert.close();
                    }
                });
            }
        }catch (SQLException e){
            System.err.println("Error while connecting to db to deposit funds " +e.getMessage());
        }
    }

}
