package me.davsennn.algorithm;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

public record Result(List<List<Person>> config, double score, UUID id) implements Comparable<Result> {
    public Result(List<List<Person>> config, double score) {
        this(config, score, UUID.nameUUIDFromBytes(
                        ByteBuffer.allocate(Integer.BYTES)
                        .putInt(config.hashCode()).array()));
    }
    
    @Override
    public String toString() {
        return Result.toString(config());
    }

    @Override
    public int compareTo(Result other) {
        double scorediff = Double.compare(this.score(), other.score());
        if (scorediff == 0) return -this.id().compareTo(other.id());
        else if (scorediff < 0) return -1;
        else return +1;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Result)) return false;
        return this.id().equals(((Result) o).id());
    }

    public static String toString(List<List<Person>> config) {
        return toString(config, ", ");
    }
    public static String toString(List<List<Person>> config, String divider) {
        if (config.isEmpty()) return "[]";
        StringBuilder ret = new StringBuilder("[");
        for (List<Person> list : config) {
            ret.append(toStringList(list)).append(divider);
        }
        return ret.delete(ret.length() - divider.length(), ret.length()).append("]").toString();
    }
    public static String toStringList(List<Person> list) {
        if (list.isEmpty()) return "[]";
        StringBuilder ret = new StringBuilder(String.valueOf(list.size())).append("[");
        for (Person p : list) {
            ret.append(p.getName()).append(", ");
        }
        return ret.delete(ret.length() - 2, ret.length()).append("]").toString();
    }
}
