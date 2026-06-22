package me.davsennn.algorithm;

import me.davsennn.Config;

import java.time.YearMonth;
import java.util.*;

// @SuppressWarnings("unused")
public class Person implements Comparable<Person> {
    // static methods
    public static final int now = YearMonth.now().getYear()*12 + YearMonth.now().getMonthValue();

    private static final List<Person> people = new ArrayList<>();
    private static List<Person> lastRemoved;
    private static final Map<Short, String> locationMap = new HashMap<>();
    private static short id_index = 0;
    public static LinkedHashMap<PersonPair, Double> custom_bonuses;
    public static boolean use_custom_bonuses;

    private static boolean[][] preferenceMatrix;
    private static double[] unfulfilledPreferencePenalties;
    private static double[][] directedPairScores;
    private static double[][] symmetricPairScores;

    private static Config.PortableConfig config = new Config.PortableConfig();

    public static List<Person> getPeople() {
        return people;
    }
    public static void updateConfig() { config = new Config.PortableConfig(); }

    public static Person[] getPeopleSorted() {
        Person[] ret = people.toArray(new Person[0]);
        Arrays.sort(ret, Person::compareId);
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
        // System.out.println(lastRemoved);
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
        id_index = 0;
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
    private final short id; // for more people than 32K, youd have to wait 99999 years anyway
    private final String name;
    private final int birthMonth; // Distance in months from birth month to Jan 0 CE
    private final short location;
    private final char gender; // 'm' for male, 'f' for female, 'd' for diverse
    private final char group;

    private List<Person> preferences;

    public void setPreferences(List<Person> preferences) {
        this.preferences = preferences;
    }

    public short getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public YearMonth getBirth() {
        return YearMonth.of(birthMonth / 12, (birthMonth % 12) + 1);
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

    public int compareId(Person other) {
        return id - other.id;
    }

    private static short getOrCreateLocationKey(String location) {
        for (Map.Entry<Short, String> entry : locationMap.entrySet()) {
            if (Objects.equals(entry.getValue(), location)) {
                return entry.getKey();
            }
        }

        short nextKey = locationMap.keySet().stream()
                .max(Short::compareTo)
                .map(maxKey -> {
                    if (maxKey == Short.MAX_VALUE) {
                        throw new IndexOutOfBoundsException(maxKey + " is out of bounds for location code");
                    }
                    return (short) (maxKey + 1);
                })
                .orElse((short) 0);
        locationMap.put(nextKey, location);
        return nextKey;
    }

    public Person(String name, YearMonth birth, String location, char gender, List<Person> preferences, char group) {
        this.location = getOrCreateLocationKey(location);
        this.id = id_index++;
        this.name = name;
        this.birthMonth = birth.getYear()*12 + birth.getMonthValue() - 1;
        this.gender = gender;
        this.preferences = preferences;
        this.group = group;
        people.add(this.id, this);
    }

    public int ageDiffMonths(int other) {
        return Math.abs(birthMonth - other);
    }

    public boolean prefers(Person other) {
        return preferences.contains(other);
    }

    public static boolean prefers(Person a, Person b) {
        return preferenceMatrix[a.getId()][b.getId()];
    }

    public static void prepareScoring() {
        unfulfilledPreferencePenalties = new double[id_index];
        directedPairScores = new double[id_index][id_index];
        symmetricPairScores = new double[id_index][id_index];
        preferenceMatrix = new boolean[id_index][id_index];

        for (Person p : people) {
            int pId = p.getId();
            unfulfilledPreferencePenalties[pId] = p.getPreferences().size() * config.UNFULFILLED_PREFERENCE_PENALTY();
            directedPairScores[pId][pId] = Double.NEGATIVE_INFINITY;

            for (Person q : people) {
                int qId = q.getId();
                if (p == q) continue;

                preferenceMatrix[p.getId()][q.getId()] = p.prefers(q);

                double score = 0.0;

                if (use_custom_bonuses && !custom_bonuses.isEmpty()) {
                    Double customScore = custom_bonuses.get(new PersonPair(p, q));
                    if (customScore != null) {
                        if (customScore == Double.NEGATIVE_INFINITY) {
                            directedPairScores[pId][qId] = Double.NEGATIVE_INFINITY;
                            continue;
                        }
                        score += customScore / 2;
                    }
                }

                int ageDiff = p.ageDiffMonths(q.getBirthMonth());
                if (p.getLocationCode() == q.getLocationCode()) score += config.SAME_LOCATION_BONUS();
                if (p.getGender() == q.getGender())             score += config.SAME_GENDER_BONUS();
                if (ageDiff >= config.AGE_DIFFERENCE_THRESHOLD())         { score -= ageDiff * config.AGE_DIFFERENCE_PENALTY();
                if (ageDiff >= config.LARGE_AGE_DIFFERENCE_THRESHOLD())     score -= ageDiff * config.LARGE_AGE_DIFFERENCE_PENALTY(); }

                boolean pref = preferenceMatrix[pId][qId];
                if (pref) {
                    score += preferenceMatrix[qId][pId] ?
                            config.MUTUAL_PREFERENCE_BONUS() :
                            config.PREFERENCE_BONUS();
                } else {
                    score -= config.NON_PREFERENCE_PENALTY();
                }
                directedPairScores[pId][qId] = score;
            }
        }

        for (int i = 0; i < id_index; i++) {
            for (int j = 0; j < id_index; j++) {
                double score = directedPairScores[i][j]
                             + directedPairScores[j][i];
                symmetricPairScores[i][j] = score;
                symmetricPairScores[j][i] = score;
            }
        }
    }

    @Override
    public boolean equals (Object o) {
        if (!(o instanceof Person)) return false;
        return id == ((Person) o).id;
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

        if (ppl.size() > Room.getMaxCapacity() + 1)
            return Double.NEGATIVE_INFINITY;

        double score = 0.0;

        boolean applyLargeGroupBonus = true;

        for (int i = 0; i < ppl.size(); ++i) {
            Person p = ppl.get(i);
            int pId = p.getId();
            score -= unfulfilledPreferencePenalties[pId];
            if (p.ageDiffMonths(now) > config.LARGE_GROUP_AGE_LIMIT()) applyLargeGroupBonus = false;

            for (int j = 0; j < ppl.size(); ++j) {
                if (i == j) continue;
                Person q = ppl.get(j);
                int qId = q.getId();
                double pairScore = directedPairScores[pId][qId];
                if (pairScore == Double.NEGATIVE_INFINITY)
                    return Double.NEGATIVE_INFINITY;

                score += pairScore;
                if (preferenceMatrix[pId][qId])
                    score += config.UNFULFILLED_PREFERENCE_PENALTY();
            }
        }
        if (applyLargeGroupBonus && ppl.size() <= config.LARGE_GROUP_SIZE_THRESHOLD())
            score += config.LARGE_GROUP_BONUS() * ppl.size();

        score /= (ppl.size() - 1); // Normalize score by number of comparisons
        return score;
    }

    public static double branchOrderScore(List<Person> group, Person candidate) {
        double score = 0;

        for (Person p : group)
            score += symmetricPairScores[candidate.getId()][p.getId()];

        return score;
    }

}
