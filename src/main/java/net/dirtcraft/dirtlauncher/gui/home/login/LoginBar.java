package net.dirtcraft.dirtlauncher.gui.home.login;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import net.cydhra.nidhogg.data.AccountCredentials;
import net.cydhra.nidhogg.exception.InvalidCredentialsException;
import net.cydhra.nidhogg.exception.UserMigratedException;
import net.dirtcraft.dirtlauncher.Data.Account;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.game.objects.LoginError;
import net.dirtcraft.dirtlauncher.gui.home.sidebar.Pack;
import net.dirtcraft.dirtlauncher.utils.Constants;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;

import java.util.Optional;

public final class LoginBar extends Pane {
    private final TextField usernameField;
    private final PasswordField passField;
    private final ActionButton actionButton;
    private final LogoutButton logout;
    private final GridPane loginContainer;
    private Pack activePackCell;

    public LoginBar() {
        actionButton = new ActionButton();
        activePackCell = null;
        passField = new PasswordField();
        usernameField = new TextField();
        loginContainer = new GridPane();
        logout = new LogoutButton(this);

        //Force the size - otherwise it changes and that's bad..
        MiscUtils.setAbsoluteSize(this, 264.0, 74 );
        MiscUtils.setAbsoluteSize(loginContainer, 250.0, 59);

        setId(Constants.CSS_ID_LOGIN_BAR);
        passField.setId("PasswordField");
        usernameField.setId("UsernameField");

        RowConstraints x1 = new RowConstraints();
        RowConstraints x2 = new RowConstraints();
        ColumnConstraints y1 = new ColumnConstraints();
        ColumnConstraints y2 = new ColumnConstraints();
        x1.setValignment(VPos.BOTTOM);
        x2.setValignment(VPos.TOP);
        y2.setHalignment(HPos.LEFT);
        y1.setHalignment(HPos.RIGHT);
        x1.setMinHeight(29);
        x1.setMaxHeight(29);        // This is how u make a grid pane without  \\
        x2.setMinHeight(29);        // scene builder. It's hard work but hey,  \\
        x2.setMaxHeight(29);        // It's an honest living. Also this space  \\
        y1.setMinWidth(190);        // was perfect for a comment block because \\
        y1.setMaxWidth(190);        // it just is screaming for someone to put \\
        y2.setMinWidth(60);        // something in this exact box shaped area \\
        y2.setMaxWidth(60);
        loginContainer.getRowConstraints().add(0, x1);
        loginContainer.getRowConstraints().add(1, x2);
        loginContainer.getColumnConstraints().add(0, y1);
        loginContainer.getColumnConstraints().add(1, y2);
        loginContainer.setLayoutX(8);
        loginContainer.setLayoutY(8);

        usernameField.setPromptText("E-Mail Address");
        passField.setPromptText("Password");
        actionButton.setDefaultButton(true);
        actionButton.setDisable(true);
        actionButton.setText("Play");
        getChildren().setAll(loginContainer);
        usernameField.setOnKeyTyped(this::setKeyTypedEvent);
        passField.setOnKeyPressed(this::setKeyTypedEvent);

        SimpleBooleanProperty firstTime =  new SimpleBooleanProperty(true);
        usernameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue && firstTime.get()){
                this.requestFocus();
                firstTime.setValue(false);
            }
        });
    }

    private void setKeyTypedEvent(KeyEvent event) {
        if (!getActivePackCell().isPresent()) {
            actionButton.setDisable(true);
            return;
        }

        if (!MiscUtils.isEmptyOrNull(usernameField.getText().trim(), passField.getText().trim()) || Main.getAccounts().hasSelectedAccount()) {
            actionButton.setDisable(false);
            actionButton.setOnAction(e -> getActionButton().fire());
        } else actionButton.setDisable(true);
    }

    public void setInputs(){
        Optional<Account> session = Main.getAccounts().getSelectedAccount();
        loginContainer.getChildren().clear();
        actionButton.refresh();
        if (session.isPresent()){
            final int barSize = 252;
            final int logoutSize = 35;
            actionButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("authenticated"), true);
            actionButton.setTranslateX(-logoutSize);
            MiscUtils.setAbsoluteSize(actionButton , barSize-logoutSize ,  59 );
            MiscUtils.setAbsoluteSize(logout , logoutSize ,  59 );
            loginContainer.add(actionButton, 0, 0,  2, 2);
            loginContainer.add(logout, 0, 0,  2, 2);
            this.actionButton.setType(session.get());
            if (activePackCell != null) actionButton.setDisable(false);
        } else {
            MiscUtils.setAbsoluteSize(actionButton, 58, 59);
            actionButton.setTranslateX(0);
            loginContainer.add(usernameField, 0, 0, 1, 1);
            loginContainer.add(passField, 0, 1, 1, 1);
            loginContainer.add(new LoginButton(this), 1, 0, 1, 2);
            actionButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("authenticated"), false);
            this.actionButton.setType(null);
            actionButton.setDisable(true);
        }
    }

    public ActionButton getActionButton() {
        return actionButton;
    }

    public PasswordField getPassField() {
        return passField;
    }

    public TextField getUsernameField() {
        return usernameField;
    }

    public Optional<Pack> getActivePackCell() {
        return Optional.ofNullable(activePackCell);
    }

    public void setActivePackCell(Pack pack) {
        this.activePackCell = pack;
        ActionButton.Types type;

        if (!pack.isInstalled()) type = ActionButton.Types.INSTALL;
        else if (pack.isOutdated()) type = ActionButton.Types.UPDATE;
        else type = ActionButton.Types.PLAY;

        this.actionButton.setType(type, pack);
    }

    public void logOut(){
        Main.getAccounts().clearSelectedAccount();
        setInputs();
    }

    public void login(){
        try {
            AccountCredentials credentials = new AccountCredentials(usernameField.getText(), passField.getText());
            Main.getAccounts().setSelectedAccount(credentials);
        } catch (Exception e) {
            Main.getHome().getNotificationBox().displayError(LoginError.from(e));
        }
        setInputs();
    }

    public void updatePlayButton(ActionButton.Types types){
        actionButton.setType(types, activePackCell);
    }
}
