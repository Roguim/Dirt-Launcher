import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
/*//////////////////////////////////////////
//                                        //
//      DO NOT USE NON-STANDARD LIBS      //
//      THIS SHIT CAN ONLY USE BASIC      //
//      JAVA RUNTIME STUFF SINCE THE      //
//      CLASS WILL BE RAN BY ITSELF!      //
//                                        //
//////////////////////////////////////////*/
@SuppressWarnings("WrongPackageStatement")
public class UpdateBootstrapper {
    public static void main(String[] args){
        File temp;
        File dest;
        HttpURLConnection con;
        String jre = args[2];
        try {
            final URL latest = new URL(args[0]);
            temp = new File(args[1] + ".dl");
            System.out.println(temp.delete()?"deleted existing temp file":"no existing temp file. this is good.");
            dest = new File(args[1]);
            con = (HttpURLConnection) latest.openConnection();
            con.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        } catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
            temp = null;
            dest = null;
            con = null;
        }


        try(InputStream is = con.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            FileOutputStream fos = new FileOutputStream(temp);
                ){
            byte[] dataBuffer = new byte[1024];
            int bytesRead = 0;
            while((bytesRead = bis.read(dataBuffer, 0 , 1024)) != -1){
                fos.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        dest.delete();
        temp.renameTo(dest);

        List<String> cmds = new ArrayList<>();
        cmds.add(jre);
        cmds.add("-jar");
        cmds.add(dest.toString());
        try {
            Runtime.getRuntime().exec(String.join(" ", cmds));
        } catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);
    }
}
