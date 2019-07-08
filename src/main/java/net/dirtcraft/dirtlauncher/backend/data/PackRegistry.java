package net.dirtcraft.dirtlauncher.backend.data;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import net.dirtcraft.dirtlauncher.backend.objects.PackList;

public class PackRegistry {

    private PackList packList;

    public PackRegistry() {
        try {
            System.out.println("Making Request");
            HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
            System.out.println("Factory Made");
            HttpRequest request = requestFactory.buildGetRequest(new GenericUrl("http://localhost/launcher/packs.json"));
            System.out.println("Data fetched");
            request.execute().parseAs(PackList.class);
            System.out.println("Data parsed");
            System.out.println(packList.getPacks().size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PackList getPackList() {
        return packList;
    }

    public void setPackList(PackList packList) {
        this.packList = packList;
    }

}