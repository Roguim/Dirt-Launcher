package net.dirtcraft.dirtlauncher.elements;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import net.cydhra.nidhogg.YggdrasilAgent;
import net.cydhra.nidhogg.YggdrasilClient;
import net.cydhra.nidhogg.data.AccountCredentials;
import net.cydhra.nidhogg.data.Session;
import net.cydhra.nidhogg.exception.InvalidCredentialsException;
import net.cydhra.nidhogg.exception.UserMigratedException;
import net.dirtcraft.dirtlauncher.Controllers.Home;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.backend.objects.LoginError;
import net.dirtcraft.dirtlauncher.backend.utils.Config;

import java.io.*;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class LoginBar extends Pane {
    private final TextField usernameField;
    private final PasswordField passField;
    private final PlayButton actionButton;
    private final YggdrasilClient client;
    private final LogoutButton logout;
    private final GridPane loginContainer;
    private Pack activePackCell;
    private Session session;

    public LoginBar() {
        Future<Session> accountFuture = loadAccountData();
        actionButton = new PlayButton(this);
        activePackCell = null;//ripblock
        passField = new PasswordField();
        usernameField = new TextField();
        loginContainer = new GridPane();
        client = new YggdrasilClient();
        logout = new LogoutButton(this);

        //Force the size - otherwise it changes and that's bad..
        setAbsoluteSize(this ,264.0 ,  74 );
        setAbsoluteSize(loginContainer,250.0, 59);

        setId("LoginBar");
        getStyleClass().add("LoginArea");
        getStyleClass().add( "LoginBar");
        passField.setId("PasswordField");
        usernameField.setId("UsernameField");

        RowConstraints x1 = new RowConstraints();
        RowConstraints x2 = new RowConstraints();
        ColumnConstraints y1 = new ColumnConstraints();
        ColumnConstraints y2 = new ColumnConstraints();
        x1.setValignment(VPos.BOTTOM);
        x2.setValignment ( VPos.TOP );
        y2.setHalignment( HPos.LEFT );
        y1.setHalignment(HPos.RIGHT );
        x1.setMinHeight(29);
        x1.setMaxHeight(29);        // This is how u make a grid pane without  \\
        x2.setMinHeight(29);        // scene builder. It's hard work but hey,  \\
        x2.setMaxHeight(29);        // It's an honest living. Also this space  \\
        y1.setMinWidth(190);        // was perfect for a comment block because \\
        y1.setMaxWidth(190);        // it just is screaming for someone to put \\
        y2.setMinWidth( 60);        // something in this exact box shaped area \\
        y2.setMaxWidth( 60);
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

        SimpleBooleanProperty firstTime =  new SimpleBooleanProperty(true);
        usernameField.focusedProperty().addListener((observable,  oldValue,  newValue) -> {
            if(newValue && firstTime.get()){
                this.requestFocus();
                firstTime.setValue(false);
            }
        });
        try {
            session = verifySession(accountFuture.get());
        } catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
            session = null;
        }

    }

    public void logOut(){
        client.invalidate(session);
        this.session = null;
        setInputs(false, null);
    }

    private void setInputs(boolean value, Session session){
        loginContainer.getChildren().clear();
        if (!value){
            setAbsoluteSize(actionButton , 58 ,  59 );
            actionButton.setTranslateX(0);
            loginContainer.add(usernameField, 0, 0, 1, 1);
            loginContainer.add(passField , 0,  1,  1,  1);
            loginContainer.add(actionButton, 1, 0,  1, 2);
            actionButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("authenticated"), false);
            this.actionButton.setType(session);
        } else {
            final int barSize = 252;
            final int logoutSize = 35;
            actionButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("authenticated"), true);
            actionButton.setTranslateX(-logoutSize);
            setAbsoluteSize(actionButton , barSize-logoutSize ,  59 );
            setAbsoluteSize(logout , logoutSize ,  59 );
            loginContainer.add(actionButton, 0, 0,  2, 2);
            loginContainer.add(logout, 0, 0,  2, 2);
            this.actionButton.setType(session);
        }
    }

    private Future<Session> loadAccountData() {
        return CompletableFuture.supplyAsync(()-> {
            //deserialize the account.
            Config settings = Main.getSettings();
            File jsonFile = settings.getAccountJson();
            if (!jsonFile.exists()) return null;
            JsonObject config;
            try (FileReader reader = new FileReader(jsonFile)) {
                JsonParser parser = new JsonParser();
                config = parser.parse(reader).getAsJsonObject();
                if (config == null) throw new JsonParseException("Json was null");
                if (!config.has("sessionID")) throw new JsonParseException("No sessionID");
                if (!config.has("sessionAlias")) throw new JsonParseException("No sessionAlias");
                if (!config.has("sessionAccessToken")) throw new JsonParseException("No sessionAccessToken");
                if (!config.has("sessionClientToken")) throw new JsonParseException("No sessionClientToken");

                String sessionID = config.get("sessionID").getAsString();
                String sessionAlias = config.get("sessionAlias").getAsString();
                String sessionAccessToken = config.get("sessionAccessToken").getAsString();
                String sessionClientToken = config.get("sessionClientToken").getAsString();

                return new Session(sessionID, sessionAlias, sessionAccessToken, sessionClientToken);

            } catch (IOException | JsonParseException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public void setSession(Session session){
        //TODO: Not absolutely fucking nothing.
    }

    private void saveAccountData(Session session){
        //serialize the account.
        Config settings = Main.getSettings();
        File jsonFile = settings.getAccountJson();
        JsonObject config;
        try (FileWriter writer = new FileWriter(jsonFile)) {
            config = new JsonObject();

            config.addProperty("sessionID", session.getId());
            config.addProperty("sessionAlias", session.getAlias());
            config.addProperty("sessionAccessToken", session.getAccessToken());
            config.addProperty("sessionClientToken", session.getClientToken());

            writer.write(config.toString());
        } catch (IOException e) {
            Main.getLogger().warn(e);
        }
    }

    public boolean hasAccount(){
        return session != null;
    }

    private Session verifySession(Session session) {
        if (session != null) {
            try {
                if (client.validate(session)) {
                    setInputs(true, session);
                    new Thread(() -> saveAccountData(session)).start();
                    return session;
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                try {
                    client.refresh(session);
                    if (client.validate(session)) {
                        setInputs(true, session);
                        new Thread(() -> saveAccountData(session)).start();
                        return session;
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        setInputs(false, session);
        return null;
    }

    public Optional<Session> getAccount() throws InvalidCredentialsException {
        session = verifySession(session);
        if (session == null) {
            try{
                session = client.login(new AccountCredentials(usernameField.getText().trim(), passField.getText().trim()), YggdrasilAgent.MINECRAFT);
                new Thread(() -> saveAccountData(session)).start();
                setInputs(true, session);
                return Optional.of(session);
            } catch (InvalidCredentialsException e) {
                Home.getInstance().getNotificationBox().displayError(LoginError.INVALID_CREDENTIALS, null);
            } catch (IllegalArgumentException e) {
                Home.getInstance().getNotificationBox().displayError(LoginError.ILLEGAL_ARGUMENT, null);
            } catch (UserMigratedException e) {
                Home.getInstance().getNotificationBox().displayError(LoginError.USER_MIGRATED, null);
            } catch (Exception e){
                System.out.println(e.getMessage());
            }
            return Optional.empty();
        }
        else return Optional.of(session);
    }

    private void setAbsoluteSize(Region node, double width, double height){
        node.setPrefSize(width, height);
        node.setMaxSize(width,  height);
        node.setMinSize(width,  height);
    }

    public PlayButton getActionButton() {
        return actionButton;
    }

    public PasswordField getPassField() {
        return passField;
    }

    public TextField getUsernameField() {
        return usernameField;
    }

    public Optional<Pack> getActivePackCell() {
        if (activePackCell!=null) return Optional.of(activePackCell);
        else return Optional.empty();
    }

    public void setActivePackCell(Pack pack) {
        this.activePackCell = pack;
        PlayButton.Types type;

        if (!pack.isInstalled()) type = PlayButton.Types.INSTALL;
        else if (pack.isOutdated()) type = PlayButton.Types.UPDATE;
        else type = PlayButton.Types.PLAY;

        this.actionButton.setType(type, pack, session);
    }

    public void updatePlayButton(PlayButton.Types types){
        actionButton.setType(types, activePackCell, session);
    }
}
