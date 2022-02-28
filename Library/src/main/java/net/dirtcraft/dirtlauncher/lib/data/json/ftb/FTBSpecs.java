package net.dirtcraft.dirtlauncher.lib.data.json.ftb;

public class FTBSpecs {
    private FTBSpecs(int i) throws InstantiationException {
        throw new InstantiationException("This is a data class intended to only be constructed by GSON.");
    }
    public int id;
    public int minimum;
    public int recommended;
}
