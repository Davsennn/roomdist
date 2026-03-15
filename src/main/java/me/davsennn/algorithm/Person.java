package me.davsennn.algorithm;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Person {
    private static List<UUID> ids = new ArrayList<>();

    public static List<UUID> getIds() {
        return ids;
    }

    private UUID id;
    private String name;
    private YearMonth birth;
    private String location;
    private char gender; // 'm' for male, 'f' for female, 'd' for diverse
    private char group;

    public void setName(String name) {
        this.name = name;
    }

    public void setBirth(YearMonth birth) {
        this.birth = birth;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setGender(char gender) {
        this.gender = gender;
    }

    public void setGroup(char group) {
        this.group = group;
    }

    private final List<Person> preferences;

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public YearMonth getBirth() {
        return birth;
    }

    public String getLocation() {
        return location;
    }

    public char getGender() {
        return gender;
    }

    public List<Person> getPreferences() {
        return preferences;
    }

    public char getGroup() {
        return group;
    }

    public Person(String name, YearMonth birth, String location, char gender, List<Person> preferences, char group) {
        this.name = name;
        this.birth = birth;
        this.location = location;
        this.gender = gender;
        this.preferences = preferences;
        this.group = group;
        this.id = UUID.randomUUID();
        ids.add(this.id);
    }

    public float ageDiffYears(YearMonth other) {
        return (this.birth.getYear() - other.getYear()) + (float) (this.birth.getMonthValue() - other.getMonthValue()) /12;
    }


}
