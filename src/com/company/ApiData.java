package com.company;

import org.json.*;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
// класс для обработки данных от API (получение данных, запись в словарь, запись в файл)
public class ApiData {
    private final int id;
    public ArrayList<Integer> idsFriends = new ArrayList<>();
    public ApiData(int id) {
        this.id = id;
    }

    public String getData() throws IOException {
        URL url = new URL(String.format("https://api.vk.com/method/friends.get?user_id=%d&fields=city&access_token=f5493d1ef5493d1ef5493d1e03f53ddc23ff549f5493d1eaae58a467485251565e27d2a&v=5.126", this.id));
        URLConnection yc = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = in.read()) != -1) {
            sb.append((char) cp);
        }
        in.close();
        return sb.toString();
    }

    public JSONArray getJsonArrayCity(String data) throws JSONException {
        JSONObject json = new JSONObject(data);
        return json.getJSONObject("response").getJSONArray("items");
    }

    public Map<String, CityInfo> getDictionary(JSONArray friends) {
        Map<String, CityInfo> result = new TreeMap<>();

        for (int i = 0; i < friends.length(); i++) {
            try {
                this.idsFriends.add(friends.getJSONObject(i).getInt("id"));
                String city = friends.getJSONObject(i).getJSONObject("city").get("title").toString();
                if (result.containsKey(city)) {
                    result.get(city).incCount();
                }//обновление счетчика друзей в структуре информации о городе

                else //добавление нового объекта в словарь
                    result.put(city, new CityInfo(city, 1));
            } catch (Exception ignored) {
            }
        }

        return result;
    }

    public JSONObject jsonFromDict(Map<String, CityInfo> cityDict) throws JSONException {
        JSONArray array = new JSONArray();
        String[] keyArray = cityDict.keySet().toArray(new String[0]);
        for (int i = 0; i < cityDict.size(); i++) {
            array.put(i, new JSONObject().put("City", cityDict.get(keyArray[i]).name())
                    .put("Amount", cityDict.get(keyArray[i]).count()));
        }

        return new JSONObject().put("Data", array);
    }

    public void createJSONFile(Map<String, CityInfo> filledDict) throws IOException, JSONException {
        String data = jsonFromDict(filledDict).toString();

        try {
            Files.createFile(Paths.get(String.format("src/%d.json", this.id)));
        } catch (Exception e) {
        }
        FileWriter fw = new FileWriter(String.format("src/%d.json", this.id));
        fw.write(data);
        fw.close();
    }
}
