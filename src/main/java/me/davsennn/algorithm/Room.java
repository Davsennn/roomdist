package me.davsennn.algorithm;

import me.davsennn.Config;

import java.util.*;

public record Room(String id, int capacity) {
    private static List<Room> rooms = new ArrayList<>();
    private static List<Room> lastRemoved;

    public static List<Room> getRooms() {
        return rooms;
    }

    // availableRooms.get(capacity) return how many rooms of specified capacity are left
    public static TreeMap<Integer, Integer> roomSizes = new TreeMap<>();
    public static TreeMap<Integer, Integer> availableRooms = new TreeMap<>();
    private static int maxCapacity;

    public static int getMaxCapacity() {
        return maxCapacity;
    }

    public static void finish() {
        maxCapacity = rooms.stream()
                .map(Room::capacity)
                .max(Integer::compareTo)
                .orElse(Integer.MAX_VALUE);

        roomSizes = new TreeMap<>();
        for (Room r : rooms) {
            Room.roomSizes.merge(r.capacity(), 1, Integer::sum);
        }
    }

    public static void resetAvailableRooms() {
        availableRooms = new TreeMap<>(roomSizes);
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
        rooms = new ArrayList<>();
    }

    public static String everywhere() {
        if (rooms.isEmpty()) return "[]";
        StringBuilder ret = new StringBuilder();
        for (Room r : rooms) {
            ret.append(r.toString()).append("\n");
        }
        return ret.toString();
    }

    public static int chooseRoom(int groupSize) {
        //return groupSize;
        if (groupSize == 0) return 0;

        Integer key;

        // exact match
        key = getAndDecrement(availableRooms, groupSize);
        if (key != null) return key;

        // groupSize - 1
        key = getAndDecrement(availableRooms, groupSize - 1);
        if (key != null) return key;

        key = getAndDecrement(availableRooms, availableRooms.ceilingKey(groupSize));
        if (key != null) return key;

        return -1;
    }

    private static Integer getAndDecrement(TreeMap<Integer, Integer> map, Integer key) {
        if (key == null) return null;
        Integer count = map.get(key);
        if (count == null) return null;

        if (count == 1) map.remove(key);
        else map.put(key, count - 1);

        return key;
    }


    public Room(String id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        if (!rooms.contains(this)) {
            Room.rooms.add(this);
            Room.availableRooms.merge(this.capacity, 1, Integer::sum);
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
        score += group.size() * Config.getLargeGroupBonus(); // Bonus for larger groups
        if (group.size() < capacity - 2)
            score -= Config.getUnderOccupancyPenalty() * (capacity - group.size()); // Penalty for under-occupancy
        if (group.size() == capacity + 1)
            score -= Config.getCriticalOccupancyPenalty(); // Penalty for critical occupancy

        return score;
    }
}
