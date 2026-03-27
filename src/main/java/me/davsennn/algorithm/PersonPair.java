package me.davsennn.algorithm;

public record PersonPair(Person a, Person b) implements Comparable<PersonPair> {
    public PersonPair(Person a, Person b) {
        if (a.compareTo(b) < 0) {
            this.a = b;
            this.b = a;
        } else {
            this.a = a;
            this.b = b;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PersonPair(Person a1, Person b1))) return false;
        return (a1 == this.a && b1 == this.b) || (a1 == this.b && b1 == this.a);
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
