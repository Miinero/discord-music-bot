//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.xao.discordbot.utilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Utils {

    public static String getVideoTitle(String url) {
        String videoTitle = "Houve um erro ao pegar o nome da música";

        try {
            Document doc = Jsoup.connect(url).get();
            Element title = doc.select("meta[property=og:title]").first();
            videoTitle = title.attr("content");
            System.out.println(videoTitle);
        } catch (IOException var4) {
            System.out.println("Houve um erro ao pegar o nome da música: " + url);
        }

        return videoTitle;
    }

    public static String getMusicTime(long duration) {
        duration /= 1000L;
        long minutes = duration / 60L;
        long res = duration % 60L;
        String toAdd;
        if (res < 10L) {
            toAdd = "0" + res;
        } else {
            toAdd = "" + res;
        }

        return "" + minutes + ":" + toAdd;
    }

    private static String getInitialDataScript(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            Elements scripts = doc.select("script");
            Iterator var3 = scripts.iterator();

            while(var3.hasNext()) {
                Element script = (Element)var3.next();
                String scriptHtml = script.html();
                if (scriptHtml.contains("var ytInitialData")) {
                    return scriptHtml.replace("var ytInitialData = ", "").replace(";", "");
                }
            }
        } catch (IOException var6) {
            var6.printStackTrace();
        }

        return null;
    }

    public static List<String> getSpotifyAudio(String url) {
        StringBuilder response = null;

        try {
            URL urlStr = new URL("https://api.song.link/v1-alpha.1/links?url=" + url);
            HttpURLConnection connection = (HttpURLConnection)urlStr.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            response = new StringBuilder();

            String inputLine;
            while((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();
            System.out.println(response);
        } catch (Exception var11) {
            System.out.println("Houve um erro ao fazer a conexão");
        }

        if (response == null) {
            return new ArrayList();
        } else {
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(response.toString());
            List<String> list = new ArrayList<>();
            Iterator<Map.Entry<String, JsonElement>> var15 = jsonElement.getAsJsonObject().getAsJsonObject("entitiesByUniqueId").entrySet().iterator();

            while(var15.hasNext()) {
                Map.Entry<String, JsonElement> entry = var15.next();
                System.out.println(entry.getKey());
                if (entry.getKey().startsWith("YOUTUBE_VIDEO")) {
                    JsonElement id = entry.getValue().getAsJsonObject().get("id");
                    System.out.println(id);
                    list.add("https://www.youtube.com/watch?v=" + id.getAsString());
                }

                if (entry.getKey().startsWith("SPOTIFY_SONG")) {
                    JsonObject jsonObject = entry.getValue().getAsJsonObject();
                    JsonElement title = jsonObject.get("title");
                    JsonElement artist = jsonObject.get("artistName");
                    String songName = "" + title.getAsString() + " - " + artist.getAsString();
                    System.out.println(songName);
                    list.add(songName);
                }
            }

            return list;
        }
    }

    private static JsonObject getYtInitialData(String json) {
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(json);
        return jsonElement.getAsJsonObject();
    }

    private static List<String> getVideoIds(JsonObject jsonObject) {
        JsonArray contentsArray = jsonObject.getAsJsonObject("contents").getAsJsonObject("twoColumnSearchResultsRenderer").getAsJsonObject("primaryContents").getAsJsonObject("sectionListRenderer").getAsJsonArray("contents");
        List<String> videoIds = new ArrayList<>();
        Iterator<JsonElement> var3 = contentsArray.iterator();

        while(true) {
            JsonObject itemSectionRenderer;
            do {
                if (!var3.hasNext()) {
                    return videoIds;
                }

                JsonElement content = var3.next();
                itemSectionRenderer = content.getAsJsonObject().getAsJsonObject("itemSectionRenderer");
            } while(itemSectionRenderer == null);

            JsonArray contents = itemSectionRenderer.getAsJsonArray("contents");

            for (JsonElement item : contents) {
                JsonObject videoRenderer = item.getAsJsonObject().getAsJsonObject("videoRenderer");
                if (videoRenderer != null) {
                    String videoId = videoRenderer.get("videoId").getAsString();
                    videoIds.add(videoId);
                }
            }
        }
    }

    public static String getFirstVideoUrl(String query) {
        String var10000 = query.replace(" ", "+");
        String url = "https://www.youtube.com/results?search_query=" + var10000.replace("|", "+");
        String initialDataScript = getInitialDataScript(url);
        JsonObject ytInitialData = getYtInitialData(initialDataScript);
        List<String> videoIds = getVideoIds(ytInitialData);
        if (!videoIds.isEmpty()) {
            String first = videoIds.get(0);
            System.out.println(first);
            return "https://www.youtube.com/watch?v=" + first;
        } else {
            return null;
        }
    }

    public static boolean isYouTubeURL(String url) {
        return url.startsWith("https://www.youtube.com");
    }

    public static boolean isSpotifyURL(String url) {
        return url.startsWith("https://open.spotify.com");
    }

    public static String getRandomImageUrl() {
        List<String> cache = new ArrayList<>();
        cache.add("https://i.imgur.com/zaop2z4.jpeg");
        cache.add("https://i.imgur.com/NWf8gzZ.jpg");
        cache.add("https://i.imgur.com/wgCGizq.jpg");
        cache.add("https://i.imgur.com/6OdKprx.jpg");
        cache.add("https://i.imgur.com/YcVKk4K.jpg");
        cache.add("https://i.imgur.com/gqV9gez.jpg");
        Random rand = new Random();
        return cache.get(rand.nextInt(cache.size() - 1));
    }

    public static String getRandomColor() {
        List<String> cache = new ArrayList<>();
        cache.add("#FFA500");
        cache.add("#FFFF00");
        cache.add("#00FF00");
        cache.add("#FFFFFF");
        cache.add("#5DD2EF");
        cache.add("#FF69B4");
        Random rand = new Random();
        return cache.get(rand.nextInt(cache.size() - 1));
    }

    public static void main(String[] args) {
        System.out.println(getFirstVideoUrl("coldplay hymn for the weekend sao paulo 10/03/2022"));
    }
}
