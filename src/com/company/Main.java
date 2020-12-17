package com.company;

import java.sql.*;
import java.util.*;


public class Main {
    public static void main(String[] args) throws InterruptedException {
        List<Integer> listId = Arrays.asList(85225806, 196901000, 137795470, 7641514, 20430470);
        String nameTable = "CityById";

        //long startTime = System.currentTimeMillis(); // таймер для просмотра продолжительности выполнения всей работы
        List<GetDataById> threadsList = new ArrayList<>();
        for (int id : listId) { //создание списка потоков
            threadsList.add(new GetDataById(id));
        }
        for (Thread t : threadsList){ // запуск потоков с созданием файлов с данными от API
            t.start();

        }
        for (Thread t : threadsList){
            t.join();
        }
        // запись данных из файйлов в БД
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            DB db = new DB();
            List<WriteToDBThread> threadWriteList = new ArrayList<>();
            for (int id : listId) { //создание списка потоков
                threadWriteList.add(new WriteToDBThread(id, db, nameTable));
            }
            for (Thread t : threadWriteList){ //запуск потоков, записывающих данные в БД
                t.start();

            }
            for (Thread t : threadWriteList){
                t.join();
            }
            //long endTime = System.currentTimeMillis();
            //System.out.println("Total execution time: " + (endTime-startTime) + "ms");


            // ===== Stream =====
            // запрос к БД
            String sqlSelect = String.format("SELECT * FROM %s WHERE IdUser='%d'", nameTable, 85225806);
            Statement st = db.getStatement();
            ResultSet result = st.executeQuery(sqlSelect);
            // запись результата в словарь
            Map<String, Integer> resultMap = new HashMap<>();
            while (result.next()) {
                resultMap.put(result.getString("CityName"), result.getInt("CountFriends"));
            }

            // выбор информации из словаря при помощи stream() - выбор городов с численностью друзей более 5
            Object[] names = resultMap.keySet()
                    .stream()
                    .filter(x -> resultMap.get(x) > 5)
                    .sorted()
                    .toArray();
            // вывод
            for (Object n : names) {
                System.out.println(n.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
