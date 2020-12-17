package com.company;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.*;
// класс для работы с базой данных
public class DB {
    Connection conn;

    public DB() throws SQLException {
        getConnection();
    }
    // подключение к БД
    public Connection getConnection() throws SQLException {
        String[] connectData = new String[3];
        try { //чтение паролей, логинов для подключения к БД из отдельного файла, которого по понятным причинам в репозитории нет
            File file = new File("src/database_properties.txt");
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);
            for (int i = 0; i < 3; i++)
                connectData[i] = reader.readLine();
        } catch (Exception ignored) {
        }
        this.conn = DriverManager.getConnection(connectData[0], connectData[1], connectData[2]);
        return this.conn;
    }

    public Statement getStatement() throws SQLException {
        return this.conn.createStatement();
    }
    // создание таблицы со статистикой
    public void createCityTable(String nameTable) throws SQLException {
        String sqlCommand = String.format("CREATE TABLE IF NOT EXISTS %s(Id INT PRIMARY KEY AUTO_INCREMENT, IdUser INT, " +
                "CityName VARCHAR(255), CountFriends INT)", nameTable);
        this.getStatement().executeUpdate(sqlCommand);
    }
    // проверка на содержание ID в БД
    public boolean checkAttributeId(String nameTable, String nameColumn, int id) throws SQLException {
        ResultSet existId = this.getStatement()
                .executeQuery(String.format("SELECT %s FROM %s WHERE %s='%d'", nameColumn, nameTable, nameColumn, id));
        if (existId.next())
            return existId.getInt("IdUser") == id;
        return false;
    }
}
