package me.davsennn.algorithm;

import me.davsennn.Config;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    private final UUID id;
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

    public boolean prefers(Person other) {
        return preferences.contains(other);
    }

    public boolean mutualPreference(Person other) {
        return prefers(other) && other.prefers(this);
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
                gender + ", " + prefstring + group;
    }

    public static double calculatePreferenceScore(List<Person> ppl, Map<Person[], Double> custom_bonuses) {
        if (!(Config.getPreferenceBonus() >= 0)) {
            Config.setDefaults();
        }
        if (ppl == null || ppl.size() < 2) {
            throw new NullPointerException("Persons cannot be null for preference score calculation.");
        }
        if (ppl.size() >= 14)
            return Double.NEGATIVE_INFINITY;

        double score = 0.0;
        Person oldest = null;
        Person youngest = null;

        for (Person p : ppl) {
            List<Person> preferences = new ArrayList<>(p.getPreferences());

            for (Person q : ppl) {
                preferences.remove(q);

                if (p.equals(q)) continue; // Skip self-comparison
                if (p.prefers(q))                               score += Config.getPreferenceBonus();
                else                                            score -= Config.getNonPreferencePenalty(); // Penalize non-preference
                if (p.mutualPreference(q))                      score += Config.getMutualPreferenceBonus() / 2;
                if (p.getLocation().equals(q.getLocation()))    score += Config.getSameLocationBonus();
                if (p.getGender() == q.getGender())             score += Config.getSameGenderBonus();



                if (custom_bonuses != null && custom_bonuses.containsKey(new Person[]{p, q})) {
                    score += custom_bonuses.get(new Person[]{p, q});
                }

                if (oldest == null || oldest.getBirth().isAfter(p.getBirth())) oldest = p;
                if (youngest == null || youngest.getBirth().isBefore(p.getBirth())) youngest = p;
            }

            score -= preferences.size() * Config.getUnfulfilledPreferencePenalty(); // Penalize for unfulfilled preferences
        }
        assert oldest != null;
        double ageDiff = oldest.ageDiffYears(youngest.getBirth());
        if (ageDiff >= Config.getAgeDifferenceThreshold())          score -= ageDiff * Config.getAgeDifferencePenalty();
        if (ageDiff >= Config.getLargeAgeDifferenceThreshold())     score -= ageDiff * Config.getLargeAgeDifferencePenalty();

        score /= (ppl.size() * (ppl.size() - 1)); // Normalize score by number of comparisons
        return score;
    }

    public static double calculateOptimality(List<List<Person>> groups, Map<Person[], Double> custom_bonuses) {
        if (groups == null || groups.isEmpty()) {
            throw new NullPointerException("Groups cannot be null or empty for optimality calculation.");
        }
        double totalScore = 0.0;
        for (List<Person> group : groups) {
            // Room room = Room.chooseRoom(group);
            // totalScore += room != null ? room.calculateGroupOptimality(group, custom_bonuses) : 0;
            int room = Room.chooseRoom(group.size());
            if (room == -1) return Double.NEGATIVE_INFINITY;
            totalScore += Room.calculateOptimality(group, room);
        }
        return totalScore;
    }
}
