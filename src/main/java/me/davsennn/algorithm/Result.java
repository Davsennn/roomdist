package me.davsennn.algorithm;

import java.util.List;

public record Result(List<List<Person>> config, double score) implements Comparable<Result> {
    @Override
    public String toString() {
        return Result.toString(config());
    }

    @Override
    public int compareTo(Result other) {
        double scorediff = Double.compare(this.score(), other.score());
        if (scorediff == 0) return 0;
        else if (scorediff < 0) return -1;
        else return +1;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Result)) return false;
        return this.score == ((Result) o).score;
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
