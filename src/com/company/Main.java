package com.company;

import org.json.*;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.sql.*;
import java.util.*;


public class Main {
    public static void main(String[] args) throws IOException, JSONException {
        List<Integer> listId = Arrays.asList(85225806, 196901000, 137795470);
        String nameTable = "CityById";
        for (int id : listId) {
            URL url = new URL(String.format("https://api.vk.com/method/friends.get?user_id=%d&fields=city&access_token=f5493d1ef5493d1ef5493d1e03f53ddc23ff549f5493d1eaae58a467485251565e27d2a&v=5.126", id));
            URLConnection yc = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            String inputLine = readAll(in);
            in.close();
            //чтение списка друзей и их количества
            JSONObject json = new JSONObject(inputLine);
            JSONArray friends = json.getJSONObject("response").getJSONArray("items");
            //заполнение словаря данными
            Map<String, CityInfo> filledDict = fillDictionary(friends);
            createJSONFile(jsonFromDict(filledDict).toString(), id);
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            Connection conn = getConnection(); // подключение к БД
            Statement statement = conn.createStatement();
            for (int id : listId) {
                //==== Чтение только что созданного файла и запись в БД ====
                JSONObject dataToDB = readJsonFile(String.format("src/%d.json", id));
                JSONArray arr = (JSONArray) dataToDB.get("Data");  //извлечение массива с городами
                createTable(statement, nameTable);  // создание новой таблицы
                // проверка на наличие данного Id в таблице
                if (checkAttribute(nameTable, "IdUser", id, statement)) continue;

                String sqlInsert = String.format("INSERT %s(IdUser, CityName, CountFriends) VALUES (?, ?, ?)", nameTable);
                PreparedStatement preparedStatement = conn.prepareStatement(sqlInsert); // шаблон запроса SQL
                // обход массива с данными из файла
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject item = (JSONObject) arr.get(i);
                    //заполнение шаблона
                    preparedStatement.setInt(1, id);
                    preparedStatement.setString(2, item.getString("City"));
                    preparedStatement.setInt(3, item.getInt("Amount"));
                    // выполнение запроса
                    preparedStatement.executeUpdate();
                }
            }
            String sqlSelect = String.format("SELECT * FROM %s", nameTable);
            Statement st = conn.createStatement();
            ResultSet result = st.executeQuery(sqlSelect);
            while (result.next()) {
                System.out.printf("Город: %s, Количество друзей в данном городе: %s\n",
                        result.getString("CityName"), result.getString("CountFriends"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean checkAttribute(String nameTable, String nameColumn, int id, Statement statement) throws SQLException {
        ResultSet existId = statement.executeQuery(String.format("SELECT %s FROM %s WHERE %s='%d'", nameColumn, nameTable, nameColumn, id));
        if (existId.next())
            return existId.getInt("IdUser") == id;
        return false;
    }

    private static JSONObject readJsonFile(String path) throws IOException, JSONException {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            return new JSONObject(reader.readLine());
        }
    }


    private static void createTable(Statement statement, String nameTable) throws SQLException {
        String sqlCommand = String.format("CREATE TABLE IF NOT EXISTS %s(Id INT PRIMARY KEY AUTO_INCREMENT, IdUser INT, " +
                "CityName VARCHAR(255), CountFriends INT)", nameTable);
        statement.executeUpdate(sqlCommand);
    }


    public static Connection getConnection() throws SQLException {
        String[] connectData = new String[3];
        try { //чтение паролей, логинов для подключения к БД из отдельного файла, которого по понятным причинам в репозитории нет
            File file = new File("src/database_properties.txt");
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);
            for (int i = 0; i < 3; i++)
                connectData[i] = reader.readLine();
        } catch (Exception ignored) {
        }
        return DriverManager.getConnection(connectData[0], connectData[1], connectData[2]);
    }

    private static Map<String, CityInfo> fillDictionary(JSONArray friends) {
        Map<String, CityInfo> result = new TreeMap<>();
        for (int i = 0; i < friends.length(); i++) {
            try {
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
    // получение JSONObject из словаря
    public static JSONObject jsonFromDict(Map<String, CityInfo> cityDict) throws JSONException {
        JSONArray array = new JSONArray();
        String[] keyArray = cityDict.keySet().toArray(new String[0]);
        for (int i = 0; i < cityDict.size(); i++) {
            array.put(i, new JSONObject().put("City", cityDict.get(keyArray[i]).name())
                    .put("Amount", cityDict.get(keyArray[i]).count()));
        }

        return new JSONObject().put("Data", array);
    }

    //создание файла .json
    private static void createJSONFile(String data, int id) throws IOException {
        try {
            Files.createFile(Paths.get(String.format("src/%d.json", id)));
        } catch (Exception e) {
        }
        FileWriter fw = new FileWriter(String.format("src/%d.json", id));
        fw.write(data);
        fw.close();
    }


    //чтение ответа api
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
