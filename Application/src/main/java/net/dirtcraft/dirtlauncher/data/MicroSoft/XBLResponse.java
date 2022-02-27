package net.dirtcraft.dirtlauncher.data.MicroSoft;

import java.util.List;

public class XBLResponse {
    public String IssueInstant;
    public String NotAfter;
    public String Token;
    public XBLDisplayClaims DisplayClaims;

    public String getIdentityToken() {
        return "XBL3.0 x=" + DisplayClaims.xui.get(0).uhs + ";" + Token;
    }

    public static class XBLDisplayClaims {
        public List<XBLClaims> xui;
    }

    public static class XBLClaims {
        public String uhs;
    }

}
