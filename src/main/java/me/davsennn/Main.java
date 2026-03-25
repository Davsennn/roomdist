package me.davsennn;

import javax.swing.*;
import me.davsennn.algorithm.*;

import java.io.*;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Main {
    public static final String version = "0.2.0";

    static void main() {
        Config.setDefaults();
        SwingUtilities.invokeLater(GUI::createWindow);
    }

    public static long processed = 0;
    public static PriorityQueue<Result> resultPriorityQueue;
    public static Result[] results;
    public static long startTime;
    public static long endTime;
    public static void execute() {
        processed = 0;
        startTime = System.nanoTime();
        // split(Person.getPeople(), Room.getRooms().size());
        Room.finish();
        resultPriorityQueue = new PriorityQueue<>(11, (a, b) -> 2*Double.compare(a.score, b.score) - a.id.compareTo(b.id));
        System.out.println("Starting...");
        assignRoom(0, Person.getPeople(), new ArrayList<>(), 0);
        endTime = System.nanoTime();
        System.out.println("Execution stopped. "
                + String.format("%,d", processed) + " Paths processed, "
                + String.format("%,d", (endTime - startTime)/1000000L) + "ms elapsed + "
                + String.format("%,d", (endTime - startTime)%1000000L) + "ns");
        System.out.println("TOP 10 (" + resultPriorityQueue.size() + ")");
        results = resultPriorityQueue.toArray(new Result[10]);
        Arrays.sort(results, Comparator.comparingDouble(r -> r.score));
        for (int i = results.length - 1; i >= 0; --i) {
            System.out.println(String.format("%+5.4g", results[i].score) + " | " + results[i]);
        }
    }

    private static void assignRoom(int roomIdx,
                           List<Person> remaining,
                           List<List<Person>> current,
                           double currentScore) {

        if (currentScore == Double.NEGATIVE_INFINITY) return;


        if (roomIdx == Room.getRooms().size() || remaining.isEmpty()) {
            if (remaining.isEmpty()) {
                resultPriorityQueue.add(new Result(new ArrayList<>(current), Person.calculateOptimality(current)));
                if (resultPriorityQueue.size() >= 11) {
                    resultPriorityQueue.remove();
                }
            }

            return;
        }

        Room room = Room.getRooms().get(roomIdx);

        // build this room's group
        buildGroup(roomIdx, room, remaining, 0,
                new ArrayList<>(), current, currentScore);
    }

    private static void buildGroup(int roomIdx,
                           Room room,
                           List<Person> remaining,
                           int start,
                           List<Person> group,
                           List<List<Person>> current,
                           double currentScore) {

        int size = group.size();


        // --- IF GROUP SIZE IS USABLE ---
        if (size >= room.capacity() - 2 &&
            size <= room.capacity() + 1) {

            double fullScore = Room.calculateOptimality(group, room.capacity());
            double newScore = currentScore + fullScore;

            // recurse to next room
            current.add(new ArrayList<>(group));

            List<Person> newRemaining = new ArrayList<>(remaining);
            newRemaining.removeAll(group);
            processed++;
            if (processed % 1000000 == 0) {
                Result best = resultPriorityQueue.peek();
                double s;
                if (best != null) {
                    s = Person.calculateOptimality(best.config);
                    System.out.printf("D%1$02d | %2$,4dM | %4$4.4f | %3$s %n", roomIdx, processed/1000000, best, s);
                } else {
                    s = Person.calculateOptimality(current);
                    System.out.printf("D%1$02d | %2$,4dM | %4$4.4f | %3$s %n", roomIdx, processed/1000000, current, s);
                }
            }
            assignRoom(roomIdx + 1, newRemaining, current, newScore);

            current.removeLast();
        }

        // --- STOP IF TOO BIG ---
        if (size >= room.capacity() + 1) return;

        // --- EXTEND GROUP ---
        for (int i = start; i < remaining.size(); i++) {
            Person p = remaining.get(i);

            group.add(p);
            buildGroup(roomIdx, room, remaining, i + 1, group, current, currentScore);
            group.removeLast();
        }
    }

    /**
     * Parses a CSV file to create a list of Person objects.
     * The CSV format is expected to be:
     * <pre>
     * {name}, {birth}[MM YYYY], {location}, {gender}["f"|"m"|"d"], {preferences}["[name1;name2; ...]""], {group}
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
