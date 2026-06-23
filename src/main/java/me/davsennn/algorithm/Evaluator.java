package me.davsennn.algorithm;

import java.util.List;
import java.util.function.*;

public class Evaluator<T> implements ToDoubleBiFunction<T, T>, BiFunction<T, T, Double> {
    private final ToDoubleBiFunction<T, T> evaluator;

    public Evaluator(ToDoubleBiFunction<T, T> evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public double applyAsDouble(T t, T t2) {
        return evaluator.applyAsDouble(t, t2);
    }

    public double applyMultilinear(List<T> ts, List<T> t2s) {
        double score = 0;
        for (T t : t2s)
            score += applyLinear(ts, t);
        return score;
    }

    public double applyLinear(List<T> ts, T t2) {
        double score = 0;
        for (T t : ts)
            score += applyAsDouble(t, t2);
        return score;
    }

    @Override
    public Double apply(T t, T t2) {
        return applyAsDouble(t, t2);
    }

    @Override
    public <V> BiFunction<T, T, V> andThen(Function<? super Double, ? extends V> after) {
        return BiFunction.super.andThen(after);
    }
}
