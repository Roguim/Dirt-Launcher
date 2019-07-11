package net.dirtcraft.dirtlauncher.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;


public class Settings {
    private Double settingsXOffset;
    private Double settingsYOffset;
    @FXML
    private Button closeButton;

    @FXML
    private ToolBar fooBar;

    @FXML
    private void initialize() {
        closeButton.setOnMouseClicked(e->Home.getInstance().getSettingsMenu().close());
    }

    public Double getSettingsXOffset(){
        return settingsXOffset;
    }
    public Double getSettingsYOffset(){
        return settingsYOffset;
    }
    public void setSettingsYOffset(Double value){
        settingsYOffset = value;
    }
    public void setSettingsXOffset(Double value){
        settingsXOffset = value;
    }
}
