package me.davsennn.algorithm;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

public class Result {
    public List<List<Person>> config;
    public double score;
    public UUID id;

    public Result(List<List<Person>> config, double score) {
        this.config = config;
        this.score = score;
        this.id = UUID.nameUUIDFromBytes(
                        ByteBuffer.allocate(Integer.BYTES)
                        .putInt(config.hashCode()).array());
    }

    public String toString() {
        return Result.toString(config);
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
