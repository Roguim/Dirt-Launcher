package net.dirtcraft.dirtlauncher.gui.home.login;

import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.game.authentification.Account;
import net.dirtcraft.dirtlauncher.gui.home.sidebar.PackSelector;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;

import java.util.Optional;

public final class LoginBar extends Pane {
    private final ActionButton actionButton;
    private final LogoutButton logout;
    private final GridPane loginContainer;
    private PackSelector activePackCell;

    public LoginBar() {
        actionButton = new ActionButton();
        activePackCell = null;
        loginContainer = new GridPane();
        logout = new LogoutButton(this);

        //Force the size - otherwise it changes and that's bad..
        MiscUtils.setAbsoluteSize(this, 264.0, 74 );
        MiscUtils.setAbsoluteSize(loginContainer, 250.0, 59);

        setId(Constants.CSS_ID_LOGIN_BAR);

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
        y2.setMinWidth(60);         // something in this exact box shaped area \\
        y2.setMaxWidth(60);
        loginContainer.getRowConstraints().add(0, x1);
        loginContainer.getRowConstraints().add(1, x2);
        loginContainer.getColumnConstraints().add(0, y1);
        loginContainer.getColumnConstraints().add(1, y2);
        loginContainer.setLayoutX(8);
        loginContainer.setLayoutY(8);

        actionButton.setDefaultButton(true);
        //actionButton.setDisable(true);
        actionButton.setText("Play");
        getChildren().setAll(loginContainer);
    }

    public void setInputs() {
        Optional<Account> session = Main.getAccounts().getSelectedAccount();
        loginContainer.getChildren().clear();
        actionButton.refresh();
        boolean account = session.isPresent();
        final int barSize = 252;
        final int logoutSize = 35;
        actionButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("authenticated"), account);
        loginContainer.add(actionButton, 0, 0, 2, 2);
        if (account) {
            actionButton.setTranslateX(-logoutSize);
            MiscUtils.setAbsoluteSize(actionButton, barSize - logoutSize, 59);
            MiscUtils.setAbsoluteSize(logout, logoutSize, 59);
            this.actionButton.setType(session.get());
            loginContainer.add(logout, 0, 0, 2, 2);
        } else {
            actionButton.setTranslateX(0);
            MiscUtils.setAbsoluteSize(actionButton, barSize, 59);
            this.actionButton.setType(ActionButton.Types.LOGIN, null);
        }
    }

    public ActionButton getActionButton() {
        return actionButton;
    }

    public Optional<PackSelector> getActivePackCell() {
        return Optional.ofNullable(activePackCell);
    }

    public void setActivePackCell(PackSelector pack) {
        this.activePackCell = pack;
        ActionButton.Types type;

        if (!pack.getModpack().isInstalled()) type = ActionButton.Types.INSTALL;
        else if (!pack.getModpack().isDependantsInstalled()) type = ActionButton.Types.REPAIR;
        else if (pack.getModpack().isOutdated()) type = ActionButton.Types.UPDATE;
        else type = ActionButton.Types.PLAY;

        this.actionButton.setType(type, pack);
    }

    public void login(){
        Main.getAccounts().login();
    }
}
