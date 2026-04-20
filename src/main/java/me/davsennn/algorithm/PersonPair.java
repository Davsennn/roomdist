package me.davsennn.algorithm;

public record PersonPair(Person a, Person b) implements Comparable<PersonPair> {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PersonPair(Person a1, Person b1))) return false;
        return (a1.equals(this.a) && b1.equals(this.b)) || (a1.equals(this.b) && b1.equals(this.a));
    }

    @Override
    public String toString() {
        return a.getName() + "|" + b.getName();
    }

    @Override
    public int compareTo(PersonPair other) {
        return a.compareTo(other.a) + b.compareTo(other.b);
    }

    @Override
    public int hashCode() {
        return 31 * a.hashCode() + b.hashCode();
    }
}
