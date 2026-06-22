package me.davsennn;

import me.davsennn.algorithm.Person;
import me.davsennn.algorithm.Room;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public final class FileIO {

    public FileIO() { throw new AssertionError("FileIO does not support instances"); }

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
