package me.davsennn.algorithm;

import me.davsennn.Config;

import java.time.YearMonth;
import java.util.*;

// name must be unique
public class Person implements Comparable<Person> {
    // static methods
    public static final int now = YearMonth.now().getYear()*12 + YearMonth.now().getMonthValue();

    private static final List<Person> people = new ArrayList<>();
    private static List<Person> lastRemoved;
    private static final Map<Short, String> locationMap = new HashMap<>();
    public static LinkedHashMap<PersonPair, Double> custom_bonuses;

    private static Config.PortableConfig config = new Config.PortableConfig();

    public static List<Person> getPeople() {
        return people;
    }
    public static void updateConfig() { config = new Config.PortableConfig(); }

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
        Person.people.clear();
        Person.locationMap.clear();
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
    private final int birthMonth; // Distance in months from birth month to Jan 0 CE
    private final short location;
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
        return YearMonth.of(birthMonth / 12, birthMonth % 12);
    }

    public int getBirthMonth() {
        return birthMonth;
    }

    public String getLocation() {
        return locationMap.get(this.location);
    }

    public short getLocationCode() {
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
        Optional<Short> idx = locationMap.keySet().stream().min(Short::compareTo);
        short nextKey = idx.isPresent() ? (short) (idx.get() + 1) : 0;
        locationMap.put(nextKey, location);

        this.name = name;
        this.birthMonth = birth.getYear()*12 + birth.getMonthValue();
        this.location = nextKey;
        this.gender = gender;
        this.preferences = preferences;
        this.group = group;
        people.add(this);
    }

    public int ageDiffMonths(int other) {
        return Math.abs(birthMonth - other);
    }

    public boolean prefers(Person other) {
        return preferences.contains(other);
    }

    @Override
    public boolean equals (Object o) {
        if (!(o instanceof Person)) return false;
        return name.equals(((Person) o).name);
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
        return String.format("%1$s, %2$tm %2$tY, %3$s, %4$s, %5$s%6$s", name, getBirth(), location, gender, prefstring, group);
    }

    // Calculation
    public static double calculatePreferenceScore(List<Person> ppl) {
        if (ppl == null || ppl.size() < 2)
            return 0;

        if (ppl.size() >= Room.getMaxCapacity())
            return Double.NEGATIVE_INFINITY;

        double score = 0.0;
        boolean applyLargeGroupBonus = true;

        for (Person p : ppl) {
            int noPreferences = p.getPreferences().size();

            for (Person q : ppl) {
                if (p.equals(q)) continue;
                if (p.getGroup() != q.getGroup()) return Double.NEGATIVE_INFINITY;

                boolean pref = p.prefers(q);
                if (pref) --noPreferences;

                int ageDiff = p.ageDiffMonths(q.getBirthMonth());

                if (pref)                                       score += q.prefers(p) ?
                                                                         config.MUTUAL_PREFERENCE_BONUS()/2 :
                                                                         config.PREFERENCE_BONUS();
                else                                            score -= config.NON_PREFERENCE_PENALTY();
                if (p.getLocationCode() == q.getLocationCode()) score += config.SAME_LOCATION_BONUS();
                if (p.getGender() == q.getGender())             score += config.SAME_GENDER_BONUS();
                if (ageDiff >= config.AGE_DIFFERENCE_THRESHOLD())         { score -= ageDiff * config.AGE_DIFFERENCE_PENALTY();
                if (ageDiff >= config.LARGE_AGE_DIFFERENCE_THRESHOLD())     score -= ageDiff * config.LARGE_AGE_DIFFERENCE_PENALTY(); }

                PersonPair pq = new PersonPair(p, q);
                if (custom_bonuses != null && custom_bonuses.containsKey(pq)) {
                    double customScore = custom_bonuses.get(pq);
                    if (customScore == Double.NEGATIVE_INFINITY) return Double.NEGATIVE_INFINITY;
                    else score += customScore / 2;
                }
            }

            if (p.ageDiffMonths(now) > config.LARGE_GROUP_AGE_LIMIT()) applyLargeGroupBonus = false;

            score -= noPreferences * config.UNFULFILLED_PREFERENCE_PENALTY(); // Penalize for unfulfilled preferences
        }
        if (applyLargeGroupBonus && ppl.size() <= config.LARGE_GROUP_SIZE_THRESHOLD())
            score += config.LARGE_GROUP_BONUS() * ppl.size();

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
