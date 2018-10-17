package app.view;

import app.App;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

public class RootController {
    @FXML
    private MenuItem about;
    @FXML
    private MenuItem exit;
    
    private App main;
    private Stage rootStage;
    
    @FXML
    private void initialize() {
        
    }
    
    @FXML
    private void handleClose() {
        this.rootStage.close();
    }
    
    @FXML
    private void handleAbout() {
        this.main.showAboutWindow();
    }
    
    public void setStage(Stage stage) {
        this.rootStage = stage;
    }
    
    public void setMainApp(App app) {
        this.main = app;
    }
}
