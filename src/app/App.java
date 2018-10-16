package app;

import java.io.IOException;

import app.view.DecoderController;
import app.view.EncoderController;
import app.view.OverviewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class App extends Application {
    
    private Stage primaryStage;

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Wombatik - by Namchee");
		
		showMainWindow();
	}
	
	private void showMainWindow() {
	    try {
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(App.class.getResource("view/OverviewWindow.fxml"));
	        AnchorPane overview = (AnchorPane)loader.load();
	        
	        Scene mainScene = new Scene(overview);
	        this.primaryStage.setScene(mainScene);
	        
	        OverviewController mainController = loader.getController();
	        mainController.setMainApp(this);
	        
	        this.primaryStage.show();
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

	public static void main(String[] args) {
		launch(args);
	}
}
