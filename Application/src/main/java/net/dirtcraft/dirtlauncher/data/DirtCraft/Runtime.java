package net.dirtcraft.dirtlauncher.data.DirtCraft;

public class Runtime {
    public String jre;
    public String jfx;
    public boolean verbose;
    public boolean debug;

    public Runtime(String jre, String jfx) {
        this.jre = jre;
        this.jfx = jfx;
        this.verbose = false;
        this.debug = false;
    }
}
