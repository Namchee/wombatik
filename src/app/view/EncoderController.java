package app.view;

import java.util.Calendar;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Optional;

import javax.imageio.ImageIO;

import app.model.ImageEncoder;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class EncoderController {
    
    @FXML
    private Label fileSourceLabel;
    
    @FXML
    private Label fileDestLabel;
    
    @FXML
    private Label watermarkSourceLabel;
    
    @FXML
    private Label characterCount;
    
    @FXML
    private MenuButton algorithm;
    
    @FXML
    private TextArea watermarkInput;
    
    private File source;
    private File dest;
    private File watermarkSource;
    private Stage encoderStage;
    private ImageEncoder encoder;
    
    private String watermark;
    
    @FXML
    private void initialize() { 
        this.source = null;
        this.dest = null;
        this.fileSourceLabel.setText("");
        this.fileDestLabel.setText("");
        this.watermarkSourceLabel.setText("");
        MenuItem md1 = new MenuItem("MD2");
        MenuItem md5 = new MenuItem("MD5");
        MenuItem sha1 = new MenuItem("SHA-1");
        MenuItem sha256 = new MenuItem("SHA-256");
        MenuItem sha384 = new MenuItem("SHA-384");
        MenuItem sha512 = new MenuItem("SHA-512");
        this.algorithm.getItems().addAll(md1, md5, sha1, sha256, sha384, sha512);
        for (MenuItem item : this.algorithm.getItems()) {
            item.setOnAction(a -> {
                this.algorithm.setText(item.getText());
            });
        }
        this.characterCount.textProperty().bind(Bindings.length(this.watermarkInput.textProperty()).asString());
    }
    
    @FXML
    private void handleCancel() {
        this.encoderStage.close();
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
    private void chooseDest() {
        FileChooser dest = new FileChooser();
        
        dest.setTitle("Select destination...");
        dest.setInitialDirectory(new File(System.getProperty("user.home")));
        
        ExtensionFilter bmp = new FileChooser.ExtensionFilter("BMP Images (*.bmp)", "*.bmp");
        ExtensionFilter png = new FileChooser.ExtensionFilter("PNG Images (*.png)", "*.png");
        
        dest.getExtensionFilters().addAll(bmp, png);
        
        File result = dest.showSaveDialog(new Stage());
        
        if (result != null) {
            this.dest = result;
            this.fileDestLabel.setText(result.getName());
        }
    }
    
    @FXML
    private void chooseWatermark() {
        FileChooser source = new FileChooser();
        source.setTitle("Select watermark file...");
        source.setInitialDirectory(new File(System.getProperty("user.home")));
        source.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Text File (.txt)", "*.txt"));
        File selected = source.showOpenDialog(new Stage());
        if (selected != null) {
            this.watermarkSourceLabel.setText(selected.getName());
            this.watermarkSource = selected;
        }
    }
    
    @FXML
    private void handleOK() {
        this.determineWatermark();
        
        if (this.dest == null || this.source == null || this.watermark == null) {
            this.showError("Please select source file, hash algorithm, and destination file correctly");
        } else if (this.watermark.length() > 65536) {
            this.showError("The message must not be longer than 65.536 character(s)");
        } else {
            // format correct!
            try {
                BufferedImage source = ImageIO.read(this.source);
                this.encoder = new ImageEncoder(source, this.watermark);
                BufferedImage res = this.encoder.encode_v2();
                
                if (res == null) {
                    this.showError("Failed to embed message, the message is too long for the source image");
                } else {
                    try {
                        // BUG HERE
                        ImageIO.write(res, "png", this.dest);
                        
                        // successful
                        this.showSuccess(ImageIO.read(this.source), res);
                    } catch (IOException e) {
                        this.showError("Failed to write result image. You don't have access to write on the destination directory");
                    }
                }
            } catch (IOException e) {
                this.showError("Failed to read source image, either the image is non-existent or you don't have permission to read it");
            }
        }
    }
   
    
    public void setStage(Stage stage) {
        this.encoderStage = stage;
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        Stage stage = (Stage)alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("warning.png"));
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        alert.showAndWait();
    }
    
    private void showSuccess(BufferedImage orig, BufferedImage mod) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        Stage stage = (Stage)alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("info.png"));
        alert.setTitle("Successful");
        alert.setHeaderText("Secret message successfully embedded");
        alert.setContentText("Details below (press Show Details button)");
        
        // generate the message
        String res = "Message embedding successful\n\n";
        res += "Time : " + Calendar.getInstance().getTime() + "\n";  
        res += "Source Path : " + this.source.getPath() + "\n";
        res += "Destination Path : " + this.dest.getPath() + "\n";
        res += "Message Embedded :\n\n";
        res += this.watermark + "\n\n";
        res += "Quality Measurements (PSNR) : " + String.format("%.5f", this.encoder.calculatePSNR(orig, mod)) + " dB\n";
        res += "Message Digest (" + this.algorithm.getText() + ") : " + this.encoder.createMessageDigest(this.watermark, this.algorithm.getText()) + "\n\n";
        res += "---DO NOT LOSE THE DIGEST!!!---\n";
        
        TextArea textArea = new TextArea(res);
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setFont(Font.font("Courier New", 12));

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 1);
        
        alert.getDialogPane().setExpandableContent(expContent);
     
        alert.showAndWait();
        
        this.createLog(res);
        
        this.showImage(mod);
        
        alert.close();
    }
    
    private void showImage(BufferedImage mod) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("View Image Now?");
        alert.setHeaderText(null);
        alert.setContentText("Watermark embedding completed successfully, do you want to view the image now?");

        ButtonType yes = new ButtonType("Yes");
        ButtonType no = new ButtonType("No");
        alert.getButtonTypes().setAll(yes, no);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == yes){
            try {
                Desktop.getDesktop().open(this.dest);
            } catch (IOException e) {
                System.out.println("Failed to open result image");
            }
        } 
    }
    
    private void createLog(String log) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Create Log?");
        alert.setHeaderText(null);
        alert.setContentText("Would you like to create a log?");
        
        ButtonType yes = new ButtonType("Yes");
        ButtonType no = new ButtonType("No");
        alert.getButtonTypes().setAll(yes, no);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == yes) {
            FileChooser dest = new FileChooser();
            dest.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Document (*.txt)", "*.txt"));
            DateFormat format = new SimpleDateFormat("yyyyMMdd");
            dest.setInitialFileName("watermark_log_" + format.format(Calendar.getInstance().getTime()) +".txt");
            File destination = dest.showSaveDialog(new Stage());
            if (destination != null) {
                BufferedWriter writer;
                try {
                    writer = new BufferedWriter(new FileWriter(destination));
                    String[] realLog = log.split("\n");
                    for (int i = 0; i < realLog.length; i++) {
                        writer.write(realLog[i]);
                        writer.newLine();
                    }
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void determineWatermark() {
        if (this.watermarkInput.getText().isEmpty() && this.watermarkSource == null) 
            this.watermark = null;
        
        else if (!this.watermarkInput.getText().isEmpty() && this.watermarkSource == null) 
            this.watermark = this.watermarkInput.getText();
        
        else if (this.watermarkInput.getText().isEmpty() && this.watermarkSource != null) {
            if (this.getWatermarkFromFile() == null) {
                this.handleWatermarkError();
            } else {
                this.watermark = this.getWatermarkFromFile();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Choose Watermark Source");
            alert.setHeaderText(null);
            alert.setContentText("From where do you want get the watermark from?");
            
            ButtonType manual = new ButtonType("Manual Input");
            ButtonType file = new ButtonType("File");
            
            alert.getButtonTypes().setAll(manual, file);
            
            Optional<ButtonType> choice = alert.showAndWait();
            
            if (choice.get() == manual) this.watermark = this.watermarkInput.getText();
            else {
                if (this.getWatermarkFromFile() == null) this.handleWatermarkError();
                else this.watermark = this.getWatermarkFromFile();
            }
        }
    }
    
    private String getWatermarkFromFile() {
        String help;
        String watermark = "";
        BufferedReader rd;
        try {
            rd = new BufferedReader(new FileReader(this.watermarkSource));
            try {
                while ((help = rd.readLine()) != null) {
                    if (!watermark.isEmpty()) watermark += "\n";
                    watermark += help;
                }
                rd.close();
                return watermark;
            } catch (IOException e) {
                return null;
            }
        } catch (FileNotFoundException e) {
            return null;
        }
    }
    
    private void handleWatermarkError() {
        this.showError("Error reading watermark from source file");
    }
}
