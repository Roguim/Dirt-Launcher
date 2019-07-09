package net.dirtcraft.dirtlauncher.backend.data;

import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import net.cydhra.nidhogg.exception.InvalidCredentialsException;
import net.dirtcraft.dirtlauncher.Controller;
import net.dirtcraft.dirtlauncher.backend.Utils.Verification;
import net.dirtcraft.dirtlauncher.backend.objects.Account;

public class LoginButtonHandler {
    private static boolean initialized = false;
    private static TextField usernameField;
    private static PasswordField passwordField;
    private static Button playButton;

    public static Account onClick() {
        if (!initialized){
            usernameField = Controller.getInstance().getUsernameField();
            passwordField = Controller.getInstance().getPasswordField();
            playButton = Controller.getInstance().getPlayButton();
        }
        Account userAccount = null;

        String email = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        try {
            userAccount = Verification.login(email, password);
        } catch (InvalidCredentialsException e) {
            System.out.println("o shit u been hacked!\n"+e);
        } catch (IllegalArgumentException e){
            System.out.println("o shit no credentialz\n"+e);
        }

        //TODO do stuff with the userAccount

        return userAccount;
    }
}
