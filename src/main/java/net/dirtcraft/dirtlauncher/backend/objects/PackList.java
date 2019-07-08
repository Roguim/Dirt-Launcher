package net.dirtcraft.dirtlauncher.backend.objects;

import com.google.api.client.util.Key;

import java.util.List;

public class PackList {

    @Key("packs")
    private List<Pack> packs;

    public PackList(List<Pack> packs) {
        this.packs = packs;
    }

    public List<Pack> getPacks() {
        return packs;
    }

    public void setPacks(List<Pack> packs) {
        this.packs = packs;
    }

}
