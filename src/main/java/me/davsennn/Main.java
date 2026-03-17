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
    }

    public static PriorityQueue<Result> resultPriorityQueue;
    public static void execute() {
        split(Person.getPeople(), Room.getRooms().size());
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
    public static List<Person> parse(File csv) {
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

    /**
     * Splits a list of persons into a specified number of groups.
     * Each group will contain a sublist of persons.
     *
     * @param list   the list of persons to split
     * @param groups the number of groups to create
     */
    public static void split(List<Person> list, int groups) {
        if (groups <= 0)
            throw new IllegalArgumentException("Invalid number of groups: " + groups);
        resultPriorityQueue = new PriorityQueue<>(11, Comparator.comparingDouble(a -> a.score));
        List<List<List<Person>>> result = new ArrayList<>();
        int n = list.size();
        int[] assignment = new int[n];
        splitHelper(list, groups, 0, assignment);
    }

    static int counted = 0;
    private static void splitHelper(List<Person> list, int groups, int idx, int[] assignment) {
        if (idx == list.size()) {
            // Build the partition from the assignment
            List<List<Person>> partition = new ArrayList<>();
            for (int i = 0; i < groups; i++) {
                partition.add(new ArrayList<>());
            }
            for (int i = 0; i < list.size(); i++) {
                partition.get(assignment[i]).add(list.get(i));
            }
            double score = Person.calculateOptimality(partition, null);
            resultPriorityQueue.add(new Result(partition, score));
            counted++;
            if (resultPriorityQueue.size() >= 11) {
                resultPriorityQueue.remove();
            }
            Result r = resultPriorityQueue.poll();
            if (r != null && (score != Double.NEGATIVE_INFINITY && score >= r.score - 10))
                System.out.println(counted + ":" + score + ":" + partition);

            return;
        }
        for (int g = 0; g < groups; g++) {
            assignment[idx] = g;
            splitHelper(list, groups, idx + 1, assignment);
        }
    }
}
