package me.davsennn;

import javax.naming.SizeLimitExceededException;
import javax.swing.*;
import me.davsennn.algorithm.*;

import java.io.*;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public final class Main {
    public static final String version = "0.2.0";
    private static Config.PortableConfig config;

    static void main() {
        Config.setDefaults();
        SwingUtilities.invokeLater(GUI::createWindow);
    }

    public static long processed = 0;
    public static long pruned = 0;
    public static long tried_prune = 0;
    public static PriorityQueue<Result> resultPriorityQueue;
    public static double worst_best_score = Double.NEGATIVE_INFINITY;
    public static Result[] results;
    public static long startTime;
    public static long endTime;
    public static void execute() throws SizeLimitExceededException {
        processed = 0;
        startTime = System.nanoTime();
        init();
        checkSizeLimits();
        System.out.println("Starting...");
        assignRoom(0, Person.getPeople(), new ArrayList<>(), 0);
        endTime = System.nanoTime();
        System.out.printf("Execution stopped. %1$,d Paths processed, %2$,dms elapsed + %3$,dns (%4$,d Paths/sec)%n",
                processed, (endTime - startTime)/1000000L, (endTime - startTime)%1000000L, (processed*1000000000L/(endTime - startTime)));
        System.out.println("TOP 10 (" + resultPriorityQueue.size() + ")");
        results = resultPriorityQueue.toArray(new Result[10]);
        Arrays.sort(results);
        for (int i = results.length - 1; i >= 0; --i) {
            System.out.println(String.format("%+5.4g", results[i].score()) + " | " + results[i]);
        }
    }

    private static void init() {
        Room.finish();
        if (!(Config.getPreferenceBonus() >= 0))
            Config.setDefaults();
        Person.updateConfig();
        Person.use_custom_bonuses = !Person.custom_bonuses.isEmpty();
        config = new Config.PortableConfig();
        resultPriorityQueue = new PriorityQueue<>(11);
    }

    private static void checkSizeLimits() throws SizeLimitExceededException {
        int amtPeople = Person.getPeople().size();
        int amtBeds = 0;
        for (Room r : Room.getRooms()) {
            amtBeds += r.capacity();
        }
        if (amtPeople > amtBeds) throw new SizeLimitExceededException("Too many people (" + amtPeople + ") for too little beds (" + amtBeds + ")");
    }

    private static void assignRoom(int roomIdx,
                           List<Person> remaining,
                           List<List<Person>> current,
                           double currentScore) {

        if (currentScore == Double.NEGATIVE_INFINITY) return;


        if (roomIdx == Room.getRooms().size() || remaining.isEmpty()) {
            if (remaining.isEmpty()) {
                if (currentScore <= worst_best_score) return;
                resultPriorityQueue.add(new Result(new ArrayList<>(current), currentScore));
                if (resultPriorityQueue.size() >= 11) {
                    resultPriorityQueue.remove();
                }
                Result worstbest = resultPriorityQueue.peek();
                worst_best_score = worstbest == null ? worst_best_score : worstbest.score();
            }

            return;
        }

        Room room = Room.getRooms().get(roomIdx);

        // build this room's group
        buildGroup(roomIdx, room.capacity(), remaining, 0,
                new ArrayList<>(), current, currentScore);
    }

    private static void buildGroup(int roomIdx,
                           int room,
                           List<Person> remaining,
                           int start,
                           List<Person> group,
                           List<List<Person>> current,
                           double currentScore) {

        int size = group.size();


        // --- IF GROUP SIZE IS USABLE ---
        if (size >= room - 1 &&
            size <= room + 1) {

            double fullScore = Room.calculateOptimality(group, room);
            double newScore = currentScore + fullScore;

            // recurse to next room
            current.add(new ArrayList<>(group));

            List<Person> newRemaining = new ArrayList<>(remaining);
            newRemaining.removeAll(group);
            processed++;
            if (processed % 1000000 == 0) {
                Result best = resultPriorityQueue.peek();
                if (best != null) {
                    System.out.printf("%1$,4dM | %4$,4d | %5$,4d | %3$4.4f | %2$s %n", processed/1000000, best, best.score(), pruned, tried_prune);
                } else {
                    System.out.println(processed/1000000 + "|" + pruned + "|" + tried_prune);
                }

                //System.out.println(processed/1000000 + "|" + pruned + "|" + tried_prune);
            }
            assignRoom(roomIdx + 1, newRemaining, current, newScore);

            current.removeLast();
        }

        // --- STOP IF TOO BIG ---
        if (size >= room + 1) return;

        // --- EXTEND GROUP ---
        for (int i = start; i < remaining.size(); i++) {
            Person p = remaining.get(i);

            if (config.USE_EARLY_PRUNING() && roomIdx <= config.EARLY_PRUNING_LENGTH() && size != 0) { // prune early
                if (size <= config.EARLY_PRUNING_STRENGTH()) {
                    ++tried_prune;
                    boolean cut = true;
                    for (Person q : group)
                        if (p.prefers(q)) {
                            cut = false;
                            break;
                        }
                    if (cut) { ++pruned; return; }
                }
                if (remaining.size() + currentScore < worst_best_score) { ++pruned; return; }
            }

            group.add(p);
            buildGroup(roomIdx, room, remaining, i + 1, group, current, currentScore);
            group.removeLast();
        }
    }

    /**
     * Parses a CSV file to create a list of Person objects.
     * The CSV format is expected to be:
     * <pre>
     * {name}, {birth}[MM YYYY], {location}, {gender}["f"|"m"|"d"], {preferences}["[name1;name2; ...]"], {group}
     * </pre>
     * @param csv the CSV file to parse
     * @return a list of Person objects created from the CSV data
     * @throws IllegalArgumentException if the CSV format is invalid
     */
    public static List<Person> parsePeople(File csv) {
        if (csv == null || !csv.exists() || !csv.isFile()) {
            throw new IllegalArgumentException("CSV file does not exist or is not a valid file");
        }
        List<Person> people = new ArrayList<>();
        Map<String, Person> personMap = new HashMap<>(); // Map to store people by name for preference resolution

        try (BufferedReader br = new BufferedReader(new FileReader(csv))) {
            String line;
            int lineNr = 1;
            while ((line = br.readLine()) != null) {
                if (lineNr == 1) {
                     ++lineNr; // Skip header line
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length < 2)
                    throw new IllegalArgumentException("At line " + lineNr + ": CSV row must have at least name and age");

                String name = parts[0].trim();
                if (name.isEmpty())
                    throw new IllegalArgumentException("At line " + lineNr + ": Name cannot be null or empty");

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM uuuu");
                YearMonth birth;
                try {
                    birth = YearMonth.parse(parts[1].trim(), formatter);
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("At line " + lineNr + ": Illegal date format; " + e.getMessage(), e);
                }
                String location = parts[2].trim();
                char gender = parts[3].trim().charAt(0);
                if (!List.of('m', 'f', 'd').contains(gender))
                    throw new IllegalArgumentException("At line " + lineNr + ": Gender must be 'm', 'f' or 'd' (Found " + gender + ")");
                char group = parts[5].trim().charAt(0);
                if (List.of(' ', '\t', '\n', '\0').contains(group))
                    throw new IllegalArgumentException("At line " + lineNr + ": Group cannot be whitespace character");

                // Create person without preferences and add to map
                Person person = new Person(name, birth, location, gender, List.of(), group);
                people.add(person);
                personMap.put(name, person);
                ++lineNr;
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }

        // Second pass to resolve preferences
        try (BufferedReader br = new BufferedReader(new FileReader(csv))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // Skip header line
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length <= 4 || parts[4].trim().isEmpty()) {
                    continue;
                }
                String name = parts[0].trim();
                String prefString = parts[4].trim();
                if (!prefString.startsWith("[") || !prefString.endsWith("]")) {
                    continue;
                }
                String[] prefNames = prefString.substring(1, prefString.length() - 1).split(";");
                List<Person> preferences = Arrays.stream(prefNames)
                        .map(String::trim)
                        .filter(pref -> !pref.isEmpty())
                        .map(personMap::get) // Resolve actual Person objects
                        .filter(Objects::nonNull) // Ignore unresolved names
                        .toList();
                Person person = personMap.get(name);
                if (person != null) {
                    person.setPreferences(preferences); // Update preferences
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file during preference resolution: " + e.getMessage());
        }

        return people;
    }

    /**
     * Parses a CSV file to create a list of Room objects.
     * The CSV format is expected to be:
     * <pre>
     * RoomID,Capacity
     * Kapellenzimmer,12
     * Kinderturmstube,8
     * </pre>
     * @param csv the CSV file to parse
     * @return a list of Person objects created from the CSV data
     * @throws IllegalArgumentException if the CSV format is invalid
     */
    public static List<Room> parseRooms(File csv) {
        if (csv == null || !csv.exists() || !csv.isFile()) {
            throw new IllegalArgumentException("CSV file does not exist or is not a valid file");
        }
        List<Room> rooms = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csv))) {
            String line;
            int lineNr = 1;
            while ((line = br.readLine()) != null) {
                if (lineNr == 1) {
                    ++lineNr; // Skip header line
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length < 2) {
                    throw new IllegalArgumentException("At line " + lineNr + ": CSV row must have at least room ID and capacity");
                }
                int capacity;
                try {
                    capacity = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("At line " + lineNr + ": Capacity must be a valid integer; " + e.getMessage(), e);
                }
                rooms.add(new Room(parts[0].trim(), capacity));
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }
        return rooms;
    }

}
