package com.company;

import java.util.Map;
// класс потока, принимающего данные от VK API
public class GetDataById extends Thread {
    int id;

    public GetDataById(int id) {
        this.id = id;
    }

    public void run() {
        ApiData API = new ApiData(id);
        try {
            Map<String, CityInfo> filledDict = API.getDictionary(API.getJsonArrayCity(API.getData()));
            API.createJSONFile(filledDict);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
