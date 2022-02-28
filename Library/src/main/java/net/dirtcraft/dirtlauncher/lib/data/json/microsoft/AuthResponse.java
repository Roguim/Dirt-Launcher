package net.dirtcraft.dirtlauncher.lib.data.json.microsoft;

public class AuthResponse {
    private AuthResponse(int i){throw new RuntimeException("do not init");}
    public final String token_type;
    public final int expires_in;
    public final String scope;
    public final String access_token;
    public final String refresh_token;
    public final String user_id;
    public final String foci;
}
