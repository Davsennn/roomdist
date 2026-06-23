package me.davsennn.algorithm;

import javax.naming.SizeLimitExceededException;
import java.util.*;

public final class PreferenceGroupEngine {

    public static void execute() throws SizeLimitExceededException {
        SearchTask.init();
        buildPairs();
    }

    private static void buildPairs() {
        List<Person> people = Person.getPeople();
        List<List<Person>> preferenceLists = people.stream().map(p -> (List<Person>) new ArrayList<>(List.of(p))).toList();

        System.out.println(Result.toString(preferenceLists));

        preferenceLists = joinLists(preferenceLists, 1,
                new Evaluator<>((p, q) -> {
                    if (Person.prefers(p, q)) {
                        if (Person.prefers(q, p))
                            return 1.5;
                        else
                            return 1;
                    } else {
                        if (Person.prefers(q, p))
                            return 1;
                        else
                            return 0;
                    }
                }));

        System.out.println(Result.toString(preferenceLists));
    }


    private static <T> List<List<T>> joinLists(List<List<T>> lists,
                                               @SuppressWarnings("SameParameterValue") double threshold,
                                               Evaluator<T> evaluator) {
        List<List<T>> joined = new ArrayList<>(lists.size());
        for (List<T> list : lists) {
            joined.add(new ArrayList<>(list));
        }

        boolean changed;
        do {
            changed = false;

            mergeSearch:
            for (int i = 0; i < joined.size(); i++) {
                List<T> a = joined.get(i);
                for (int j = i + 1; j < joined.size(); j++) {
                    List<T> b = joined.get(j);

                    if (evaluator.applyMultilinear(a, b) >= threshold) {
                        for (T c : b)
                            if (!a.contains(c)) a.add(c);
                        joined.remove(j);
                        changed = true;
                        break mergeSearch;
                    }
                }
            }
            changed = joined.removeIf(List::isEmpty) || changed;
        } while (changed);

        return joined;
    }

}
