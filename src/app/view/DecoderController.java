package app.view;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import app.model.ImageDecoder;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class DecoderController {
    
    @FXML
    private Label fileSourceLabel;
    
    @FXML
    private MenuButton algorithm;
    
    @FXML
    private TextField hash;
    
    private Stage decoderStage;
    private File source;
    
    @FXML
    private void initialize() {
        this.fileSourceLabel.setText("");
        MenuItem md2 = new MenuItem("MD2");
        MenuItem md5 = new MenuItem("MD5");
        MenuItem sha1 = new MenuItem("SHA-1");
        MenuItem sha256 = new MenuItem("SHA-256");
        MenuItem sha384 = new MenuItem("SHA-384");
        MenuItem sha512 = new MenuItem("SHA-512");
        this.algorithm.getItems().addAll(md2, md5, sha1, sha256, sha384, sha512);
        for (MenuItem item : this.algorithm.getItems()) {
            item.setOnAction(a -> {
                this.algorithm.setText(item.getText());
            });
        }
    }
    
    @FXML
    private void chooseSource() {
        FileChooser source = new FileChooser();
        source.setTitle("Select source...");
        source.setInitialDirectory(new File(System.getProperty("user.home")));
        source.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", 
                "*.jpg", 
                "*.jpeg",
                "*.gif",
                "*.png",
                "*.bmp")
        );
        File selected = source.showOpenDialog(new Stage());
        if (selected != null) {
            this.fileSourceLabel.setText(selected.getName());
            this.source = selected;
        }
    }
    
    @FXML
    private void handleOK() {
        if (this.source == null || this.algorithm.getText().equals("Select Algorithm...") || this.hash.getText().isEmpty()) {
            this.showError("Please select source file, hash algorithm, and destination file correctly (do not leave any of them empty)");
        } else {
            try {
                ImageDecoder decoder = new ImageDecoder(ImageIO.read(this.source));
                String res = decoder.decode(this.hash.getText(), this.algorithm.getText());
                if (res == null) {
                    this.notAuthorized();
                } else {
                    this.authorized();
                }
            } catch (IOException e) {
                this.showError("Failed to read source image, either the image is non-existent or you don't have permission to read it");
            }
            
        }
    }
    
    @FXML
    private void handleCancel() {
        this.decoderStage.close();
    }
    
    public void setStage(Stage stage) {
        this.decoderStage = stage;
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        alert.showAndWait();
    }
    
    private void notAuthorized() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("You don't have the creative rights of this content");
        String res = "";
        res += "There are several reasons for this : " + "\n\n";
        res += "1. The image is tampered," + "\n";
        res += "2. Hash is mistyped," + "\n";
        res += "3. Indeed, you don't have any rights on this content.";
        alert.setContentText(res);
        
        alert.showAndWait();
        
        this.decoderStage.close();
    }
    
    private void authorized() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Success! The image is not tampered and you have creative right(s) on this content!");
        
        alert.showAndWait();
        
        this.decoderStage.close();
    }
}
