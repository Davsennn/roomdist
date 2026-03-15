package me.davsennn.algorithm;

import java.util.List;

public class Room {
    private String id;
    private int capacity;
    private List<Person> occupants;

    public Room(String id, int capacity) {
        this.id = id;
        this.capacity = capacity;
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
}
