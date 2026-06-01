package com.pocketempire.entities;

import java.util.ArrayList;

public class Faction {
    private int id;
    private String name;
    private int color;
    private ArrayList<Unit> units;
    private ArrayList<City> cities;
    private boolean isAlive;
    private boolean isAI;

    public Faction(int id, String name, int color) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.units = new ArrayList<>();
        this.cities = new ArrayList<>();
        this.isAlive = true;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getColor() {
        return color;
    }

    public ArrayList<Unit> getUnits() {
        return units;
    }

    public ArrayList<City> getCities() {
        return cities;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public void addUnit(Unit unit) {
        units.add(unit);
    }

    public void removeUnit(Unit unit) {
        units.remove(unit);
    }

    public void addCity(City city) {
        cities.add(city);
    }

    public void removeCity(City city) {
        cities.remove(city);
    }

    public int getUnitCount() {
        return units.size();
    }

    public int getCityCount() {
        return cities.size();
    }

    public boolean isAI() {
        return isAI;
    }

    public void setAI(boolean isAI) {
        this.isAI = isAI;
    }
}
