package net.dirtcraft.dirtlauncher.game.authentification.account;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.data.MicroSoft.*;
import net.dirtcraft.dirtlauncher.utils.WebUtils;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.*;

public class MicroAccount extends Account {

    public static final Gson gson = Main.gson;
    private String refreshToken;
    private String name;
    private String uuid;
    private String accessToken;

    public MicroAccount(String token) {
        super(AccountType.MICROSOFT);
        AuthResponse authResponse = getAccessToken(token);
        XBLResponse xblResponse = getXBLToken(authResponse);
        XBLResponse xstsResponse = getXstsToken(xblResponse);
        MSMCLoginResponse msmcLoginResponse = loginToMinecraft(xstsResponse.getIdentityToken());

        this.accessToken = msmcLoginResponse.access_token;
        this.refreshToken = authResponse.refresh_token;
        updateUsername();
    }

    private void refreshTokens() {
        AuthResponse authResponse = refreshAccessToken(refreshToken);
        XBLResponse xblResponse = getXBLToken(authResponse);
        XBLResponse xstsResponse = getXstsToken(xblResponse);
        MSMCLoginResponse msmcLoginResponse = loginToMinecraft(xstsResponse.getIdentityToken());

        this.accessToken = msmcLoginResponse.access_token;
        this.refreshToken = authResponse.refresh_token;
    }

    private void updateUsername() {
        MSProfile profile = getMcProfile(accessToken);
        this.name = profile.name;
        this.uuid = profile.id;
    }

    @Override
    public String getAlias() {
        return name;
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public String getId() {
        return uuid;
    }

    @Override
    public boolean isValid() {
        try {
            String uuid = this.uuid;
            updateUsername();
            if (uuid.equals(this.uuid)) return true;
            else {
                System.out.println("Session not valid, Attempting to refresh it!");
                refreshTokens();
                updateUsername();
                return uuid.equals(this.uuid);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Session not valid.");
            return false;
        }
    }

    private AuthResponse getAccessToken(String authToken){
        RequestBody data = new FormBody.Builder()
                .add("client_id", "00000000402b5328")
                .add("code", authToken)
                .add("grant_type", "authorization_code")
                .add("redirect_uri", "https://login.live.com/oauth20_desktop.srf")
                .add("scope", "service::user.auth.xboxlive.com::MBI_SSL")
                .build();

        Request request = new Request.Builder()
                .url("https://login.live.com/oauth20_token.srf")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(data).build();

        return WebUtils.getGsonFromRequest(request, TypeToken.of(AuthResponse.class)).get();
    }

    private AuthResponse refreshAccessToken(String refreshToken) {
        RequestBody data = new FormBody.Builder()
                .add("client_id", "00000000402b5328")
                .add("refresh_token", refreshToken)
                .add("grant_type", "refresh_token")
                .add("redirect_uri", "http://127.0.0.1:28562")
                .build();

        Request request = new Request.Builder()
                .url("https://login.live.com/oauth20_token.srf")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(data)
                .build();

        return WebUtils.getGsonFromRequest(request, TypeToken.of(AuthResponse.class)).get();
    }

    private XBLResponse getXBLToken(AuthResponse accessToken){
        Map<Object, Object> properties = new HashMap<Object, Object>();
        properties.put("AuthMethod", "RPS");
        properties.put("SiteName", "user.auth.xboxlive.com");
        properties.put("RpsTicket", accessToken.access_token);

        Map<Object, Object> data = new HashMap<Object, Object>();
        data.put("Properties", properties);
        data.put("RelyingParty", "http://auth.xboxlive.com");
        data.put("TokenType", "JWT");

        Request request = new Request.Builder()
                .url("https://user.auth.xboxlive.com/user/authenticate")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("x-xbl-contract-version", "1")
                .post(RequestBody.create(gson.toJson(data), MediaType.get("application/json; charset=utf-8"))).build();

        return WebUtils.getGsonFromRequest(request, TypeToken.of(XBLResponse.class)).get();
    }

    private XBLResponse getXstsToken(XBLResponse xblToken) {
        Map<Object, Object> properties = new HashMap<Object, Object>();
        properties.put("SandboxId", "RETAIL");

        List<String> userTokens = new ArrayList<String>();
        userTokens.add(xblToken.Token);
        properties.put("UserTokens", userTokens);

        Map<Object, Object> data = new HashMap<Object, Object>();
        data.put("Properties", properties);
        data.put("RelyingParty", "rp://api.minecraftservices.com/");
        data.put("TokenType", "JWT");

        Request request = new Request.Builder().url("https://xsts.auth.xboxlive.com/xsts/authorize")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("x-xbl-contract-version", "1")
                .post(RequestBody.create(gson.toJson(data), MediaType.get("application/json; charset=utf-8")))
                .build();

        return WebUtils.getGsonFromRequest(request, TypeToken.of(XBLResponse.class)).get();
    }

    private MSMCLoginResponse loginToMinecraft(String xstsToken) {
        Map<Object, Object> data = new HashMap<Object, Object>();
        data.put("xtoken", xstsToken);
        data.put("platform", "PC_LAUNCHER");

        Request request = new Request.Builder().url("https://api.minecraftservices.com/launcher/login")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(RequestBody.create(gson.toJson(data), MediaType.get("application/json; charset=utf-8")))
                .build();

        return WebUtils.getGsonFromRequest(request, TypeToken.of(MSMCLoginResponse.class)).get();
    }

    private MSProfile getMcProfile(String login) {
        Request request = new Request.Builder().url("https://api.minecraftservices.com/minecraft/profile")
                .header("Authorization", "Bearer " + login)
                .build();

        return WebUtils.getGsonFromRequest(request, TypeToken.of(MSProfile.class)).get();
    }
}
