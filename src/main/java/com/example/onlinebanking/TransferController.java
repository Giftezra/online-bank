package com.example.onlinebanking;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
/**
 * This is the major class that would handle the user transactions . this class will have two inner classes that wi;l handle seperate tasks.
 * the transaction class will handle the sending of money from the user bank account to the recipients bank account, the user accounts class
 * will get some data from the user
 */
public class TransferController implements Initializable, Transaction, Runnable{
    /*These are the fields from the fxml file and class level fields. with an observables list*/
    private static boolean RECIPIENT_ACCOUNT_UPDATED = false;
    private static boolean SENDER_ACCOUNT_UPDATED = false;
    private int userId;
    @FXML
    private  TextField accountNameField, sortCodeField, accountNumberField, amountField;
    @FXML
    private Button sendButton, cancelButton;
    @FXML
    private ListView<Beneficiary> beneficiaryListView;
    ObservableList<Beneficiary> beneficiaryList;
    private Transact transact;

    /* Runs the codes within its body as soon as the page loads*/
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        DatabaseManager db = new DatabaseManager();
        db.createBeneficiaryTable();
        /* Initialize the beneficiary list to an observables fx collection arraylist*/
        beneficiaryList = FXCollections.observableArrayList();
        /* Set the cell factory for the beneficiary list using a lambda to call a new list cell and override its update item method
        * passing the beneficiary object and a boolean*/
        beneficiaryListView.setCellFactory(param -> new ListCell<>() {
            /* This method updates the list view cells according to the main node orientations and its children node*/
            @Override
            protected void updateItem(Beneficiary beneficiary, boolean b) {
                super.updateItem(beneficiary, b);
                /* Create a vbox container and set its alignment to top center, set the spacing to 10.
                * create a label for the account name*/
                VBox beneficiaryContainer = new VBox();
                beneficiaryContainer.setAlignment(Pos.TOP_CENTER);
                beneficiaryContainer.setSpacing(10);
                Label accountName = new Label();
                /* Create another account detail container to hold the sort code and the account number labels*/
                HBox accountDetail  = new HBox();
                accountDetail.setAlignment(Pos.CENTER);
                accountDetail.setSpacing(10);
                // Create the label for the sort code and account number
                Label sortCode = new Label();
                Label accountNumber = new Label();
                /* Check if the boolean is empty and if the beneficiary list is null*/
                if (b || beneficiary == null){
                    setText(null);
                }else{
                    /* If not null call the setText method of the nodes and pass the beneficiary values to it*/
                    accountName.setText(beneficiary.getAccountName());
                    sortCode.setText(String.valueOf(beneficiary.getSortCode()));
                    accountNumber.setText(String.valueOf(beneficiary.getAccountNumber()));
                    /* Add the children node to the account detail
                    * Add the account detail node to the beneficiary container.
                    * set the graphics*/
                    accountDetail.getChildren().addAll(sortCode, accountNumber);
                    beneficiaryContainer.getChildren().add(accountDetail);
                    setGraphic(beneficiaryContainer);
                }
            }
        });
        // Call the transaction calling pass its arguments
        sendButton.setOnAction(event -> {
            Thread thread = new Thread(this);
            thread.start();
        });
        cancelButton.setOnAction(event -> {
            toMain();
        });
    }

    public int getUserId() {
        return userId;
    }

    public void setUser_id(int user_id) {
        this.userId = user_id;
    }


    /* This method will set the user field so only numeric data types are accepted*/
    private boolean isNumeric(String text) {
        // Check if the given text is numeric (contains only digits)
        return Pattern.matches("\\d*", text);
    }

    /**
     * This method will be used to update the recipients bank account balances
     *
     * @param amount'
     * @param accountNumber'
     * @param sortCode'
     * @param accountName'   the user will be verified before updating the users account balance
     */
    private void updateRecipientAccount (double amount, long accountNumber, long sortCode, String accountName){
        String query = "UPDATE user SET balance = balance + ? WHERE accountNumber = ? AND sortCode = ?";// Query statement
        try (Connection connection = DriverManager.getConnection(DatabaseManager.USERDATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(query)){
            if (validateRecipientDetails(accountNumber, sortCode, accountName)){
                /* Use a try catch to set the arguments to the statement methods.
                 * get the statement execute update return and check if the result is greater than 1 which means
                 * that the update was a success
                 * */
                try{
                    statement.setDouble(1, amount);
                    statement.setLong(2, accountNumber);
                    statement.setLong(3, sortCode);
                    int result = statement.executeUpdate();
                    if (result > 0){
                        RECIPIENT_ACCOUNT_UPDATED = true; // Update the account if result set return true
                        System.out.println("Successfully updated the recipients bank account");
                    }
                }catch (SQLException e){
                    System.err.println("Error while making the recipient statement " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error while updating the recipients account " + e.getMessage());
        }
    }

    /**
     * This method will be used to update the sender account.
     *
     * @param amount'*
     * @param user_id'
     */
    public void updateSenderAccount (double amount, int user_id) {
        String query = "UPDATE user SET balance = balance - ? WHERE id = ?";
        // Use a try resource to open the connection
        try (Connection connection = DriverManager.getConnection(DatabaseManager.USERDATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(query)){
            /* Update the sender balance by setting the amount - the balance in the database*/
            statement.setDouble(1, amount);
            statement.setInt(2, user_id);
            int updated = statement.executeUpdate();
            if (updated > 0){
                System.out.println("Sender account has been debited successfully");
                SENDER_ACCOUNT_UPDATED = true;
            }
        }catch (SQLException e){
            System.err.println();
        }
    }

    /*This method will check the account table for the user balance with the given id and parse it to the user balance */
    public double getSenderBalance() {
        String query = "SELECT * FROM user WHERE  id = ?";
        // Use a try resources to open and automatically close the connection. also used to catch the errors
        try (Connection connection = DriverManager.getConnection(DatabaseManager.USERDATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(query)) { // Prepare the statement with the query
            statement.setInt(1, getUserId());
            /* Get the result of the statement and confirm if there is any data in it before returning the balance of the user
             * with the specified id
             * return -1 if no user data was found*/
            try (ResultSet resultSet = statement.executeQuery()){ // Get the result from the query
                if (resultSet.next()){
                    return resultSet.getDouble("balance"); // Return the users account balance
                }else {
                    return -1; // If the user account found
                }
            }
        } catch (SQLException e) {
            System.err.println("Error while getting the user balance " + e.getMessage());
        }
        return  0.0;
    }


    /* This method will validate the users account number to make sure the account the user is entering ia a valid bank account detail
     * */
    public boolean validateRecipientDetails(long accountNumber, long sortCode, String accountName) {

        String query = "SELECT * FROM user WHERE accountNumber = ? AND sortCode= ?";
        try (Connection connection = DriverManager.getConnection(DatabaseManager.USERDATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, accountNumber);
            statement.setLong(2, sortCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String actualAccountName = resultSet.getString("accountName");
                    if (!accountName.equals(actualAccountName)) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Account Confirmation");
                            alert.setHeaderText("The account name you entered does not match the\naccount name related to the account.");
                            alert.setContentText("Found Account Name: " + actualAccountName + "\nDo you want to continue?");
                            /*Set the border color of the missing fields to red when not correct
                            * Instantiate a button type for the confirm and cancel button to get the user decision. if the user decides to continue with
                            * the transaction update the bank balances else take the user back to the main page*/
                            accountNameField.setStyle("-fx-border-color: red"); // Set the border color of the account name field to red
                            ButtonType confirmButton = new ButtonType("Continue");
                            ButtonType cancelButton = new ButtonType("Cancel");
                            alert.getButtonTypes().setAll(confirmButton, cancelButton);
                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.isPresent() && result.get() == confirmButton) {
                                alert.close();
                            }else {
                                toMain();
                            }
                        });
                        return true;
                    }
                    return true; // Returns true if the account exists
                }else {
                    /* Display a message to the user if the account they entered is not on the banks system*/
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Account Confirmation");
                        alert.setHeaderText("The account number and sort code you entered does not match any account in our system.");
                        /*Set the border color of the missing fields to red which will indicate what data was missing*/
                        sortCodeField.setStyle("-fx-border-color: red");
                        accountNumberField.setStyle("-fx-border-color: red");

                    });
                    return false; // Returns false if the account does not exist
                }
            }
        } catch (SQLException e) {
            // Log or print the exception details for debugging
            System.err.println("Error while confirming the recipient's account details " + e.getMessage());
            return false;
        }
    }

    /** This is the overridden transferFunds method from the transaction interface
     * this method will be used to transfer funds from the user account to the recipient account
     * */
    @Override
    public void transferFunds() {
        String  accountNumberText = accountNumberField.getText();
        String sortCodeText = sortCodeField.getText();
        String amountFieldText = amountField.getText();
        String accountName = accountNameField.getText();
        /* The task will handle  the transaction on the background thread and within its call method, the local date object will be called
        * to get the current time the transaction is completed.
        * if the fields are not numbers display anm alert message
        * if the senders balance is greater than the amount to send, the transaction
        * continues and saves the beneficiaries data to the database*/
        Task<Void> transferTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                LocalDateTime localDate = LocalDateTime.now(); // Get the current local time
                if (!isNumeric(accountNumberText) || !isNumeric(sortCodeText) || !isNumeric(amountFieldText)){
                    accountNameField.setStyle("-fx-border-color:red");
                    sortCodeField.setStyle("-fx-border-color:red");
                    amountField.setStyle("-fx-border-color:red");
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Number Error");
                    alert.setHeaderText("Enter only numbers in the fields");
                    alert.showAndWait();
                };
                // Convert the data to the proper data type required by the methods
                long accountNumber = Long.parseLong(accountNumberText);
                long sortCode = Long.parseLong(sortCodeText);
                double amount = Double.parseDouble(amountFieldText);
                if (getSenderBalance() > amount) {
                    updateRecipientAccount(amount, accountNumber, sortCode, accountName);
                    updateSenderAccount(amount, getUserId());
                    /* Check if the recipient account and the user account has been updated successfully.
                     * make an instance of the beneficiary object, pass its parameters and call its save method
                     * which will save the account entered by the user into the database with the senders id used as the fk*/
                    if (RECIPIENT_ACCOUNT_UPDATED && SENDER_ACCOUNT_UPDATED) {
                        Timestamp transactionTime = Timestamp.valueOf(localDate);
                        Beneficiary beneficiary = new Beneficiary(accountName, accountNumber, sortCode, amount);
                        beneficiary.saveToBeneficiaryDatabase(getUserId(),accountName, accountNumber, sortCode, amount, transactionTime);

                        updateBeneficiaryList();
                        /* Call the alert methods using the platform.run since they would be running on the javafx thread*/
                        Platform.runLater(() -> {
                            showSuccessAlert();
                            toMain();
                        });
                    }
                } else {
                    Platform.runLater(() -> showInsufficientFundsAlert());
                }
                return null;
            }
        };

        // Start the task in a separate thread
        new Thread(transferTask).start();
    }

    /**
     * This method will get the beneficiaries data from the db and add it to the list of beneficiaries
     */
    public void getBeneficiaries(){
        String query = "SELECT * FROM beneficiary WHERE user_id = ?";
        /*Use a try resource to open the connection and the statement. set the user id and get the result
        * while the result is not empty, get the account names, account numbers and sort codes linked to the user
        * and add it to the observables list of beneficiaries */
        try(Connection connection = DriverManager.getConnection(DatabaseManager.USERDATABASE_URL);
            PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, getUserId()); // Get the beneficiaries attached to the user id
            ResultSet result = statement.executeQuery(); // Get the result from the query
            while (result.next()){
                // Initialize the beneficiary object with three arguments
                Beneficiary beneficiary = new Beneficiary(
                        result.getString("accountName"),
                        result.getLong("accountNumber"),
                        result.getLong("sortCode")
                );
                beneficiaryList.add(beneficiary);
            }
        }catch (SQLException e){
            System.err.println("Error while getting data from the beneficiary database: " + e.getMessage());
        }
    }

    private void showSuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(String.valueOf(Transact.SENT));
        alert.setHeaderText("Transaction Successful");
        alert.setContentText("Your transaction was successful");
        alert.showAndWait();
    }

    private void showInsufficientFundsAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Insufficient funds");
        alert.setHeaderText(String.valueOf(Transact.FAILED));
        alert.showAndWait();
    }

    /*This method takes the user back to the main and has a css style sheet specifically for the main page*/
    private void toMain () {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
            Parent root = fxmlLoader.load();
            MainController mainController = fxmlLoader.getController();
            mainController.setUserId(getUserId());
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/main.css")).toExternalForm());
            Stage mainStage = new Stage();
            mainStage.setTitle("Enigma Mobile Bank");
            mainStage.setScene(scene);
            mainStage.show();
        } catch (Exception e) {
            System.err.println("Error while opening the main window " + e.getMessage());
        }
    }

    private void updateBeneficiaryList(){
        getBeneficiaries();
        Platform.runLater(() -> {
            beneficiaryListView.setItems(beneficiaryList);
        });

    }

    @Override
    public void run() {
        transferFunds();
    }

    @Override
    public void deposit() {
        // Add deposit method default
    }
}
