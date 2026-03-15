package me.davsennn.algorithm;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class Person {
    private static List<Person> people = new ArrayList<>();

    public static List<Person> getPeople() {
        return people;
    }

    public static void addPeople(Collection<? extends Person> people) {
        Person.people.addAll(people
                            .stream()
                            .filter(p -> !people.contains(p))
                            .toList());
        // prevent duplicates by name
    }

    public static void clearPeople() {
        Person.people = new ArrayList<>();
    }

    public static Person fromName(String name) {
        for (Person p : people) {
            if (p.getName().equals(name))
                return p;
        }
        return null;
    }

    public static String everyone() {
        if (people.isEmpty()) return "[]";
        StringBuilder ret = new StringBuilder(
                "{name}, {birth}[MM YYYY], {location}, {gender}[\"f\"|\"m\"|\"d\"], {preferences}[\"[name1;name2; ...]\"\"], {group}\n");
        for (Person p : people) ret.append(p.toString()).append("\n");
        return ret.toString();
    }

    private UUID id;
    private String name;
    private YearMonth birth;
    private String location;
    private char gender; // 'm' for male, 'f' for female, 'd' for diverse
    private char group;

    private List<Person> preferences;

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

    public void setPreferences(List<Person> preferences) {
        this.preferences = preferences;
    }

    public void setGroup(char group) {
        this.group = group;
    }

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
        people.add(this);
        System.out.println(people.toArray().length);
    }

    public float ageDiffYears(YearMonth other) {
        return (this.birth.getYear() - other.getYear()) + (float) (this.birth.getMonthValue() - other.getMonthValue()) /12;
    }

    @Override
    public String toString() {
        StringBuilder prefstring = new StringBuilder("[");
        for (Person p : preferences) {
            prefstring.append(p.getName()).append(";");
        }
        if (prefstring.length() != 1) {
            prefstring.deleteCharAt(prefstring.length() - 1);
        }
        prefstring.append("], ");
        return name + ", " + birth.format(DateTimeFormatter.ofPattern("MM uuuu")) + ", " + location + ", " +
                gender + ", " + prefstring.toString() + group;
    }


}
