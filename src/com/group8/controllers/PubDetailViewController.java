package com.group8.controllers;

import com.group8.database.MysqlDriver;
import com.group8.singletons.BeerData;
import com.group8.singletons.Navigation;
import com.group8.singletons.PubData;
import com.group8.singletons.UserData;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created by Felipe Benjamin
 * Pub Detail View
 * --> Showing details about a specific chosen pub
 */
public class PubDetailViewController extends BaseController implements Initializable{

	/**
	 * Make a connection to FXML elements existing inside pubDetailView.fxml
	 */
	@FXML
	public CheckBox follow;
	@FXML
    public ImageView pubImage;
	@FXML
	public TextArea descriptionArea;
	@FXML
	public Label pubNameLabel;
	@FXML
	public Label pubAddressLabel;
	@FXML
	public Label pubOffersLabel;
	@FXML
	public Label pubEntranceFeeLabel;
	@FXML
	public Label phoneNumberLabel;
	@FXML
	public Button addToFavouritesButton, removeFromFavourites;
	@FXML
	public Label added;

	/**
	 * Created by Felipe Benjamin
	 *	Place out all information about selectedPub in respective fields
	 * @param location
	 * @param resources
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {

		Navigation.current_CenterFXML = "/com/group8/resources/views/pubDetailView.fxml";

	  	pubImage.setImage(PubData.selectedPub.getImage());
		pubNameLabel.setText(PubData.selectedPub.getName());
		pubAddressLabel.setText(PubData.selectedPub.get_address());
		pubOffersLabel.setText(PubData.selectedPub.getOffer());
		pubEntranceFeeLabel.setText("" +PubData.selectedPub.get_entranceFee());
		phoneNumberLabel.setText(PubData.selectedPub.getPhoneNumber());
		descriptionArea.setText(PubData.selectedPub.getDescription());

		if(UserData.userInstance!=null) {
			if (!UserData.userInstance.getIsPub()) {
				if(notAddedToFavourites())
				{
					addToFavouritesButton.setVisible(true);
				}
				else
				{
					removeFromFavourites.setVisible(true);
					follow.setVisible(true);
					if(MysqlDriver.select("select subed from favouritePub where pubID= "+PubData.selectedPub.getPubId()+" and userId= "+UserData.userInstance.getId()+";").toString()=="true"){
						follow.setSelected(true);
						System.out.println("Tadaaaa!");
					}else{
						follow.setSelected(false);
						System.out.println("TadaNOT");
					
					}
				}
			}
		}
	}
 	@FXML
    public void toggleA(ActionEvent event) throws IOException{
		String toggleA = "Update favouritePub set subed= "+(follow.isSelected()?1:0)+" where pubID = "+PubData.selectedPub.getPubId()+" and userId = "+UserData.userInstance.getId()+";";
    	MysqlDriver.update(toggleA);
    	follow.setText("Subscribed");
    	if(!follow.isSelected()){
    		follow.setText("Unsubscribed");
    	}
    }

	public void onFavourites(ActionEvent event) throws Exception
	{
		if(UserData.userInstance!=null)
		{
			String sqlQuery = "insert into favouritePub values(" + PubData.selectedPub.getPubId() + ", " + UserData.userInstance.getId() + ", 1);";
			
			MysqlDriver.insert(sqlQuery);

			added.setText("Added to favourites!");
			added.setVisible(true);

			follow.setVisible(true);
			follow.setSelected(true);
			
			addToFavouritesButton.setVisible(false);
			removeFromFavourites.setVisible(true);
		}
	}

	public void removeFromFavourites(ActionEvent event) throws Exception
	{
		if(UserData.userInstance!=null)
		{
			String sqlQuery = "delete from favouritePub where pubID = " + PubData.selectedPub.getPubId() + " and userId = " + UserData.userInstance.getId() + ";";

			MysqlDriver.update(sqlQuery);

			added.setText("Removed from favourites!");
			added.setVisible(true);

			follow.setVisible(false);

			removeFromFavourites.setVisible(false);

			addToFavouritesButton.setVisible(true);
		}
	}

	/**
	 * Created by Linus Eiderström Swahn.
	 *
	 * Checks if the pub is all ready in the users favourite list.
	 * @return
	 */
	public boolean notAddedToFavourites(){
		String selectQuery = "Select * from favouritePub where userID = '" +UserData.userInstance.getId()+"' and pubID = '"+PubData.selectedPub.getPubId() + "';";
		if(RegisterUserController.checkAvailability(selectQuery)){
			return true;
		}else{
			return false;
		}
	}
}
