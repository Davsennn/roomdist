package me.davsennn.algorithm;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Person {
    public static List<UUID> ids = new ArrayList<>();

    private final UUID id;
    private final String name;
    private final YearMonth birth;
    private final String location;
    private final char gender; // 'm' for male, 'f' for female, 'd' for diverse

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

    public Person(String name, YearMonth birth, String location, char gender, List<Person> preferences) {
        this.name = name;
        this.birth = birth;
        this.location = location;
        this.gender = gender;
        this.preferences = preferences;
        this.id = UUID.randomUUID();
        ids.add(this.id);
    }

    public float ageDiffYears(YearMonth other) {
        return (this.birth.getYear() - other.getYear()) + (float) (this.birth.getMonthValue() - other.getMonthValue()) /12;
    }


}
