package net.dirtcraft.dirtlauncher.lib.util;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
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
                runnable.run();
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

                return new HexBinaryAdapter().marshal(sha1.digest());
            }
        } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
        return null;
    }
}
