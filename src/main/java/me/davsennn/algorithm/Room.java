package me.davsennn.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Room {
    private static List<Room> rooms = new ArrayList<>();
    public static List<Room> getRooms() {
        return rooms;
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


    private String id;
    private int capacity;
    private List<Person> occupants;

    public Room(String id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        this.occupants = new ArrayList<>();
        if (!rooms.contains(this))
            Room.rooms.add(this);
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

    public void addOccupant(Person occupant) {
        occupants.add(occupant);
    }

    public String toString() {
        return this.id + "," + this.capacity;
    }
}
