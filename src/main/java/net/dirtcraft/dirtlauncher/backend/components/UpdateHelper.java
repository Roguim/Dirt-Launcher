package net.dirtcraft.dirtlauncher.backend.components;

import net.dirtcraft.dirtlauncher.backend.utils.Constants;

import java.io.*;
import java.net.URISyntaxException;

public class UpdateHelper {
    public UpdateHelper() {
        final String className = "UpdateBootstrapper";
        final String JRE = "javaw";
        File currentDir;
        File currentJar;
        try {
            currentJar = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            currentDir = currentJar.getParentFile();
        } catch (URISyntaxException e) {
            currentDir = null;
            currentJar = null;
            e.printStackTrace();
            System.exit(-1);
        }

        final File bootStrapper = new File(currentDir, className + ".class");

        try(InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(className + ".class");
            BufferedInputStream bis = new BufferedInputStream(is);
            FileOutputStream fos = new FileOutputStream(bootStrapper);
        ){
            byte[] dataBuffer = new byte[1024];
            int bytesRead = 0;
            while((bytesRead = bis.read(dataBuffer, 0 , 1024)) != -1){
                fos.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        String[] args = new String[5];
        args[0] = JRE;
        args[1] = className;
        args[2] = Constants.UPDATE_URL;
        args[3] = currentJar.getPath();
        args[4] = JRE;

        try {
            Runtime.getRuntime().exec(String.join(" ", args));
        } catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);

    }
}
