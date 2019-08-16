package net.dirtcraft.dirtlauncher.gui.home.toolbar;

import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.dirtcraft.dirtlauncher.Data.Config;
import net.dirtcraft.dirtlauncher.gui.genericControls.NumberField;
import net.dirtcraft.dirtlauncher.utils.Constants;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;

import java.io.File;

public final class Settings extends Scene {
    private Stage stage = null;
    private final Config config;
    private final TextField gamesDirInputField;
    private final TextField javaArgsInput;
    private final NumberField xmsInput;
    private final NumberField xmxInput;
    public Settings(Config config) {
        super(new FlowPane(), 600, 300);

        this.config = config;

        Text titleText = new Text("Settings");
        FlowPane title = new FlowPane();
        title.setAlignment(Pos.CENTER);
        title.setId(Constants.CSS_CLASS_TITLE);
        title.getChildren().add(titleText);


        xmsInput = new NumberField(config.getMinimumRam());
        xmsInput.setMinSize(200, 30);
        xmsInput.setMaxSize(200, 30);
        Text xmsText = new Text("Minimum Allocated Ram (MB)");
        xmsText.getStyleClass().add(Constants.CSS_CLASS_TEXT);
        FlowPane xms = new FlowPane();
        xms.setMinWidth(300);
        xms.setMaxWidth(300);
        xms.setAlignment(Pos.CENTER);
        xms.setColumnHalignment(HPos.CENTER);
        xms.setOrientation(Orientation.VERTICAL);
        xms.setVgap(3);
        xms.getChildren().addAll(xmsInput, xmsText);

        xmxInput = new NumberField(config.getMaximumRam());
        xmxInput.setMinSize(200, 30);
        xmxInput.setMaxSize(200, 30);
        Text xmxText = new Text("Maximum Allocated Ram (MB)");
        xmxText.getStyleClass().add(Constants.CSS_CLASS_TEXT);
        FlowPane xmx = new FlowPane();
        xmx.setMinWidth(300);
        xmx.setMaxWidth(300);
        xmx.setAlignment(Pos.CENTER);
        xmx.setColumnHalignment(HPos.CENTER);
        xmx.setOrientation(Orientation.VERTICAL);
        xmx.setVgap(3);
        xmx.getChildren().addAll(xmxInput, xmxText);

        GridPane ram = new GridPane();
        RowConstraints ramRow = new RowConstraints();
        ramRow.setValignment(VPos.CENTER);
        ColumnConstraints left = new ColumnConstraints();
        left.setHalignment(HPos.CENTER);
        left.setMaxWidth(300);
        left.setMinWidth(300);
        ColumnConstraints right = new ColumnConstraints();
        right.setHalignment(HPos.CENTER);
        right.setMaxWidth(300);
        right.setMinWidth(300);
        ram.getColumnConstraints().addAll(left, right);
        ram.getRowConstraints().add(ramRow);
        ram.add(xms, 0 ,0);
        ram.add(xmx, 1 ,0);

        javaArgsInput = new TextField(config.getJavaArguments());
        javaArgsInput.setMinSize(300, 30);
        javaArgsInput.setMaxSize(300, 30);

        Text javaArgsText = new Text("Java Arguments");
        javaArgsText.getStyleClass().add(Constants.CSS_CLASS_TEXT);

        FlowPane javaArgs = new FlowPane();
        javaArgs.setAlignment(Pos.CENTER);
        javaArgs.setColumnHalignment(HPos.CENTER);
        javaArgs.setOrientation(Orientation.VERTICAL);
        javaArgs.setVgap(3);
        javaArgs.getChildren().addAll(javaArgsInput, javaArgsText);

        gamesDirInputField = new TextField(config.getGameDirectory().toString());
        gamesDirInputField.setMinSize(270, 30);
        gamesDirInputField.setMaxSize(270, 30);

        Button gamesDirInputButton = new Button("...");
        gamesDirInputButton.setOnAction(this::onGameDirectoryFolderGuiRequested);
        gamesDirInputButton.setMinSize(30, 29);
        gamesDirInputButton.setMaxSize(30, 29);
        gamesDirInputButton.setTranslateX(270);
        gamesDirInputButton.setFocusTraversable(false);

        Pane gamesDirInput = new Pane();
        gamesDirInput.setMinSize(300, 30);
        gamesDirInput.setMaxSize(300, 30);
        gamesDirInput.getChildren().addAll(gamesDirInputField, gamesDirInputButton);
        gamesDirInput.setId(Constants.CSS_ID_SETTINGS_GMDR);

        Text gamesDirText = new Text("Game Directory");
        gamesDirText.getStyleClass().add(Constants.CSS_CLASS_TEXT);

        FlowPane gamesDir = new FlowPane();
        gamesDir.setAlignment(Pos.CENTER);
        gamesDir.setColumnHalignment(HPos.CENTER);
        gamesDir.setOrientation(Orientation.VERTICAL);
        gamesDir.setVgap(3);
        gamesDir.getChildren().addAll(gamesDirInput, gamesDirText);

        FlowPane root = (FlowPane) getRoot();
        root.setAlignment(Pos.TOP_CENTER);
        root.setOrientation(Orientation.VERTICAL);
        root.setId(Constants.CSS_ID_ROOT);
        root.getStylesheets().add(MiscUtils.getResourcePath(Constants.JAR_CSS_FXML, "Settings", "Global.css"));
        root.getChildren().addAll(title, ram, javaArgs, gamesDir);
    }

    public Stage getStage() {
        if (stage == null) {
            stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("Dirt Launcher Settings");
            stage.getIcons().setAll(MiscUtils.getImage(Constants.JAR_ICONS, "settings.png"));
            stage.setScene(this);
            stage.setResizable(false);
            stage.setOnCloseRequest(event -> {
                final int minimumRam = xmsInput.getAsInt();
                final int maximumRam = xmxInput.getAsInt();
                final String gameDirectory = gamesDirInputField.getText();
                final String javaArguments = javaArgsInput.getText();
                config.updateSettings(minimumRam, maximumRam, javaArguments, gameDirectory);
            });
        }
        return stage;
    }

    private void onGameDirectoryFolderGuiRequested(ActionEvent event){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(gamesDirInputField.getText()));
        File selectedFolder = directoryChooser.showDialog(stage);
        if (selectedFolder != null) gamesDirInputField.setText(selectedFolder.getPath());
    }
}
