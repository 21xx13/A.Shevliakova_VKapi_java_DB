package com.company;

import org.json.*;

import java.io.*;
import java.sql.*;
// класс потока, записывающего данные в БД
public class WriteToDBThread extends Thread {
    DB db;
    int id;
    Connection conn;
    String nameTable;

    public WriteToDBThread(int id, DB dataBase, String nameTable) throws SQLException {
        this.id = id;
        this.db = dataBase;
        this.nameTable = nameTable;
        this.conn = this.db.getConnection();
    }

    public void run() {
        try {
            JSONObject dataToDB = readJsonFile(String.format("src/%d.json", id));
            JSONArray arr = (JSONArray) dataToDB.get("Data");  //извлечение массива с городами
            db.createCityTable(nameTable);
            // проверка на наличие переданного Id пользователя в таблице
            if (!db.checkAttributeId(nameTable, "IdUser", id)) {
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

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private JSONObject readJsonFile(String path) throws IOException, JSONException {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            return new JSONObject(reader.readLine());
        }
    }
}
