package app.view;

import javafx.fxml.FXML;
import javafx.stage.Stage;

public class EncoderController {
    
    private Stage encoderStage;
    
    @FXML
    private void initialize() { 
    }
    
    @FXML
    private void handleCancel() {
        this.encoderStage.close();
    }
    
    public void setStage(Stage stage) {
        this.encoderStage = stage;
    }
}
