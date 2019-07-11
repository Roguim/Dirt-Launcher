package net.dirtcraft.dirtlauncher.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import jfxtras.styles.jmetro8.JMetro;

public class Install {

    private static Install instance;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private void initialize() {
        instance = this;

        JMetro jMetro = new JMetro(JMetro.Style.DARK);
        jMetro.applyTheme(progressBar);

        progressBar.setProgress(50);

    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public Install getInstance() {
        return instance;
    }

}
