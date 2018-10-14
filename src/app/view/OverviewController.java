package app.view;

import javafx.fxml.FXML;
import app.App;

public class OverviewController {
    
    private App main;
    
    public OverviewController() {
        
    }
    
    @FXML
    private void initialize() {
        
    }
    
    @FXML
    private void handleEncode() {
        this.main.showEncoderWindow();
    }
    
    @FXML
    private void handleDecode() {
        this.main.showDecoderWindow();
    }
    
    public void setMainApp(App app) {
        this.main = app;
    }
}
