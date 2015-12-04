package com.group8.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

import com.group8.database.tables.Beer;
import com.lynden.gmapsfx.MainApp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;


public class PubInfo extends BaseController implements Initializable{
	public TextField pubID;
	public TextField pubName;
	public TextField pubAddress;
	public TextField pubPhoneNumber;
	public TextField pubDescription;
	public TextField pubOffer;
	
	public TextField pubEntranceFee;
	
	public Button pubSaveNew;
	public Button addBeer;
	@FXML
	public Button getMap;
	@FXML
    public ImageView pubImage;
	ImageView img= new ImageView((this.getClass().getResource("/com/group8/resources/Images/Icon_2.png").toString()));





	//table for beers in Pub
    public TableView<Beer> beerTable;
    @FXML
    public TableColumn<Beer, String> beerName;
    @FXML
    public TableColumn<Beer, String> beerType;
    @FXML
    public TableColumn<Beer, String> beerOrigin;
    @FXML
    public TableColumn<Beer, String> beerProducer;
    @FXML
    public TableColumn<Beer, String> beerPackage;
    @FXML
    public TableColumn<Beer, String> beerPercentage;
    @FXML
    public TableColumn<Beer, String> avrageRank;
    @FXML
    public TableColumn<Beer,Image> beerImage;
    @FXML
    public PieChart showPie;
    @FXML
    public Label userName;

	// Latlong
	double latitude = PubData.loggedInPub.get_geoLat();
	double longitude = PubData.loggedInPub.get_geoLong();
	
