package app.view;

import app.App;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;

public class AboutController {
    @FXML
    private Hyperlink link;
    
    private App app;
    
    @FXML
    private void initialize() {
        
    }
    
    @FXML
    private void handleLink() {
        this.app.getHostServices().showDocument("https://github.com/Namchee/wombatik");
    }
    
    public void setMainApp(App app) {
        this.app = app;
    }
}
