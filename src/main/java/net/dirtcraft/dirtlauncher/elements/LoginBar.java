package net.dirtcraft.dirtlauncher.elements;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import net.cydhra.nidhogg.MojangClient;
import net.cydhra.nidhogg.YggdrasilAgent;
import net.cydhra.nidhogg.YggdrasilClient;
import net.cydhra.nidhogg.data.AccountCredentials;
import net.cydhra.nidhogg.data.NameEntry;
import net.cydhra.nidhogg.data.Session;
import net.cydhra.nidhogg.exception.InvalidCredentialsException;
import net.cydhra.nidhogg.exception.UserMigratedException;
import net.dirtcraft.dirtlauncher.Controllers.Home;
import net.dirtcraft.dirtlauncher.backend.objects.Account;
import net.dirtcraft.dirtlauncher.backend.objects.LoginError;
import net.dirtcraft.dirtlauncher.backend.utils.Constants;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class LoginBar extends Pane {
    private GridPane loginContainer;
    private TextField usernameField;
    private PasswordField passField;
    private PlayButton actionButton;
    private Pack activePackCell;

    public LoginBar() {
        activePackCell = null;//ripblock
        passField = new PasswordField();
        usernameField = new TextField();
        actionButton = new PlayButton(this);
        loginContainer = new GridPane();

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

    }

    @Nullable
    public Account login() {
        Account account = null;

        String email = usernameField.getText().trim();
        String password = passField.getText().trim();

        try {
            account = login(email, password);
        } catch (InvalidCredentialsException e) {
            Home.getInstance().getNotificationBox().displayError(LoginError.INVALID_CREDENTIALS, null);
        } catch (IllegalArgumentException e) {
            Home.getInstance().getNotificationBox().displayError(LoginError.ILLEGAL_ARGUMENT, null);
        } catch (UserMigratedException e) {
            Home.getInstance().getNotificationBox().displayError(LoginError.USER_MIGRATED, null);
        }

        return account;
    }

    private Account login(String email, String password) throws InvalidCredentialsException {

        YggdrasilClient client = new YggdrasilClient();

        Session session = client.login(new AccountCredentials(email, password), YggdrasilAgent.MINECRAFT);
        final UUID uuid = session.getUuid();

        MojangClient mojangClient = new MojangClient(session.getClientToken());

        List<NameEntry> names = mojangClient.getNameHistoryByUUID(uuid);

        Account account = new Account(session, mojangClient, names.get(names.size() - 1).getName(), password, uuid, client.validate(session));

        if (Constants.VERBOSE) {
            System.out.println("USERNAME: " + account.getUsername());
            System.out.println("PASSWORD: " + account.getPassword());
            System.out.println("UUID: " + account.getUuid());
            System.out.println("IS AUTHENTICATED: " + account.isAuthenticated());
        }
        return account;

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
