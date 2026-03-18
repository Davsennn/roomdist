package me.davsennn;

import javax.swing.*;
import me.davsennn.algorithm.*;

import java.io.*;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {

    static void main(String[] args) {
        Config.setDefaults();
        SwingUtilities.invokeLater(GUI::createWindow);
        System.out.println(Double.compare(12, 6));
    }

    static long processed = 0;
    public static PriorityQueue<Result> resultPriorityQueue;
    public static Result[] results;
    static long startTime;
    static long endTime;
    public static void execute() {
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
        System.out.println("TOP 10");
        results = resultPriorityQueue.toArray(new Result[10]);
        Arrays.sort(results, Comparator.comparingDouble(r -> r.score));
        for (int i = results.length - 1; i >= 0; --i) {
            System.out.println(((double)((int)(results[i].score*100)))/100 + " | " + results[i]);
        }
    }

    static void assignRoom(int roomIdx,
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
                /*
                System.out.println("---------------------------");
                System.out.println("FINISHED");
                Result r = resultPriorityQueue.peek();
                if (r != null)
                    System.out.println(r.score + " | " + r.toString());

                for (Result r : resultPriorityQueue) {
                    System.out.println(((double)((int)(r.score*100)))/100 + " | " + r);
                }
                 */
            }

            return;
        }

        Room room = Room.getRooms().get(roomIdx);

        // build this room's group
        buildGroup(roomIdx, room, remaining, 0,
                new ArrayList<>(), current, currentScore);
    }

    static void buildGroup(int roomIdx,
                           Room room,
                           List<Person> remaining,
                           int start,
                           List<Person> group,
                           List<List<Person>> current,
                           double currentScore) {

        int size = group.size();


        // --- IF GROUP SIZE IS USABLE ---
        if (size >= room.getCapacity() - 1 &&
                size <= room.getCapacity() + 1) {

            double fullScore = Room.calculateOptimality(group, room.getCapacity());
            double newScore = currentScore + fullScore;

            // recurse to next room
            current.add(new ArrayList<>(group));

            List<Person> newRemaining = new ArrayList<>(remaining);
            newRemaining.removeAll(group);
            processed++;
            if (processed % 1000000 == 0) {
                double s = Person.calculateOptimality(current);
                System.out.println("D" + roomIdx + " | " + processed + " | " + s + " | " + Result.toString(current));
            }
            assignRoom(roomIdx + 1, newRemaining, current, newScore);

            current.removeLast();
        }

        // --- STOP IF TOO BIG ---
        if (size >= room.getCapacity() + 1) return;

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
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // Skip header line
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length < 2) {
                    throw new IllegalArgumentException("CSV row must have at least name and age");
                }
                String name = parts[0].trim();
                if (name.isEmpty()) {
                    throw new IllegalArgumentException("Name cannot be null or empty");
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM uuuu");
                YearMonth birth = YearMonth.parse(parts[1].trim(), formatter);
                String location = parts[2].trim();
                char gender = parts[3].trim().charAt(0);
                char group = parts[5].trim().charAt(0);

                // Create person without preferences and add to map
                Person person = new Person(name, birth, location, gender, List.of(), group);
                people.add(person);
                personMap.put(name, person);
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
                if (parts.length > 4 && !parts[4].trim().isEmpty()) {
                    String name = parts[0].trim();
                    String prefString = parts[4].trim();
                    if (prefString.startsWith("[") && prefString.endsWith("]")) {
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
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // Skip header line
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length < 2) {
                    throw new IllegalArgumentException("CSV row must have at least room ID and capacity");
                }
                int capacity;
                try {
                    capacity = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Capacity must be a valid integer", e);
                }
                rooms.add(new Room(parts[0].trim(), capacity));
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }
        return rooms;
    }

}
