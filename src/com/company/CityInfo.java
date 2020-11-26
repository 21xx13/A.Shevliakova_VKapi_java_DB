package com.company;

//информация о городе
public class CityInfo {
    private final String name; //название
    private int count; //количество друзей, живущих в данном городе

    public CityInfo(String name, int count) {
        this.count = count;
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    public int count() {
        return this.count;
    }


    public void incCount(){
        this.count++;
    }

    public String toString() {
        return String.format("%s: %d, ", this.name, this.count);
    }
}
