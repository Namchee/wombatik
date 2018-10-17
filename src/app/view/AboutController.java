package app.view;

import app.App;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class AboutController {
    @FXML
    private Hyperlink link;
    
    private App app;
    
    @FXML
    private void initialize() {
        
    }
    
    @FXML
    private void handleLink() {
        System.out.println("test");
        WebView view = new WebView();
        final WebEngine engine = view.getEngine();
        this.app.getHostServices().showDocument("https://github.com/Namchee/wombatik");
    }
    
    public void setMainApp(App app) {
        this.app = app;
    }
}
