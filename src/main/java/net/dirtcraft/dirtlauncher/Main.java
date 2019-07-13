package net.dirtcraft.dirtlauncher;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.dirtcraft.dirtlauncher.Controllers.Settings;
import net.dirtcraft.dirtlauncher.Controllers.Update;
import net.dirtcraft.dirtlauncher.backend.config.Internal;
import net.dirtcraft.dirtlauncher.backend.config.Directories;
import net.dirtcraft.dirtlauncher.backend.utils.FileUtils;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;
import net.dirtcraft.dirtlauncher.backend.utils.RamUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main extends Application {

    private static Logger logger;
    private static Main instance;
    private Stage stage;


    public static void main(String[] args) {

        System.out.println("\n\n\n                                  whatthefu                                         \n" +
                "                              ckdidyoujustfucki                                     \n" +
                "                          ngsayaboutme,youlittlebi                                  \n" +
                "                  tch?I’llhaveyou           knowIgra                                \n" +
                "               duatedtopofmy                  classin                               \n" +
                "             theNavySeals,an                   dI’veb                               \n" +
                "             eeninvolvedinnum                   erous                               \n" +
                "             secretraids onAl-Q    uaeda,andIh  aveov                               \n" +
                "             er300confirmedkills .Iamtrainedingo rill                               \n" +
                "             awarfareandI’mthe  topsniperintheentireU                               \n" +
                "            Sarme  dforces.You  arenothingtomebutjust                               \n" +
                "           anothertarget.Iwillw ipeyouthefu ckoutwith                               \n" +
                "          precisionthelikesof   whichhasneverbeenseen                               \n" +
                "         beforeonthisEarth,markmyfuckingwords  .Yout                                \n" +
                "        hinky          oucangetawaywithsa     yingth                                \n" +
                "       atshi                      ttomeov     ertheI                                \n" +
                "      nterne                                 t?Thin                                 \n" +
                "     kagain                                 ,fucke                                  \n" +
                "    r.Aswe                                  speakI                                  \n" +
                "    amcon                      tact        ingmys                                   \n" +
                "    ecre                      tnetw ork   ofspie                                    \n" +
                "    sacr                      osstheUSAa  ndyou                         rIPisbein   \n" +
                "   gtrac                      edrightno  wsoyo                        ubetterprepa  \n" +
                "   refor                     thestorm,m aggot                       .Thest    ormt  \n" +
                "   hatwi                     pesoutthe  pathe                     ticlitt    lethi  \n" +
                "   ngyou                    callyourl  ife.Yo                   u’refuc     kingd   \n" +
                "   ead,k                    id.Icanb   eanywhere,anytime,an   dIcanki     llyou     \n" +
                "    inov                   ersevenh    undredways,andthat’sjustwith      mybar      \n" +
                "    ehan                   ds.Noto     nlyam   Iexte   nsivelytra      inedin       \n" +
                "    unar                  medcomba      t,b   utIhaveaccesstoth      eentir         \n" +
                "    earse               nalof theUn         itedStatesMarineCo     rpsandI          \n" +
                "     will             useit  toitsfu         llextenttowipeyourm   iserable         \n" +
                "     assof            fthefaceofthec                     ontinent    ,youlittl      \n" +
                "      eshit            .Ifonlyyouco              uldh       avekno  wnwh atunh      \n" +
                "      olyret              ribu                   tion        yourli  ttle\"cle       \n" +
                "       ver\"co                                mme              ntwas    abou         \n" +
                "        ttobring                            down              upony     ou,m        \n" +
                "           aybeyou                          woul              dhaveheldyourf        \n" +
                " uck        ingtongue.                       Buty           oucouldn’t,youd         \n" +
                "idn’t,a    ndnowyou’repaying                  the         price,y    o              \n" +
                "ugoddamnidiot. Iwillshitfuryallovery           ouan    dyouwil                      \n" +
                "ldro wninit.You’ref    uckingdead,kidd o.Wwhatthefuckdidyouj                        \n" +
                " ustf  uckingsaya         boutme,youl ittlebitch?I’llhave                           \n" +
                "  youk   nowIgr         aduatedtopof mycla ssintheNavy                              \n" +
                "   Seals,andI           ’vebeeninvo  lved                                           \n" +
                "    innumer              oussecre   trai                                            \n" +
                "      dso                nAl-Qu    aeda                                             \n" +
                "                          ,andIh  aveo                                              \n" +
                "                           ver300conf                                               \n" +
                "                             irmedki                                                \n" +
                "                               lls                                                  ");

        System.out.println("\n\n\n All Hail Dirt \n\n\n");

        System.setProperty("log4j.saveDirectory", Directories.getLog().toString());
        logger = LogManager.getLogger(Main.class);
        logger.info("Logger logging, App starting.");
        // Ensure that the application folders are created
        Directories.getInstallDirectory().mkdirs();
        Directories.getInstancesDirectory().mkdirs();
        Directories.getVersionsDirectory().mkdirs();
        Directories.getAssetsDirectory().mkdirs();
        Directories.getForgeDirectory().mkdirs();
        // Ensure that all required manifests are created
        if (!Directories.getConfiguration().exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.addProperty("minimum-ram", RamUtils.getMinimumRam() * 1024);
            emptyManifest.addProperty("maximum-ram", RamUtils.getRecommendedRam() * 1024);
            emptyManifest.addProperty("java-arguments", Internal.DEFAULT_JAVA_ARGS);
            FileUtils.writeJsonToFile(Directories.getConfiguration(), emptyManifest);
        }
        if(!Directories.getDirectoryManifest(Directories.getInstancesDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("packs", new JsonArray());
            FileUtils.writeJsonToFile(Directories.getDirectoryManifest(Directories.getInstancesDirectory()), emptyManifest);
        }
        if(!Directories.getDirectoryManifest(Directories.getVersionsDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("versions", new JsonArray());
            FileUtils.writeJsonToFile(Directories.getDirectoryManifest(Directories.getVersionsDirectory()), emptyManifest);
        }
        if(!Directories.getDirectoryManifest(Directories.getAssetsDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("assets", new JsonArray());
            FileUtils.writeJsonToFile(Directories.getDirectoryManifest(Directories.getAssetsDirectory()), emptyManifest);
        }
        if(!Directories.getDirectoryManifest(Directories.getForgeDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("forgeVersions", new JsonArray());
            FileUtils.writeJsonToFile(Directories.getDirectoryManifest(Directories.getForgeDirectory()), emptyManifest);
        }
        // Launch the application
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        instance = this;

        Parent root = FXMLLoader.load(MiscUtils.getResourceURL(Internal.SCENES, "main.fxml"));

        primaryStage.setTitle("Dirt Launcher");
        primaryStage.getIcons().setAll(MiscUtils.getImage(Internal.ICONS, "main.png"));

        Scene scene = new Scene(root, MiscUtils.screenDimension.getWidth() / 1.15, MiscUtils.screenDimension.getHeight() / 1.35);

        primaryStage.initStyle(StageStyle.DECORATED);

        primaryStage.setScene(scene);
        stage = primaryStage;


        stage.show();

        Settings.loadSettings();
        if (Update.hasUpdate()) Update.showStage();

    }

    @Override
    public void stop(){
        LogManager.shutdown();
    }

    public Stage getStage() {
        return stage;
    }

    public static Main getInstance() {
        return instance;
    }
}
