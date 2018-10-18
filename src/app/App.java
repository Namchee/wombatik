package app;

import java.io.IOException;

import app.view.AboutController;
import app.view.DecoderController;
import app.view.EncoderController;
import app.view.OverviewController;
import app.view.RootController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class App extends Application {
    
    private Stage primaryStage;
    private BorderPane rootLayout;

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Wombatik - by Namchee");
		
		showMainWindow();
		showOverview();
	}
	
	private void showMainWindow() {
	    try {
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(App.class.getResource("view/RootWindow.fxml"));
	        this.rootLayout = (BorderPane)loader.load();
	        
	        Scene mainScene = new Scene(this.rootLayout);
	        this.primaryStage.setScene(mainScene);
	        this.primaryStage.getIcons().add(new Image("wombat.png"));
	        
	        RootController controller = loader.getController();
	        controller.setMainApp(this);
	        controller.setStage(this.primaryStage);
	        
	        this.primaryStage.show();
	    } catch (IOException e) {
	        System.out.println("Loading main window failed!");
	    }
	}
	
	private void showOverview() {
	    try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(App.class.getResource("view/OverviewWindow.fxml"));
            AnchorPane overview = (AnchorPane)loader.load();
            
            OverviewController mainController = loader.getController();
            mainController.setMainApp(this);
            
            this.rootLayout.setCenter(overview);
        } catch (IOException e) {
            System.out.println("Loading main window failed!");
        }
	}
	
	public void showEncoderWindow() {
	    try {
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(App.class.getResource("view/EncoderWindow.fxml"));
	        AnchorPane encoderWindow = (AnchorPane)loader.load();
	        
	        Stage encoderStage = new Stage();
	        encoderStage.getIcons().add(new Image("wombat.png"));
	        encoderStage.setTitle("Encode - Wombatik");
	        encoderStage.initOwner(this.primaryStage);
	        encoderStage.initModality(Modality.WINDOW_MODAL);
	        
	        Scene encoderScene = new Scene(encoderWindow);
	        encoderStage.setScene(encoderScene);
	        
	        EncoderController controller = loader.getController();
	        controller.setStage(encoderStage);
	        
	        encoderStage.showAndWait();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public void showDecoderWindow() {
	    try {
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(getClass().getResource("view/DecoderWindow.fxml"));
	        AnchorPane decoderWindow = (AnchorPane)loader.load();
	        
	        Stage decoderStage = new Stage();
	        decoderStage.getIcons().add(new Image("wombat.png"));
	        decoderStage.setTitle("Decoder - Wombatik");
	        decoderStage.initOwner(this.primaryStage);
	        decoderStage.initModality(Modality.WINDOW_MODAL);
	        
	        Scene decoderScene = new Scene(decoderWindow);
	        decoderStage.setScene(decoderScene);
	        
	        DecoderController controller = loader.getController();
	        controller.setStage(decoderStage);
	        
	        decoderStage.showAndWait();
	    } catch (IOException e) {
	        System.out.println("Loading decoder window failed!");
	    }
	}
	
	public void showAboutWindow() {
	    try {
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(getClass().getResource("view/AboutWindow.fxml"));
	        AnchorPane pane = (AnchorPane)loader.load();
	        
	        Stage aboutStage = new Stage();
	        aboutStage.getIcons().add(new Image("wombat.png"));
	        aboutStage.initOwner(this.primaryStage);
	        aboutStage.setTitle("About this cool app");
	        aboutStage.initModality(Modality.WINDOW_MODAL);
	        
	        Scene aboutScene = new Scene(pane);
	        aboutStage.setScene(aboutScene);
	        
	        AboutController controller = loader.getController();
	        controller.setMainApp(this);
	        
	        aboutStage.showAndWait();
	    } catch (IOException e) {
	        System.out.println("Loading about window failed!");
	    }
	}

	public static void main(String[] args) {
		launch(args);
	}
}
