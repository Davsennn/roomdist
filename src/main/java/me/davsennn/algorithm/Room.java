package me.davsennn.algorithm;

import me.davsennn.Config;

import javax.naming.SizeLimitExceededException;
import java.util.*;

public record Room(String id, int capacity) {
    private static final List<Room> rooms = new ArrayList<>();
    private static List<Room> lastRemoved;

    private static int maxCapacity;

    public static List<Room> getRooms() {
        return rooms;
    }

    public static int getMaxCapacity() {
        return maxCapacity;
    }

    public static void finish() throws SizeLimitExceededException {
        maxCapacity = rooms.stream()
                .map(Room::capacity)
                .max(Integer::compareTo)
                .orElse(Integer.MAX_VALUE);

        removeOverhangRooms();
    }

    public static void removeOverhangRooms() throws SizeLimitExceededException {
        int amtPeople = Person.getPeople().size();
        int amtBeds = 0;
        for (Room r : rooms) amtBeds += r.capacity;
        int diff = amtBeds - amtPeople;
        System.out.println(amtPeople + " people, " + amtBeds + " beds, " + diff + " diff");
        if (diff < 0) throw new SizeLimitExceededException(amtPeople + " is too many people for " + amtBeds + " beds");
        while (diff > 0) {
            Optional<Room> minSize = rooms.stream().min(Comparator.comparingInt(Room::capacity));
            if (minSize.isEmpty()) return;
            Room min = minSize.get();
            if (diff < min.capacity) return;
            rooms.remove(min);
            diff -= min.capacity;
        }
        rooms.sort(Comparator.comparingInt(a -> -a.capacity));
    }


    public static void addRooms(Collection<? extends Room> rooms) {
        Room.rooms.addAll(rooms
                .stream()
                .filter(room -> !rooms.contains(room))
                .toList());
    }

    public static void removeRooms(int[] indices) {
        List<Room> remove = new ArrayList<>(indices.length);
        for (int i : indices)
            remove.add(rooms.get(i));
        lastRemoved = remove;
        rooms.removeAll(remove);
    }

    public static void restore() {
        if (lastRemoved == null) return;
        rooms.addAll(lastRemoved);
        lastRemoved = null;
    }

    public static void clearRooms() {
        rooms.clear();
    }

    public static String everywhere() {
        if (rooms.isEmpty()) return "[]";
        StringBuilder ret = new StringBuilder();
        for (Room r : rooms) {
            ret.append(r.toString()).append("\n");
        }
        return ret.toString();
    }


    public Room(String id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        if (!rooms.contains(this)) {
            Room.rooms.add(this);
        }
    }

    public String toString() {
        return this.id + "," + this.capacity;
    }

    public static double calculateOptimality(List<Person> group, int capacity) {
        if (group == null || group.size() < 2) {
            return 0.0;
        }
        if (group.size() > capacity + 1) {
            return Double.NEGATIVE_INFINITY; // Group exceeds room capacity
        }
        double score = Person.calculatePreferenceScore(group);
        if (group.size() == capacity + 1)
            score -= Config.getCriticalOccupancyPenalty(); // Penalty for critical occupancy

        return score;
    }
}
