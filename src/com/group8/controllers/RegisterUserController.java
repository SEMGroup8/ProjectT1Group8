package com.group8.controllers;

import com.group8.database.MysqlDriver;
import com.group8.database.tables.Pub;
import com.group8.database.tables.User;
import com.group8.singletons.Navigation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Linus Eiderström Swahn.
 *
 * Controller for the register user scene.
 */
public class RegisterUserController extends BaseController {

    @FXML
    public Button back;
    @FXML
    public Button register;
    @FXML
    public TextField username, fullName;
    @FXML
    public TextField email, pubName, age;
    @FXML
    public CheckBox isPub;
    @FXML
    public PasswordField password;

    @FXML
    public Label usernameError, emailError, pubNameError, passwordError, isPubError, fullNameError, pubNameLabel, ageError;

    /**
     * Created by Linus Eiderström Swahn.
     *
     * This function gets called when the user presses the register user button.
     *
     * It first performs checks to see if the input is correct, then inserts a new user into the database.
     * @param event
     * @throws IOException
     */
    @FXML
    public void onRegister(ActionEvent event) throws IOException
    {
        if(!checkInput())
        {
            return;
        }

        String selectQuery = "Select * from users where username = '" + username.getText().toLowerCase() + "';";
        if (!checkAvailability(selectQuery))
        {
            usernameError.setText("Username is in use.");
            username.setStyle("-fx-border-color: red;");

            return;
        }

        selectQuery = "Select * from users where email = '" + email.getText() + "';";

        if (!checkAvailability(selectQuery))
        {
            emailError.setText("Email is in use.");
            email.setStyle("-fx-border-color: red;");

            return;
        }

        int pubId = 0;

        if(isPub.isSelected())
        {
            Pub newPub = new Pub();

            newPub.setName(pubName.getText());

            if(newPub.insertPub())
            {
                pubId = newPub.getPubId();
            }
            else
            {
                pubNameError.setText("Pub name is taken.");
                pubName.setStyle("-fx-border-color: red;");

                return;
            }
        }

        User newUser = new User(username.getText(), fullName.getText(), password.getText(), email.getText(), isPub.isSelected(), pubId, Integer.parseInt(age.getText()));

        newUser.insert();

        mainScene.changeCenter(Navigation.homescreenFXML);
    }

    /**
     * Created by Linus Eiderström Swahn.
     *
     * Checks the input of each field so that it is correct.
     *
      * @return
     * returns true if the user has inputed everything correctly, false otherwise.
     */
    private boolean checkInput()
    {
        boolean canRegister = true;

        if(username.getText().length()==0)
        {
            usernameError.setText("Username has to be filled in.");
            username.setStyle("-fx-border-color: red;");
            canRegister = false;
        }

        if(fullName.getText().length()==0)
        {
            fullNameError.setText("Name has to be filled in.");
            fullName.setStyle("-fx-border-color: red;");
            canRegister = false;
        }

        if(!email.getText().contains("@")||!email.getText().contains("."))
        {
            emailError.setText("Email is invalid.");
            email.setStyle("-fx-border-color: red;");
            canRegister = false;
        }

        if(email.getText().length()==0)
        {
            emailError.setText("Email has to be filled in.");
            email.setStyle("-fx-border-color: red;");
            canRegister = false;
        }

        if(password.getText().length()<8)
        {
            passwordError.setText("Password has to be at least 8 characters.");
            password.setStyle("-fx-border-color: red;");
            canRegister = false;
        }

        if(password.getText().length()==0)
        {
            passwordError.setText("Password can't be empty.");
            password.setStyle("-fx-border-color: red;");
            canRegister = false;
        }

        if(age.getText().length()==0)
        {
            ageError.setText("Age has to be filled in.");
            age.setStyle("-fx-border-color: red;");
            canRegister = false;
        }
        else
        {
            for(int i = 0; i<age.getText().length(); i++)
            {
                if(!age.getText().matches("[0-9]+"))
                {
                    ageError.setText("Age can only be numerical.");
                    age.setStyle("-fx-border-color: red;");
                    canRegister = false;
                }
            }
        }

        if(isPub.isSelected())
        {
            if(pubName.getText().length() == 0)
            {
                pubNameError.setText("Pub name can't be empty if you are a pub owner");
                pubName.setStyle("-fx-border-color: red;");
                canRegister = false;
            }
        }

        return canRegister;
    }

    /**
     * Created by Linus Eiderström Swahn.
     *
     * performs a check to see if the data we want to insert into the database all ready exists.
     *
     * @param query
     * The string containing a sql-query for selecting the data we want to check from the database.
     * @return
     * true if the data doesn't exist onthe database, false otherwise.
     */
    public static boolean checkAvailability(String query)
    {
        ArrayList<Object> returnedUser = MysqlDriver.select(query);

        boolean canRegister = true;

        if(returnedUser!=null)
        {
            canRegister =  false;
        }

        return canRegister;
    }

    /**
     * Created by Linus eiderström Swahn.
     *
     * If the user wants to register a pub user this changes the layout of the form.
     * @param event
     */
    @FXML
    void isPubSelected(ActionEvent event) {

        if(isPub.isSelected()){
            pubNameLabel.setVisible(true);
            pubName.setVisible(true);
        }
        else{
            pubNameLabel.setVisible(false);
            pubName.setVisible(false);
        }
    }
}
