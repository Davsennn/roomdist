package me.davsennn.algorithm;

import java.util.List;

public class Result {
    public List<List<Person>> config;
    public double score;

    public Result(List<List<Person>> config, double score) {
        this.config = config;
        this.score = score;
    }
}
