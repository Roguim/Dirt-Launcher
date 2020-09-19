package net.dirtcraft.dirtlauncher.gui.home.sidebar;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.game.modpacks.ModpackManager;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class PackList extends ScrollPane {
    private Future<List<PackSelector>> listPreload;
    private final VBox packs;
    public PackList(){
        packs = new VBox();
        packs.getStyleClass().add(Constants.CSS_CLASS_VBOX);
        packs.setFocusTraversable(false);
        packs.setAlignment(Pos.TOP_CENTER);


        listPreload = CompletableFuture.supplyAsync(()-> {
            ModpackManager manager = ModpackManager.getInstance();
            return manager.getModpacks().stream()
                    .map(PackSelector::new)
                    .sorted(PackSelector::compareTo)
                    .collect(Collectors.toList());
        });

        setFitToWidth(true);
        setFocusTraversable(false);
        setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setPannable(true);
        MiscUtils.setAbsoluteWidth(this, 300);
        AnchorPane.setTopAnchor(this, 100d);
        AnchorPane.setLeftAnchor(this, 0d);
        AnchorPane.setBottomAnchor(this, 0d);
        setContent(packs);
    }

    public void updatePacksAsync(){
        CompletableFuture.runAsync(()-> {
            if (listPreload != null) {
                Platform.runLater(()->{
                    try {
                        packs.getChildren().clear();
                        packs.getChildren().addAll(listPreload.get());
                    } catch (Exception e){
                        e.printStackTrace();
                        updatePacksAsync();
                        return;
                    }
                    listPreload = null;
                });
                return;
            }

            List<PackSelector> packsList = ModpackManager.getInstance().getModpacks().stream()
                        .map(PackSelector::new)
                        .sorted(PackSelector::compareTo)
                        .collect(Collectors.toList());
            Platform.runLater(() -> {
                packs.getChildren().clear();
                packs.getChildren().addAll(packsList);
            });
        });
    }

    public void update(){
        updatePacksAsync();
    }
}
