package net.dirtcraft.dirtlauncher.lib.data.json.dirtcraft;

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
