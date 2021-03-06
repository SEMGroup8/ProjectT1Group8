package com.group8.controllers;

import com.group8.database.MysqlDriver;
import com.group8.database.tables.Beer;
import com.group8.database.tables.Pub;

import com.group8.database.tables.User;
import com.group8.singletons.BeerData;
import com.group8.singletons.Navigation;
import com.group8.singletons.PubData;
import com.group8.singletons.UserData;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.collections.*;

import javax.swing.*;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Controller for the center home scene.
 *
 * Inherits BaseController for some UI-functionality.
 *
 * Implements Initializable so that we can tale advantage of the initialize() function.
 */
public class HomeCenter extends BaseController implements Initializable
{
    @FXML
    public Button search, pubSearchButton, userSearchButton, showAllUsers, showAllPubs;
    @FXML
    public Label error;
    @FXML
    public Button randomButton;
    @FXML
    public CheckBox advancedType;
    @FXML
    public CheckBox advancedProducer;
    @FXML
    public CheckBox advancedDescription;
    @FXML
    public CheckBox advanced;
    @FXML
    public CheckBox all;
    @FXML
    public CheckBox searchForUsersCheckbox;
    @FXML
    public CheckBox searchForPubsCheckbox;
    @FXML
    public CheckBox advancedName;
    @FXML
    public CheckBox advancedCountry;
    @FXML
    public Button beerScanButton, beerScanSearchButton;

    public TextField searchText;
    @FXML
    public ProgressIndicator Load;
    @FXML
    public ToggleButton notButton;
    @FXML
    public ListView<String> notList;
    @FXML
    public SplitPane notWindow;
    @FXML
    public Label notifications;

    private SwingNode webCam;
    private BorderPane pane;
    private Stage webcamStage;
    private Scene webcamScene;
    private static String barcode = null;
    private static Button workaroundButton;
    boolean cameraOpen= false;
    
    int notNum=0;

    private Service<Void> backgroundThread;

