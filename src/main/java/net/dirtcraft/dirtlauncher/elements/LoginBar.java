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
import net.dirtcraft.dirtlauncher.backend.objects.Account;
import net.dirtcraft.dirtlauncher.backend.objects.LoginError;
import net.dirtcraft.dirtlauncher.backend.utils.Config;

import java.io.*;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class LoginBar extends Pane {
    private final TextField usernameField;
    private final PasswordField passField;
    private final PlayButton actionButton;
    private final YggdrasilClient client;
    private Pack activePackCell;
    private Account account;

    public LoginBar() {
        Future<Account> accountFuture = loadAccountData();
        activePackCell = null;//ripblock
        client = new YggdrasilClient() ;
        passField = new PasswordField();
        usernameField = new TextField();
        actionButton = new PlayButton(this);
        GridPane loginContainer = new GridPane();

        //Force the size - otherwise it changes and that's bad..
        setAbsoluteSize(actionButton , 58 ,  59 );
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
        loginContainer.add(actionButton, 1, 0,  1, 2);
        loginContainer.add(usernameField, 0, 0, 1, 1);
        loginContainer.add(passField , 0,  1,  1,  1);
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
            account = verifySession(accountFuture.get());
        } catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
            account = null;
        }

    }

    private Future<Account> loadAccountData() {
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
                if (!config.has("uuid")) throw new JsonParseException("No uuid");
                if (!config.has("username")) throw new JsonParseException("No username");
                if (!config.has("sessionID")) throw new JsonParseException("No sessionID");
                if (!config.has("sessionAlias")) throw new JsonParseException("No sessionAlias");
                if (!config.has("sessionAccessToken")) throw new JsonParseException("No sessionAccessToken");
                if (!config.has("sessionClientToken")) throw new JsonParseException("No sessionClientToken");

                String uuid = config.get("uuid").getAsString();
                String username = config.get("username").getAsString();
                String sessionID = config.get("sessionID").getAsString();
                String sessionAlias = config.get("sessionAlias").getAsString();
                String sessionAccessToken = config.get("sessionAccessToken").getAsString();
                String sessionClientToken = config.get("sessionClientToken").getAsString();

                Session session = new Session(sessionID, sessionAlias, sessionAccessToken, sessionClientToken);
                return new Account(session, username, UUID.fromString(uuid), true);

            } catch (IOException | JsonParseException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    private void saveAccountData(Account account){
        //serialize the account.
        Config settings = Main.getSettings();
        File jsonFile = settings.getAccountJson();
        JsonObject config;
        try (FileWriter writer = new FileWriter(jsonFile)) {
            config = new JsonObject();

            config.addProperty("uuid", account.getUuid().toString());
            config.addProperty("username", account.getUsername());
            config.addProperty("sessionID", account.getSession().getId());
            config.addProperty("sessionAlias", account.getSession().getAlias());
            config.addProperty("sessionAccessToken", account.getSession().getAccessToken());
            config.addProperty("sessionClientToken", account.getSession().getClientToken());

            writer.write(config.toString());
        } catch (IOException e) {
            Main.getLogger().warn(e);
        }
    }

    public boolean hasAccount(){
        return account != null;
    }

    private Account verifySession(Account account) {
        if (account != null) {
            try {
                if (client.validate(account.getSession())) {
                    usernameField.pseudoClassStateChanged(PseudoClass.getPseudoClass("authenticated"), true);
                    passField.pseudoClassStateChanged(PseudoClass.getPseudoClass("authenticated"), true);
                    new Thread(() -> saveAccountData(account)).start();
                    return account;
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                try {
                    client.refresh(account.getSession());
                    if (client.validate(account.getSession())) {
                        usernameField.pseudoClassStateChanged(PseudoClass.getPseudoClass("authenticated"), true);
                        passField.pseudoClassStateChanged(PseudoClass.getPseudoClass("authenticated"), true);
                        new Thread(() -> saveAccountData(account)).start();
                        return account;
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        usernameField.pseudoClassStateChanged(PseudoClass.getPseudoClass("authenticated"), false);
        passField.pseudoClassStateChanged(PseudoClass.getPseudoClass("authenticated"), false);
        return null;
    }

    public Optional<Account> getAccount() throws InvalidCredentialsException {
        account = verifySession(account);
        if (account == null) {
            try{
                final Session session = client.login(new AccountCredentials(usernameField.getText().trim(), passField.getText().trim()), YggdrasilAgent.MINECRAFT);
                account = new Account(session, session.getAlias(), session.getUuid(), true);
                new Thread(() -> saveAccountData(account)).start();
                usernameField.pseudoClassStateChanged(PseudoClass.getPseudoClass("authenticated"), true);
                passField.pseudoClassStateChanged(PseudoClass.getPseudoClass("authenticated"), true);
                return Optional.of(account);
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
        else return Optional.of(account);
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

        this.actionButton.setType(type, pack);
    }

    public void updatePlayButton(PlayButton.Types types){
        actionButton.setType(types, activePackCell);
    }
}
