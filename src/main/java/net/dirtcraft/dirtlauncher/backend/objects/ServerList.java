package net.dirtcraft.dirtlauncher.backend.objects;

import net.dirtcraft.dirtlauncher.backend.config.Directories;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ServerList {
    private final File serverDat;
    private final List<Server> servers;

    public static ServerList builder(String pack){
        return new ServerList(pack);
    }

    private ServerList(String packName){
        servers = new ArrayList<>();
        this.serverDat = Paths.get(Directories.getInstancesDirectory().toString(),packName.replaceAll("\\s+", "-"), "servers.dat").toFile();
    }
    //§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§\\
    private List<Byte> getAsByteArray(){
        final List<Byte> compound = new ArrayList<>();
        //Server lists are wrapped in an empty compound tag.
        //As usual, the second two bytes are the size of the
        //key string. in this case, it's nameless so it's 0.
        compound.add(Types.COMPOUND.getByte());
        compound.add((byte) 0);
        compound.add((byte) 0);

        //The server list is a named list called servers.
        //since we know that, we enter a key length of 7.
        compound.add(Types.LIST.getByte());
        compound.add((byte) 0);
        compound.add((byte) 7);
        for (byte bit : "servers".getBytes(StandardCharsets.US_ASCII)) compound.add(bit);

        //The values of the list is a nameless compound, which is
        //our first three bytes. the fifth and sixth are just the
        //length of the server list. pretty simple stuff i guess.
        compound.add(Types.COMPOUND.getByte());
        compound.add((byte) 0);
        compound.add((byte) 0);
        compound.add((byte) ((servers.size() >> 8 ) & 0xff));
        compound.add((byte) (servers.size() & 0xff));

        //we add all the server info down here, then put an
        //end to tell the game the initial compound is over
        servers.forEach(server -> compound.addAll(server.get()));
        compound.add(Types.END.getByte());
        return compound;
    }

    public ServerList addServer(String ip, String name, String icon){
        servers.add(new Server(ip, name, icon==null?DIRTCRAFT_ICON:icon));
        return this;
    }

    public ServerList addServer(String ip, String name){
        servers.add(new Server(ip, name, DIRTCRAFT_ICON));
        return this;
    }

    public void build() {
        try (FileOutputStream fos = new FileOutputStream(serverDat);
             DataOutputStream dos = new DataOutputStream(fos)){
            System.out.println(serverDat.createNewFile());
            List<Byte> byteList = getAsByteArray();
            Byte[] bigBytes = byteList.toArray(new Byte[0]);
            byte[] smlBytes = ArrayUtils.toPrimitive(bigBytes);
            dos.write(smlBytes);
        } catch (IOException e){
            LogManager.getLogger().error(e);
        }

    }

    private final class Server{
        private final String name;
        private final String ip;
        private final String icon;

        private Server(String ip, String name, String icon){
            this.name = name;
            this.ip = ip;
            this.icon = icon;
        }

        private List<Byte> addProperty(Types type, String name, String value){
            final List<Byte> bytes = new ArrayList<>();
            //First byte of an entry is always the value type
            bytes.add(type.getByte());

            //The second two are the size of the key
            final short nameLength = (short) name.length();
            bytes.add((byte) ((nameLength >> 8 ) & 0xff));
            bytes.add((byte) (nameLength & 0xff));
            for (byte bit :name.getBytes(StandardCharsets.US_ASCII)) bytes.add(bit);

            //After the property, the next 2 are the size of the value
            //Since we want to add colours, we blank the size and work
            //it out later and fill it in, since when we convert the §
            //character to ascii, it has to become two separate bytes.
            short valueLength = (short) value.length();
            final int valueLengthIndex = bytes.size();
            bytes.add((byte) (0));
            bytes.add((byte) (0));
            final byte[] asciiChar = value.getBytes(StandardCharsets.US_ASCII);
            final char[] utf16Char = value.toCharArray();
            for (short i = 0; i < value.length(); i++){
                if (utf16Char[i]=='§'){
                    bytes.add((byte)194);
                    bytes.add((byte)167);
                    valueLength++;
                } else bytes.add(asciiChar[i]);
            }
            bytes.set(valueLengthIndex, (byte) ((valueLength >> 8 ) & 0xff));
            bytes.set(valueLengthIndex + 1, (byte) (valueLength & 0xff));
            return bytes;
        }

        private List<Byte> get(){
            final List<Byte> compound = new ArrayList<>(); //We don't need identifiers as the list does it for us.
            compound.addAll(addProperty(Types.STRING, "ip", ip));
            compound.addAll(addProperty(Types.STRING, "name", name));
            compound.addAll(addProperty(Types.STRING, "icon", icon));
            compound.add(Types.END.getByte()); //End indicates the end of a compound.
            return compound;
        }
    }
    private enum Types{
        END,
        BYTE,
        SHORT,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        BYTE_ARRAY,
        STRING,
        LIST,
        COMPOUND,
        INT_ARRAY,
        LONG_ARRAY;

        private Byte getByte(){
            return (byte) this.ordinal();
        }
    }

    private static final String DIRTCRAFT_ICON = "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";

}