	Image imgLoad;
	FileInputStream imageStream ;
	File file;
	 public ObservableList<Beer> masterData = FXCollections.observableArrayList(UserData.userInstance.favourites);
	
	
	 public void getRow(){
	        beerTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
	            // Select item will only be displayed when dubbleclicked

	            /**
	             * Dubleclick event
	             * @param event
	             */
	            @Override
	            public void handle(MouseEvent event) {
	                if (event.getClickCount() == 2) {
	                        // Show that we can select items and print it
	                        //System.out.println("clicked on " + beerTable.getSelectionModel().getSelectedItem());
	                        // Set the selectedBeer instance of beer we have to selected item
	                        BeerData.selectedBeer = beerTable.getSelectionModel().getSelectedItem();
	                        // Load the details scene
	                        // Has to be in a tr / catch becouse of the event missmatch, ouseevent cant throw IOexceptions
	                        try {
	  							mainScene.changeCenter("/com/group8/resources/views/beerDetails_center.fxml");
	                        } catch (IOException e) {
	                            // Print error msg
	                            e.printStackTrace();
	                        }
	                }
	            }

	        });


	}


	public void initialize(URL location, ResourceBundle resources) {
				
		pubName.setText(PubData.loggedInPub.get_name());
		pubPhoneNumber.setText(PubData.loggedInPub.get_phoneNumber());
		pubAddress.setText(PubData.loggedInPub.get_address());
		pubDescription.setText(PubData.loggedInPub.get_description());
		pubOffer.setText(PubData.loggedInPub.get_offer());
		pubEntranceFee.setText(""+PubData.loggedInPub.get_entranceFee());
		pubImage.setImage(PubData.loggedInPub.getImage());
		System.out.println(PubData.loggedInPub.getImage()+"    why IMAGE");

		//testtLabel.setText(BeerData.beersInPubDetails.get_price());
		
        Navigation.backFXML = "/com/group8/resources/views/favourites.fxml";
        Navigation.resultviewFXML = "/com/group8/resources/views/pubDetailView.fxml";

        // You have to have a get function that is named get +" type" for it to work sets values.
        beerName.setCellValueFactory(new PropertyValueFactory<Beer, String>("Name"));
        beerType.setCellValueFactory(new PropertyValueFactory<Beer, String>("Type"));
        beerOrigin.setCellValueFactory(new PropertyValueFactory<Beer, String>("Origin"));
        beerProducer.setCellValueFactory(new PropertyValueFactory<Beer, String>("Producer"));
        beerPackage.setCellValueFactory(new PropertyValueFactory<Beer, String>("BeerPackage"));
        avrageRank.setCellValueFactory(new PropertyValueFactory<Beer, String>("AvRank"));
        beerPercentage.setCellValueFactory(new PropertyValueFactory<Beer, String>("Percentage"));


        // Try loading the image, if there is none will use placeholder
        beerImage.setCellValueFactory(new PropertyValueFactory<Beer, Image>("Image"));
        /**
         * Set the Cellfactory
         */
        beerImage.setCellFactory(new Callback<TableColumn<Beer, Image>, TableCell<Beer, Image>>() {
            @Override
            public TableCell<Beer, Image> call(TableColumn<Beer, Image> param) {
                TableCell<Beer, Image> cell = new TableCell<Beer, Image>() {

                    /**
                     * Override the updateItem method to set a imageView
                     * @param item
                     * @param empty
                     */
                    @Override
                    public void updateItem(Image item, boolean empty) {

                        if(!empty) {
                            if (item != null) {
                                VBox vb = new VBox();
                                vb.setAlignment(Pos.CENTER);
                                ImageView imgVw = new ImageView();
                                imgVw.setImage(item);
                                imgVw.setFitWidth(20);
                                imgVw.setFitHeight(40);
                                vb.getChildren().addAll(imgVw);
                                setGraphic(vb);

                            } else {
                                VBox vb = new VBox();
                                vb.setAlignment(Pos.CENTER);
                                ImageView imgVw = new ImageView();
                                imgVw.setImage(new Image(new File("src/com/group8/resources/Images/beerHasNoImage.png").toURI().toString()));
                                imgVw.setFitWidth(20);
                                imgVw.setFitHeight(40);
                                // Test Output
                                //System.out.println(imgVw.getImage().toString());
                                vb.getChildren().addAll(imgVw);
                                setGraphic(vb);

                            }
                        }
                    }
                };
                return cell;
            }

        });

        //Populate the Tableview
        beerTable.setItems(masterData);

    }
		
	
	
		 
		
	public void updatePub(ActionEvent event) throws SQLException, ClassNotFoundException{
		
		float entrance = Float.parseFloat(pubEntranceFee.getText());
		
		Byte[] emptyImage = new Byte[0];
		String pubInfo = "";
		int pubID = UserData.userInstance.get_pubId();

		// Setup the mysel connection
		String url = "jdbc:mysql://sql.smallwhitebird.com:3306/beerfinder";
        String user = "Gr8";
        String password = "group8";
        
		Class.forName("com.mysql.jdbc.Driver"); 
		Connection con = DriverManager.getConnection(url, user, password);
		
		// Make the sql statement
		PreparedStatement statement = con.prepareStatement("UPDATE `pubs` SET `name`='"+
				pubName.getText()+"',`phoneNumber`='"+
				pubPhoneNumber.getText()+"',`image`=?,`description`='"+
				pubDescription.getText()+"',`offers`='"+
				pubOffer.getText()+"',`entrenceFee`="+entrance+" WHERE pubID='"+
				UserData.userInstance.get_pubId()+"';");
		statement.setBinaryStream(1, imageStream, (int) file.length());
		//System.out.println(pubInfo);
		statement.executeUpdate();
		statement = con.prepareStatement("UPDATE `pubAddress` SET `address`='"+pubAddress.getText()+"',`longitude`='" + longitude + "',`latitude`='" + latitude +"' where addressID=" + PubData.loggedInPub.get_adressId());
		statement.executeUpdate();

		// Show confirmation
		img.setFitWidth(60);
		img.setFitHeight(60);
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Update Complete");
		alert.setHeaderText("Alert!");
		alert.setContentText("Your pub has been updated!");
		alert.setGraphic(img);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image("file:src/com/group8/resources/Images/Icon.png"));
		alert.showAndWait();

	}


	public void loadPubImage(ActionEvent event)throws IOException {

		// Start a filechooser
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("open image file");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

		Stage primaryStage=new Stage();
		file= fileChooser.showOpenDialog(primaryStage);


		imageStream = new FileInputStream(file);

		// Supported formats include JPEG , PNG, JPG
		if (file.isFile() && (file.getName().contains(".jpg")
				||file.getName().contains(".png")
				||file.getName().contains(".jpeg")
		)){


			String thumbURL = file.toURI().toURL().toString();
			//	System.out.println(thumbURL);
			Image imgLoad = new Image(thumbURL);
			pubImage.setImage(imgLoad);
		}
	} // end of method

	/**
	 * Load the addBeer scene
	 * --> Used to change the center FXML to the addBeer FXML alt "scene"
	 * @param event
	 * @throws IOException
	 */
	public void onAddBeer(ActionEvent event) throws IOException{
		mainScene.changeCenter("/com/group8/resources/views/addBeer.fxml");
         
	}

		public void getMap(javafx.event.ActionEvent event) throws IOException{

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("/com/group8/resources/views/AddressMap.fxml"));
			BorderPane page = loader.load();
			// Create the dialog Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Add Your Location");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(Navigation.primaryStage);
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:src/com/group8/resources/Images/Icon.png"));
			// Show the dialog and wait until the user closes it
			dialogStage.show();


			// Some real cool shit

			// When exeting the window update the address and fetch the latlong
			dialogStage.setOnHidden(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					if(BeerData.Address != null) {

						// Store the address as lat and long doubles
						latitude = BeerData.Address.getLatitude();
						longitude = BeerData.Address.getLongitude();
						System.out.println(latitude + " " + longitude);
					}
				}
			});

		}
}

	


		
	
	
	





	

