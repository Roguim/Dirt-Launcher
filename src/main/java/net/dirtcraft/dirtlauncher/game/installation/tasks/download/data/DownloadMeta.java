package net.dirtcraft.dirtlauncher.game.installation.tasks.download.data;

import net.dirtcraft.dirtlauncher.logging.Logger;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;
import org.apache.commons.codec.digest.DigestUtils;
import sun.plugin2.message.Message;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Optional;

public class DownloadMeta implements IFileDownload {
    private final URL src;
    private final File dest;
    private long size;
    private final String sha1;

    public DownloadMeta(String src, File dest){
        this.src = MiscUtils.getURL(src).orElse(null);
        this.dest = dest;
        this.size = -1;
        this.sha1 = null;
    }

    public DownloadMeta(URL src, File dest){
        this.src = src;
        this.dest = dest;
        this.size = -1;
        this.sha1 = null;
    }

    public DownloadMeta(String src, File dest, long size){
        this.src = MiscUtils.getURL(src).orElse(null);
        this.dest = dest;
        this.size = size;
        this.sha1 = null;
    }

    public DownloadMeta(URL src, File dest, long size){
        this.src = src;
        this.dest = dest;
        this.size = size;
        this.sha1 = null;
    }

    public DownloadMeta(URL src, File dest, long size, String sha1){
        this.src = src;
        this.dest = dest;
        this.size = size;
        this.sha1 = sha1;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public URL getUrl() {
        return src;
    }

    @Override
    public Path getFolder() {
        return dest.getParentFile().toPath();
    }

    @Override
    public Optional<String> getSha1() {
        return Optional.ofNullable(sha1);
    }

    @Override
    public boolean verify() {
        return dest.exists() && dest.length() == size && sha1 != null && sha1.equals(getSha1(dest));
    }

    @Override
    public String getFileName() {
        return dest.getName();
    }

    public static String getSha1(File file) {
        try (InputStream is = Files.newInputStream(file.toPath());
             BufferedInputStream bis = new BufferedInputStream(is)
        ){
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] buf = new byte[8192];
            for (int i = 0; i <= 0; i = bis.read(buf)) digest.update(buf, 0, i);
            return new String(digest.digest());
        } catch (Exception e){
            Logger.INSTANCE.error(e);
            return "";
        }
    }
}
