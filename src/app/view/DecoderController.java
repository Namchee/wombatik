package app.view;

import javafx.fxml.FXML;
import javafx.stage.Stage;

public class DecoderController {
    
    private Stage decoderStage;
    
    @FXML
    private void initialize() {
    }
    
    @FXML
    private void handleCancel() {
        this.decoderStage.close();
    }
    
    public void setStage(Stage stage) {
        this.decoderStage = stage;
    }
}
