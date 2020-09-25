package net.dirtcraft.dirtlauncher.gui.home.sidebar;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.game.modpacks.ModpackManager;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class PackList extends ScrollPane {
    private final Semaphore updateLock;
    private final AtomicBoolean pendingUpdate;
    private final VBox packs;
    public PackList(){
        packs = new VBox();
        packs.getStyleClass().add(Constants.CSS_CLASS_VBOX);
        packs.setFocusTraversable(false);
        packs.setAlignment(Pos.TOP_CENTER);

        updateLock = new Semaphore(0);
        pendingUpdate = new AtomicBoolean(true);
        CompletableFuture
                .runAsync(()->update(true))
                .whenComplete((v,e)->updateLock.release());
        updateAsync();

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

    private void update(boolean localOnly){
        ModpackManager manager = ModpackManager.getInstance();
        if (!localOnly) manager.updateToLatestAsync().join();
        final List<PackSelector> packs = manager.getModpacks().stream()
                .map(PackSelector::new)
                .sorted(PackSelector::compareTo)
                .collect(Collectors.toList());
        Platform.runLater(()->{
            ObservableList<Node> sidebar = this.packs.getChildren();
            sidebar.clear();
            sidebar.addAll(packs);
        });
        if (pendingUpdate.getAndSet(false)) update(false);
    }

    public void updateAsync(){
        if (!updateLock.tryAcquire()) pendingUpdate.set(true);
        else CompletableFuture.runAsync(()->update(false)).whenComplete((v, e)->updateLock.release());
    }
}
