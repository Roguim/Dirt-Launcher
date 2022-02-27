package net.dirtcraft.dirtlauncher.gui.genericControls;

import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;

public final class NumberField extends TextField {
    public NumberField(Number number){
        super(number.toString());
        init();
    }
    public NumberField(){
        super();
        init();
    }

    private void init(){
        setOnKeyTyped(event -> {
            if (event.getCharacter().matches("\\D*")) event.consume();
        });
    }

    @Override
    public void paste(){
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString() && clipboard.getString().matches("\\d*")) super.paste();
    }

    public double getAsDouble(){
        return Double.parseDouble(getText());
    }

    public long getAsLong(){
        return Long.parseLong(getText());
    }

    public int getAsInt(){
        return Integer.parseInt(getText());
    }
}
