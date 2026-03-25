package me.davsennn.algorithm;

import me.davsennn.Config;

import java.time.YearMonth;
import java.util.*;

public class Person implements Comparable<Person> {
    // static methods
    public static final YearMonth now = YearMonth.now();

    private static List<Person> people = new ArrayList<>();
    private static List<Person> lastRemoved;
    public static Map<Person[], Double> custom_bonuses;

    public static List<Person> getPeople() {
        return people;
    }

    public static Person[] getPeopleSorted() {
        Person[] ret = people.toArray(new Person[0]);
        Arrays.sort(ret);
        return ret;
    }

    public static void addPeople(Collection<? extends Person> people) {
        Person.people.addAll(people
                            .stream()
                            .filter(p -> !people.contains(p))
                            .toList());
        // prevent duplicates by name
    }

    public static void removePeople(int[] people) {
        Person[] people1 = Person.getPeopleSorted();
        List<Person> remove = new ArrayList<>(people.length);
        for (int i : people) {
            remove.add(people1[i]);
        }
        lastRemoved = remove;
        System.out.println(lastRemoved);
        Person.people.removeAll(remove);
    }

    public static void restore() {
        if (lastRemoved == null) return;
        people.addAll(lastRemoved);
        lastRemoved = null;
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
        for (Person p : getPeopleSorted()) ret.append(p.toString()).append("\n");
        return ret.toString();
    }

    // Instance
    private final String name;
    private final YearMonth birth;
    private final String location;
    private final char gender; // 'm' for male, 'f' for female, 'd' for diverse
    private final char group;

    private List<Person> preferences;

    public void setPreferences(List<Person> preferences) {
        this.preferences = preferences;
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

    @Override
    public int compareTo(Person other) {
        return getName().compareTo(other.getName());
    }

    public Person(String name, YearMonth birth, String location, char gender, List<Person> preferences, char group) {
        this.name = name;
        this.birth = birth;
        this.location = location;
        this.gender = gender;
        this.preferences = preferences;
        this.group = group;
        people.add(this);
    }

    public float ageDiffYears(YearMonth other) {
        return Math.abs((this.birth.getYear() - other.getYear()) + (float) (this.birth.getMonthValue() - other.getMonthValue()) /12);
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
        return String.format("%1$s, %2$tm %2$tY, %3$s, %4$s, %5$s%6$s", name, birth, location, gender, prefstring, group);
    }

    // Calculation
    public static double calculatePreferenceScore(List<Person> ppl) {
        if (!(Config.getPreferenceBonus() >= 0))
            Config.setDefaults();

        if (ppl == null || ppl.size() < 2)
            return 0;

        if (ppl.size() >= Room.getMaxCapacity())
            return Double.NEGATIVE_INFINITY;

        double score = 0.0;

        for (Person p : ppl) {
            int noPreferences = p.getPreferences().size();

            for (Person q : ppl) {
                if (p.prefers(q)) --noPreferences;

                if (p.equals(q)) continue;
                if (p.getGroup() != q.getGroup()) return Double.NEGATIVE_INFINITY;

                double ageDiff = p.ageDiffYears(q.getBirth());

                if (p.mutualPreference(q))                      score += Config.getMutualPreferenceBonus() / 2;
                else if (p.prefers(q))                          score += Config.getPreferenceBonus();
                else                                            score -= Config.getNonPreferencePenalty();
                if (p.getLocation().equals(q.getLocation()))    score += Config.getSameLocationBonus();
                if (p.getGender() == q.getGender())             score += Config.getSameGenderBonus();
                if (ageDiff >= Config.getAgeDifferenceThreshold())          score -= ageDiff * Config.getAgeDifferencePenalty();
                if (ageDiff >= Config.getLargeAgeDifferenceThreshold())     score -= ageDiff * Config.getLargeAgeDifferencePenalty();

                Person[] pq = new Person[]{p, q};
                if (custom_bonuses != null && custom_bonuses.containsKey(pq))
                    score += custom_bonuses.get(pq);
            }

            if (p.ageDiffYears(now) <= Config.getLargeGroupAgeLimit() &&
                ppl.size() >= Config.getLargeGroupSizeThreshold())          score += Config.getLargeGroupBonus() * ppl.size();


            score -= noPreferences * Config.getUnfulfilledPreferencePenalty(); // Penalize for unfulfilled preferences
        }

        score /= (ppl.size() - 1); // Normalize score by number of comparisons
        return score;
    }

    public static double calculateOptimality(List<List<Person>> groups) {
        if (groups == null || groups.isEmpty()) {
            return Double.NEGATIVE_INFINITY;
        }
        double totalScore = 0.0;
        for (List<Person> group : groups) {
            int room = Room.chooseRoom(group.size());
            if (room == -1) return Double.NEGATIVE_INFINITY;
            totalScore += Room.calculateOptimality(group, room);
        }
        Room.resetAvailableRooms();
        return totalScore;
    }
}