    /**
     * Created by Joseph Roberto Delatolas
     */
    public void getRow(){
        notList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            // Select item will only be displayed when dubbleclicked

            /**
             * Dubleclick event
             */
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    String notification = notList.getSelectionModel().getSelectedItem();
                    
                    String remove = "Delete from notifications where notification = '"+notification+"' and userId = "+UserData.userInstance.getId()+";";
                    MysqlDriver.update(remove);
                    
                    notification = notification.substring(0, notification.indexOf(' ')); 
                    
                    BeerData.searchInput = "SELECT distinct `beerID`,`name`,`image`,`description`,beerTypeEN,countryName, percentage, producerName, volume, isTap, packageTypeEN, price, avStars, countryFlag " +
                            "from beers, beerType, origin, package " +
                            "where beers.beerTypeID = beerType.beerTypeID " +
                            "and beers.originID = origin.originID " +
                            "and beers.package = package.packageID " +
                            "and (";
                    
                    BeerData.searchInput += "name like '%" + notification + "%');";
                    
                    ArrayList<ArrayList<Object>> sqlData;

                    sqlData = MysqlDriver.selectMany(BeerData.searchInput);

                    for (int i = 0; i < sqlData.size(); i++) {
                        // Add a new Beer to the beer arraylist
                        Beer beer = new Beer(sqlData.get(i));
                        
                        BeerData.beer.add(beer);
                    }
                    
                    if ((BeerData.beer.size()==1)) {

                        BeerData.selectedBeer = BeerData.beer.get(0);
                        try {
                            mainScene.changeCenter("/com/group8/resources/views/beerDetails_center.fxml");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if ((BeerData.beer.size()>1)) {

                        // load the result stage
                        try {
                            mainScene.changeCenter("/com/group8/resources/views/result_center.fxml");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        });
    }
    /**
     * Created by Joseph Roberto Delatolas
     */
    public void onNot(){
    	if(notButton.isSelected()){
    		notWindow.setVisible(true);
    	}
    	else{
    		notWindow.setVisible(false);
    	}
    }
    
    /**
     * Created by Andreas Fransson.
     */
    public void showAdvanced()
    {
        error.setText("");

        if(advanced.isSelected()){
            randomButton.setVisible(false);
            searchForPubsCheckbox.setVisible(false);
            searchForUsersCheckbox.setVisible(false);
        }else{
            randomButton.setVisible(true);
            searchForPubsCheckbox.setVisible(true);
            searchForUsersCheckbox.setVisible(true);
        }

        // Handle diffrent casesof visability and selection
        if(!advancedDescription.isVisible() && !advancedType.isVisible() && !advancedProducer.isVisible())
        {
            advancedType.setVisible(true);
            advancedProducer.setVisible(true);
            advancedDescription.setVisible(true);
            all.setVisible(true);
            advancedName.setVisible(true);
            advancedCountry.setVisible(true);
            advancedType.setSelected(false);
            advancedProducer.setSelected(false);
            advancedDescription.setSelected(false);
            advancedName.setSelected(true);
            advancedCountry.setSelected(false);
            all.setSelected(false);
        }else
        {
            advancedType.setVisible(false);
            advancedProducer.setVisible(false);
            advancedDescription.setVisible(false);
            all.setVisible(false);
            advancedName.setVisible(false);
            advancedCountry.setVisible(false);
            advancedName.setSelected(true);
        }
    }

    /**
     * Created by Linus Eiderström Swahn.
     *
     * Is called when the user presses the pub search checkbox.
     *
     * Changes visibility of different elements.
     */
    @FXML
    public void pubsChecked()
    {
        advanced.setVisible(!searchForPubsCheckbox.isSelected());
        searchForUsersCheckbox.setVisible(!searchForPubsCheckbox.isSelected());

        search.setVisible(!searchForPubsCheckbox.isSelected());
        userSearchButton.setVisible(false);
        showAllUsers.setVisible(false);

        pubSearchButton.setVisible(searchForPubsCheckbox.isSelected());
        showAllPubs.setVisible(searchForPubsCheckbox.isSelected());
    }

    /**
     * Created by Linus Eiderström Swahn.
     *
     * Is called when the user presses the user search checkbox.
     *
     * Changes visibility of different elements.
     */
    @FXML
    public void userChecked()
    {
        advanced.setVisible(!searchForUsersCheckbox.isSelected());
        searchForPubsCheckbox.setVisible(!searchForUsersCheckbox.isSelected());

        search.setVisible(!searchForUsersCheckbox.isSelected());
        pubSearchButton.setVisible(false);
        showAllPubs.setVisible(false);

        userSearchButton.setVisible(searchForUsersCheckbox.isSelected());
        showAllUsers.setVisible(searchForUsersCheckbox.isSelected());
    }

    /**
     * Created by Andreas Fransson.
     */
    public void checkAll()
    {
        if ( !advancedType.isSelected() || !advancedProducer.isSelected() || !advancedDescription.isSelected()) {
            advancedType.setSelected(true);
            advancedProducer.setSelected(true);
            advancedDescription.setSelected(true);
            advancedName.setSelected(true);
            advancedCountry.setSelected(true);
        }else{
            advancedType.setSelected(false);
            advancedProducer.setSelected(false);
            advancedDescription.setSelected(false);
            advancedName.setSelected(false);
            advancedCountry.setSelected(false);
        }
    }

    /**
     * Created by Andreas Fransson.
     *
     */
    public void exitField()
    {
        if (searchText.getText().isEmpty()){
            searchText.setText("Search...");
        }
    }

    /**
     * Created by Andreas Fransson.
     *
     * On clicking the Search button execute query through MySqlDriver
     * @param event
     * @throws IOException
     */
    @FXML
    public void onSearch(javafx.event.ActionEvent event) throws IOException {

        // Set background service different from the UI fx thread to run stuff on
        backgroundThread = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        
                        if(!searchText.getText().equals("")){


                            //Get source of pressed button
                            Object source = event.getSource();

                            // load wheel until task is finished//
                            Load.setVisible(true);

                            // Fetch the user input
                            BeerData.searchInput = "";


                            /**
                             * SQL query
                             *
                             * Construct a query as a String dependent on user specifications
                             */

                            //If request comes from the barcode scanner
                            if (((Node) event.getSource()).getId() == beerScanSearchButton.getId()) {

                                BeerData.searchInput = "SELECT distinct `beerID`,`name`,`image`,`description`,beerTypeEN,countryName, percentage, producerName, volume, isTap, packageTypeEN, price, avStars, countryFlag, barcode" +
                                        " from beers, beerType, origin, package where " +
                                        "beers.beerTypeID = beerType.beerTypeID " +
                                        "and beers.originID = origin.originID " +
                                        "and beers.package = package.packageID " +
                                        "and (beers.barcode = " + barcode;
                            } else {
                                // name search is defualt
                                BeerData.searchInput = "SELECT distinct `beerID`,`name`,`image`,`description`,beerTypeEN,countryName, percentage, producerName, volume, isTap, packageTypeEN, price, avStars, countryFlag " +
                                        "from beers, beerType, origin, package " +
                                        "where beers.beerTypeID = beerType.beerTypeID " +
                                        "and beers.originID = origin.originID " +
                                        "and beers.package = package.packageID " +
                                        "and (";

                                if (advancedName.isSelected()) {
                                    BeerData.searchInput += "name like '%" + searchText.getText() + "%'";
                                }

                                // Advanced
                                if (advanced.isSelected()) {
                                    // For reasons
                                    int selectedIteams = 0;

                                    if (advancedCountry.isSelected()) {
                                        if (advancedName.isSelected() || advancedProducer.isSelected() || advancedType.isSelected() || advancedDescription.isSelected()) {
                                            BeerData.searchInput += " or countryName like '%" + searchText.getText() + "%'";
                                            selectedIteams++;
                                        } else {
                                            BeerData.searchInput += "countryName like '%" + searchText.getText() + "%'";
                                        }
                                    }

                                    if (advancedType.isSelected()) {
                                        if (advancedName.isSelected() || advancedProducer.isSelected() || advancedDescription.isSelected() || advancedCountry.isSelected()) {
                                            BeerData.searchInput += " or beerTypeEN like '%" + searchText.getText() + "%'";
                                            selectedIteams++;
                                        } else {
                                            BeerData.searchInput += "beerTypeEN like '%" + searchText.getText() + "%'";
                                        }
                                    }
                                    if (advancedProducer.isSelected()) {
                                        if (advancedName.isSelected() || advancedType.isSelected() || advancedDescription.isSelected() || advancedCountry.isSelected()) {
                                            BeerData.searchInput += " or producerName like '%" + searchText.getText() + "%'";
                                            selectedIteams++;
                                        } else {
                                            BeerData.searchInput += "producerName like '%" + searchText.getText() + "%'";
                                        }
                                    }
                                    if (advancedDescription.isSelected()) {
                                        if (advancedName.isSelected() || advancedProducer.isSelected() || advancedType.isSelected() || advancedCountry.isSelected()) {
                                            BeerData.searchInput += " or description like '%" + searchText.getText() + "%'";
                                            selectedIteams++;
                                        } else {
                                            BeerData.searchInput += "description like '%" + searchText.getText() + "%'";
                                        }
                                    }

                                    if (!advancedName.isSelected() && selectedIteams > 1) {

                                        BeerData.searchInput = BeerData.searchInput.substring(0, 260) + BeerData.searchInput.substring(262);
                                    }
                                }
                            }

                            // Added a 100 beer limit as a safety for now / maybe have pages also?
                            BeerData.searchInput += ") limit 100 ";

                            // Execute user query
                            ArrayList<ArrayList<Object>> sqlData;

                            sqlData = MysqlDriver.selectMany(BeerData.searchInput);

                            for (int i = 0; i < sqlData.size(); i++) {
                                // Add a new Beer to the beer arraylist
                                Beer beer = new Beer(sqlData.get(i));
                                // Testoutput
                                BeerData.beer.add(beer);
                            }
                        }

                        return null;
                    }
                };
            }
        };

        // When the thread is done try to go to next stage.
        backgroundThread.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {

                Load.setVisible(false);
                if(cameraOpen) {
                    try {

                        closeWebcam();

                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }

                // If only one result for a search, go straight to beer profile
                if ((BeerData.beer.size()==1)) {

                    BeerData.selectedBeer = BeerData.beer.get(0);
                    try {
                        mainScene.changeCenter("/com/group8/resources/views/beerDetails_center.fxml");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if ((BeerData.beer.size()>1)) {

                    // load the result stage
                    try {
                        mainScene.changeCenter("/com/group8/resources/views/result_center.fxml");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else
                {
                    advanced.setSelected(false);
                    advancedType.setVisible(false);
                    advancedProducer.setVisible(false);
                    advancedDescription.setVisible(false);
                    all.setVisible(false);
                    advancedName.setVisible(false);
                    advancedCountry.setVisible(false);
                    advancedName.setSelected(true);

                    //load.setVisible(false);
                    error.setText("No result for: " + searchText.getText());
                }
            }
        });

        // Start thread
        backgroundThread.start();
    }

    /**
     * Created by Linus Eiderström Swahn.
     *
     * Gets called when the user presses the search for pubs button.
     *
     * Searches the pub database where the pub name is simillar to the search string.
     */
    @FXML
    public void pubSearch()
    {
        // Set background service diffrent from the UI fx thread to run stuff on( i know indentation is retarded)
        backgroundThread = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {


                        // load wheel until task is finished//
                        Load.setVisible(true);

                        if(!searchText.getText().equals("")) {


                            String listOfPub = "select pubID,`name`,image, `phoneNumber`, `description`, `offers`, `entrenceFee` from pubs " +
                                    "where name like '%" + searchText.getText().toLowerCase() + "%';";

                            ArrayList<ArrayList<Object>> SQLData4;

                            SQLData4 = MysqlDriver.selectMany(listOfPub);
                            ArrayList<Pub> pubListDetails = new ArrayList<Pub>();

                            for (int i = 0; i < SQLData4.size(); i++) {
                                // Add a new Beer to the beer arraylist
                                Pub pub = new Pub(SQLData4.get(i));
                                pubListDetails.add(pub);
                            }

                            PubData.pubs = pubListDetails;
                        }
                        else
                        {
                            PubData.pubs = new ArrayList<Pub>();
                        }
                        return null;
                    }
                };
            }
        };

        backgroundThread.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                Load.setVisible(false);

                if(PubData.pubs.size()==0)
                {
                    error.setText("No Pubs Found!");
                    error.setVisible(true);
                }
                else if(PubData.pubs.size()==1)
                {
                    PubData.selectedPub = PubData.pubs.get(0);

                    try {
                        mainScene.changeCenter("/com/group8/resources/views/pubDetailView.fxml");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {

                    // load the result stage
                    try {
                        mainScene.changeCenter("/com/group8/resources/views/pubList.fxml");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // Start thread
        backgroundThread.start();
    }

    /**
     * Created by Linus Eiderström Swahn.
     *
     * Gets called when the user presses the search for users button.
     *
     * Searches the user database where the username or full name is simillar to the search string.
     */
    @FXML
    public void userSearch()
    {
        // Set background service diffrent from the UI fx thread to run stuff on( i know indentation is retarded)
        backgroundThread = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {


                        // load wheel until task is finished//
                        Load.setVisible(true);

                        if(!searchText.getText().equals("")) {


                            String listOfPub = "select * from users " +
                                    "where username like '%" + searchText.getText().toLowerCase() + "%' " +
                                    "or fullName like '%" + searchText.getText().toLowerCase() + "%';";

                            ArrayList<ArrayList<Object>> SQLData4;

                            SQLData4 = MysqlDriver.selectMany(listOfPub);
                            ArrayList<User> userListDetails = new ArrayList<User>();

                            for (int i = 0; i < SQLData4.size(); i++) {
                                // Add a new Beer to the beer arraylist
                                User user = new User(SQLData4.get(i));
                                userListDetails.add(user);
                            }

                            UserData.users = userListDetails;
                        }
                        else
                        {
                            UserData.users = new ArrayList<User>();
                        }
                        return null;
                    }
                };
            }
        };

        backgroundThread.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                Load.setVisible(false);

                if(UserData.users.size()==0)
                {
                    error.setText("No USers Found!");
                    error.setVisible(true);
                }
                else if(UserData.users.size()==1)
                {
                    UserData.selected = UserData.users.get(0);

                    try {
                        mainScene.changeCenter("/com/group8/resources/views/OtherUser.fxml");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {

                    // load the result stage
                    try {
                        mainScene.changeCenter("/com/group8/resources/views/userList.fxml");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // Start thread
        backgroundThread.start();
    }
    /**
     * Created by Mantas Namgaudis
     * Loads the random beer generator
     * @param event
     * @throws Exception
     */
    @FXML
    public void onRandom (ActionEvent event) throws Exception{

        mainScene.changeCenter("/com/group8/resources/views/RandomBeerScenes/scene1.fxml");
    }

    /**
     * Created by Collins
     * Show all existing Pubs
     * @param event
     * @throws Exception
     */
    @FXML
    public void showAllPubs (ActionEvent event) throws Exception{
        // Set background service diffrent from the UI fx thread to run stuff on( i know indentation is retarded)
        backgroundThread = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {

                        // load wheel until task is finished//
                        Load.setVisible(true);

                        /**
                         * select all pubs from the database
                         */
                        String listOfPub = "select pubID,`name`,image, `phoneNumber`, `description`, `offers`, `entrenceFee` from pubs";
                        ArrayList<ArrayList<Object>> SQLData4;

                        SQLData4 = MysqlDriver.selectMany(listOfPub);
                        ArrayList<Pub> pubListDetails = new ArrayList<Pub>();

                        for (int i = 0; i < SQLData4.size(); i++) {
                            // Add a new Beer to the pub arraylist
                            Pub pub = new Pub(SQLData4.get(i));
                            pubListDetails.add(pub);
                        }

                        PubData.pubs = pubListDetails;

                        return null;
                    }
                };
            }
        };

        backgroundThread.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                Load.setVisible(false);


                // load the result stage
                try {
                    mainScene.changeCenter("/com/group8/resources/views/pubList.fxml");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Start thread
        backgroundThread.start();
    }

    /**
     *Created by Colllins
     * @param event
     * @throws IOException
     */
    @FXML
    public void showAllUsers(ActionEvent event) throws IOException
    {
        // Set background service diffrent from the UI fx thread to run stuff on( i know indentation is retarded)
        backgroundThread = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {

                        // load wheel until task is finished//
                        Load.setVisible(true);
                         String userList="select * from users where isPub="+0;

                           ArrayList<ArrayList<Object>> users;
                          users =	MysqlDriver.selectMany(userList);
                          ArrayList<User> allUsers = new ArrayList<User>();

                            for (int i = 0; i < users.size(); i++) {

                           User newUser = new User(users.get(i));

                          allUsers.add(newUser);
        }

                        UserData.users = allUsers;
                        return null;
                    }
                };
            }
        };

        backgroundThread.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                Load.setVisible(false);


                // load the result stage
                try {
                    mainScene.changeCenter("/com/group8/resources/views/userList.fxml");
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

        // Start thread
        backgroundThread.start();


    }



    /**
     * Created by Andreas Fransson.
     * Auto clear fields when selected
     * Clear the Search field
     */
    public void clearFieldSearch()
    {
        exitField();

        if (searchText.getText().equals("Search...")) {
            searchText.setText("");
        }
    }

    /**
     * Created by Mantas Namgaudis
     *
     * Execute search on pressing "Enter" key.
     */
    @FXML
    public void searchEnterPressed(KeyEvent event){
        if (event.getCode() == KeyCode.ENTER) {

            if(searchForUsersCheckbox.isSelected())
            {
                userSearchButton.setDefaultButton(true);
            }
            else if(searchForPubsCheckbox.isSelected())
            {
                pubSearchButton.setDefaultButton(true);
            }
            else
            {
                search.setDefaultButton(true);
            }
        }
    }

    /**
     * Created by Mantas Namgaudis
     *
     * Opens and sets properties for the beer scanner window.
     * @param event
     * @throws Exception
     */
    @FXML
    public void onBeerScan (ActionEvent event) throws Exception {

        cameraOpen = true;
        webCam = new SwingNode();
        pane = new BorderPane();
        workaroundButton = new Button();
        workaroundButton.setOnAction(e -> beerScanSearchButton.fire());
        Button webcamClose = new Button("X");
        webcamClose.setOnAction(e -> closeWebcam());


        //Dim background
        Navigation.primaryStage.setOpacity(0);


        // Webcam window settings
        webcamStage = new Stage();
        webcamScene = new Scene(pane, 400, 250);
        webcamStage.setScene(webcamScene);
        webcamStage.initStyle(StageStyle.TRANSPARENT);
        webcamStage.setAlwaysOnTop(true);
        webcamScene.setFill(Color.TRANSPARENT);
        pane.setCenter(webCam);
        pane.setTop(webcamClose);
        pane.setBorder(new Border(new BorderStroke(Paint.valueOf("orange"), BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(3))));
        pane.setBackground(new Background(new BackgroundFill(Paint.valueOf("black"), new CornerRadii(8), null)));

        webcamStage.show();

        getWebcam(webCam);
    }

    /**
     * Created by Mantas Namgaudis
     * Runs swing component as javafx node.
     * @param webcam
     */
    private void getWebcam(final SwingNode webcam) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                BeerScanner scanner = new BeerScanner();
                webcam.setContent(scanner.panel);
            }
        });
    }
    /**
     * Created by Mantas Namgaudis
     */
    public static void setBarcode(String string){
        barcode = string;
    }

    /**
     * Created by Mantas Namgaudis
     */
    public static void checkIfbarcodeIsSet(){

        boolean running = true;
        do {
            if(barcode == null){
                continue;
            }
            else{
                try {
                    workaroundButton.fire();
                    running = false;
                }
                catch (NullPointerException e){
                }
            }
        }
        while (running);
    }
    /**
     * Created by Mantas Namgaudis
     */
    public void closeWebcam() {
        webcamStage.close();
        Navigation.primaryStage.setOpacity(1);
        BeerScanner.disconnectWebcam();
    }

    /**
     * Created by Andreas Fransson and Joseph Roberto Delatolas separately.
     *  Initialize Main controller
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Reset the BeerData Arraylist
        BeerData.beer = new ArrayList<Beer>();
        Navigation.current_CenterFXML = "/com/group8/resources/views/home_center.fxml";
        beerScanSearchButton.setVisible(false);
       if(UserData.userInstance != null){
    	   notButton.setDisable(false);
           String sqlSt = "Select distinct notification from notifications where userId = "+ UserData.userInstance.getId() +";";
           ArrayList<ArrayList<Object>> notSql = MysqlDriver.selectMany(sqlSt);
           notNum = notSql.size();
           notButton.setText(notNum+"");
           notifications.setText("You have "+notNum+" Notifications");
           ObservableList<String> notMessage = FXCollections.observableArrayList();
           for(int i=0; i<notNum;i++){
        	   notMessage.add(notSql.get(i).get(0).toString());
           }
           notList.setItems(notMessage);
       }else{
    	   notButton.setDisable(true);
           notWindow.setVisible(false);
       }
        Load.setStyle("-fx-accent: IVORY");
    }
}
