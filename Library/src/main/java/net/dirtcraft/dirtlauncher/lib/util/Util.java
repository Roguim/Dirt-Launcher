package net.dirtcraft.dirtlauncher.lib.util;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.TimerTask;

public class Util {
    public static TimerTask timerTask(Runnable runnable){
        return new TimerTask() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static void spin(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }

    public static String getFileSha1(File file) throws IOException {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            try (InputStream input = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int len = input.read(buffer);
                while (len != -1) {
                    sha1.update(buffer, 0, len);
                    len = input.read(buffer);
                }

                return toHex(sha1.digest(), false);
            }
        } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
        return null;
    }

    private static final char[] hexLower = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final char[] hexUpper = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    public static String toHex(byte[] data, boolean uppercase) {
        StringBuilder result = new StringBuilder(data.length * 2);
        char[] hex = uppercase? hexUpper : hexLower;
        for (byte b : data) result.append(hex[b & 0xF]).append(hex[(b << 4) & 0xF]);
        return result.toString();
    }

    public static String getStringFromUrl(String url) {
        HttpResponse httpResponse = null;
        try {
            HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
            HttpRequest httpRequest = requestFactory.buildGetRequest(new GenericUrl(url));
            httpResponse = httpRequest.execute();
            return httpResponse.parseAsString();
        } catch (Exception exception) {
            try {Thread.sleep(2000);} catch (InterruptedException ignored) {}
            System.out.println(exception.getMessage() + "\nRetrying...");
            return getStringFromUrl(url);
        } finally {
            if (httpResponse != null) try{httpResponse.disconnect();}catch (IOException ignored){};
        }
    }
}
