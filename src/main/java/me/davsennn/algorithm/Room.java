package me.davsennn.algorithm;

import me.davsennn.Config;

import java.util.*;

public class Room {
    private static List<Room> rooms = new ArrayList<>();
    public static List<Room> getRooms() {
        return rooms;
    }
    // availableRooms.get(capacity) return how many rooms of specified capacity are left
    public static TreeMap<Integer, Integer> availableRooms = new TreeMap<>();
    private static int maxCapacity;
    public static int getMaxCapacity() {
        return maxCapacity;
    }
    public static void finish() {
        maxCapacity = rooms.stream()
                .map(Room::getCapacity)
                .max(Integer::compareTo)
                .orElse(Integer.MAX_VALUE);
    }
    public static void resetAvailableRooms() {
        availableRooms = new TreeMap<>();
        for (Room r : rooms) {
            Room.availableRooms.merge(r.getCapacity(), 1, Integer::sum);
        }
    }


    public static void addRooms(Collection<? extends Room> rooms) {
        Room.rooms.addAll(rooms
                        .stream()
                        .filter(room -> !rooms.contains(room))
                        .toList());
    }

    public static void clearRooms() {
        rooms = new ArrayList<>();
    }

    public static Room fromRoomID(String id) {
        for (Room r : rooms) {
            if (r.getId().equals(id))
                return r;
        }
        return null;
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
        if (groupSize == 0) return 0;
        //return groupSize;

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


    private String id;
    private int capacity;
    private List<Person> occupants;

    public Room(String id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        this.occupants = new ArrayList<>();
        if (!rooms.contains(this)) {
            Room.rooms.add(this);
            Room.availableRooms.merge(this.capacity, 1, Integer::sum);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<Person> getOccupants() {
        return occupants;
    }

    public void setOccupants(List<Person> occupants) {
        this.occupants = occupants;
    }

    public boolean addOccupant(Person person) {
        if (occupants.size() < capacity) {
            occupants.add(person);
            return true;
        } else if (occupants.size() == capacity) {
            occupants.add(person);
            System.out.println("Critical occupancy reached in room: " + id);
        } else {
            System.out.println("Room " + id + " is full. Cannot add " + person.getName());
        }
        return false;
    }

    public String toString() {
        return this.id + "," + this.capacity;
    }

    /**
     * Calculates the optimality score for a group of persons in this room.
     * The score is based on preference compatibility and group size.
     * Other parameters are passed to {@link Person#calculatePreferenceScore(List, Map)}.
     *
     * @return a score representing the optimality of the group in this room, Negative infinity if the mapping is not possible
     */
    public double calculateGroupOptimality(List<Person> group, Map<Person[], Double> custom_bonuses) {
        if (group == null || group.size() < 2) {
            return 0.0;
        }
        if (group.size() > capacity + 1) {
            System.out.println("Group exceeds room capacity: " + group.size() + " > " + capacity);
            return Double.NEGATIVE_INFINITY; // Group exceeds room capacity
        }
        double score = Person.calculatePreferenceScore(group, custom_bonuses);
        score += group.size() * Config.getLargeGroupBonus(); // Bonus for larger groups
        if (group.size() < capacity - 2)
            score -= Config.getUnderOccupancyPenalty() * (capacity - group.size()); // Penalty for under-occupancy
        if (group.size() == capacity + 1)
            score -= Config.getCriticalOccupancyPenalty(); // Penalty for critical occupancy

        return score;
    }

    public static double calculateOptimality(List<Person> group, int capacity) {
        if (group == null || group.size() < 2) {
            return 0.0;
        }
        if (group.size() > capacity + 1) {
            System.out.println("Group exceeds room capacity: " + group.size() + " > " + capacity);
            return Double.NEGATIVE_INFINITY; // Group exceeds room capacity
        }
        double score = Person.calculatePreferenceScore(group, null);
        score += group.size() * Config.getLargeGroupBonus(); // Bonus for larger groups
        if (group.size() < capacity - 2)
            score -= Config.getUnderOccupancyPenalty() * (capacity - group.size()); // Penalty for under-occupancy
        if (group.size() == capacity + 1)
            score -= Config.getCriticalOccupancyPenalty(); // Penalty for critical occupancy

        return score;
    }
}
