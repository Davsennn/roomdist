package me.davsennn.algorithm;

import me.davsennn.Config;
import me.davsennn.Main;

import javax.naming.SizeLimitExceededException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.LongAdder;

public record SearchTask(
        List<Person> remaining,
        int start,
        List<Person> group,
        double score
) {
    @Override
    public String toString() {
        return Result.toStringList(group);
    }

    private static Config.PortableConfig config;
    private static final Object resultLock = new Object();

    public static void execute() throws SizeLimitExceededException {
        init();

        List<SearchTask> tasks = buildInitialTasks(3);
        System.out.println(tasks.size());
        System.out.println(tasks);

        ExecutorService executor = Executors.newFixedThreadPool(Main.threads);

        try {
            List<Future<?>> futures = new ArrayList<>();

            for (SearchTask task : tasks) {
                futures.add(executor.submit(() ->
                    buildGroup(
                            0,
                            Room.getRooms().getFirst().capacity(),
                            task.remaining(),
                            task.start(),
                            task.group(),
                            new ArrayList<>(),
                            0
                    )
                ));
            }

            for (Future<?> future : futures)
                future.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e.getCause());
        } finally {
            executor.shutdown();
        }

        finishUp();
    }

    private static List<SearchTask> buildInitialTasks(@SuppressWarnings("SameParameterValue") int startingSize) {
        List<SearchTask> tasks = new ArrayList<>();
        List<Person> people = Person.getPeople();
        int firstRoomCapacity = Room.getRooms().getFirst().capacity();

        if (startingSize < 1) {
            throw new IllegalArgumentException("startingSize must be at least 1");
        }
        if (startingSize > firstRoomCapacity + 1) {
            throw new IllegalArgumentException("startingSize cannot exceed first room capacity + 1");
        }

        collectInitialTasks(people, startingSize, 0, new ArrayList<>(), tasks);

        return tasks;
    }

    private static void collectInitialTasks(List<Person> people,
                                            int targetSize,
                                            int start,
                                            List<Person> group,
                                            List<SearchTask> tasks) {
        int size = group.size();

        if (size == targetSize) {
            tasks.add(new SearchTask(
                    people,
                    start,
                    new ArrayList<>(group),
                    Person.calculatePreferenceScore(group)
            ));
            return;
        }

        int remainingSlots = targetSize - group.size();
        int lastStart = people.size() - remainingSlots;
        for (int i = start; i <= lastStart; ++i) {
            if (config.USE_EARLY_PRUNING() && 0 <= config.EARLY_PRUNING_LENGTH() && size != 0) { // prune early
                if (size <= config.EARLY_PRUNING_STRENGTH()) {
                    tried_prune.increment();
                    boolean cut = true;
                    for (Person q : group)
                        if (Person.prefers(people.get(i), q)) {
                            cut = false;
                            break;
                        }
                    if (cut) { pruned.increment(); continue; }
                }
            }

            group.add(people.get(i));
            collectInitialTasks(people, targetSize, i + 1, group, tasks);
            group.removeLast();
        }
    }

    public static final LongAdder processed = new LongAdder();
    public static final LongAdder pruned = new LongAdder();
    public static final LongAdder tried_prune = new LongAdder();
    public static PriorityQueue<Result> resultPriorityQueue;
    public static volatile double worst_best_score = Double.NEGATIVE_INFINITY;

    public static Result[] results;
    public static long startTime;
    public static long endTime;
    private static int[] minPeopleByRoom;
    private static int[] maxPeopleByRoom;
    private static double[] maxFutureScoreByRoom;

    public static void init() throws SizeLimitExceededException {
        startTime = System.nanoTime();
        processed.reset();
        pruned.reset();
        tried_prune.reset();
        worst_best_score = Double.NEGATIVE_INFINITY;
        Room.finish();
        Person.updateConfig();
        Person.use_custom_bonuses = Person.custom_bonuses != null && !Person.custom_bonuses.isEmpty();
        config = new Config.PortableConfig();
        Person.prepareScoring();
        buildOccupancyBounds();
        buildOptimisticScoreBounds();
        resultPriorityQueue = new PriorityQueue<>(11);
    }

    private static void finishUp() {
        endTime = System.nanoTime();
        long processedNow = processed.sum();
        System.out.printf("Execution stopped. %1$,d Paths processed, %2$,dms elapsed + %3$,dns (%4$,d Paths/sec)%n",
                processedNow, (endTime - startTime)/1000000L, (endTime - startTime)%1000000L, (processedNow*1000000000L/(endTime - startTime)));
        System.out.println("TOP 10 (" + resultPriorityQueue.size() + ")");
        results = resultPriorityQueue.toArray(new Result[0]);
        Arrays.sort(results);
        for (int i = results.length - 1; i >= 0; --i) {
            System.out.println(String.format("%+5.4g", results[i].score()) + " | " + results[i]);
        }
    }

    private static void buildOccupancyBounds() {
        List<Room> rooms = Room.getRooms();
        minPeopleByRoom = new int[rooms.size() + 1];
        maxPeopleByRoom = new int[rooms.size() + 1];
        for (int i = rooms.size() - 1; i >= 0; --i) {
            Room room = rooms.get(i);
            minPeopleByRoom[i] = minPeopleByRoom[i + 1] + Math.max(0, room.capacity() - 1);
            maxPeopleByRoom[i] = maxPeopleByRoom[i + 1] + room.capacity() + 1;
        }
    }

    private static void buildOptimisticScoreBounds() {
        double maxDirectedPairScore = Math.max(
                Math.max(config.MUTUAL_PREFERENCE_BONUS(), config.PREFERENCE_BONUS()),
                -config.NON_PREFERENCE_PENALTY()
        );
        if (Person.use_custom_bonuses) {
            for (double customScore : Person.custom_bonuses.values()) {
                if (customScore != Double.NEGATIVE_INFINITY) {
                    maxDirectedPairScore += Math.max(0.0, customScore / 2);
                }
            }
        }

        List<Room> rooms = Room.getRooms();
        maxFutureScoreByRoom = new double[rooms.size() + 1];
        for (int i = rooms.size() - 1; i >= 0; --i) {
            Room room = rooms.get(i);
            double roomBound = 0.0;
            for (int size = Math.max(2, room.capacity() - 1); size <= room.capacity() + 1; ++size) {
                double sizeBound = size * maxDirectedPairScore;
                if (size == room.capacity() + 1) {
                    sizeBound -= config.CRITICAL_OCCUPANCY_PENALTY();
                }
                roomBound = Math.max(roomBound, sizeBound);
            }
            maxFutureScoreByRoom[i] = maxFutureScoreByRoom[i + 1] + roomBound;
        }
    }

    private static void assignRoom(int roomIdx,
                                   List<Person> remaining,
                                   List<List<Person>> current,
                                   double currentScore) {

        if (remaining.isEmpty()) {
            submitResult(new ArrayList<>(current), currentScore);
            return;
        }

        if (currentScore == Double.NEGATIVE_INFINITY) return;

        if (roomIdx == Room.getRooms().size()) return;

        int remainingSize = remaining.size();
        if (remainingSize < minPeopleByRoom[roomIdx] || remainingSize > maxPeopleByRoom[roomIdx]) {
            pruned.increment();
            return;
        }
        if (currentScore + maxFutureScoreByRoom[roomIdx] <= worst_best_score) {
            pruned.increment();
            return;
        }

        Room room = Room.getRooms().get(roomIdx);

        // build this room's group
        buildGroup(roomIdx, room.capacity(), remaining, 0,
                new ArrayList<>(), current, currentScore);
    }

    private record Candidate(int index, double score) {}

    @Deprecated
    private static void buildGroup(int roomIdx,
                                   int room,
                                   List<Person> remaining,
                                   int start,
                                   List<Person> group,
                                   List<List<Person>> current,
                                   double currentScore) {

        int size = group.size();


        // --- IF GROUP SIZE IS USABLE ---
        int remainingAfterThisRoom = remaining.size() - size;
        boolean canFinishAfterThisRoom = remainingAfterThisRoom == 0 ||
                roomIdx + 1 < minPeopleByRoom.length &&
                        remainingAfterThisRoom >= minPeopleByRoom[roomIdx + 1] &&
                        remainingAfterThisRoom <= maxPeopleByRoom[roomIdx + 1];

        if (size >= room - 1 &&
                size <= room + 1 &&
                canFinishAfterThisRoom) {

            double fullScore = Room.calculateOptimality(group, room);
            double newScore = currentScore + fullScore;
            if (newScore + maxFutureScoreByRoom[roomIdx + 1] <= worst_best_score) {
                pruned.increment();
            } else {
                // recurse to next room
                current.add(new ArrayList<>(group));

                List<Person> newRemaining = new ArrayList<>(remaining.size() - group.size());
                int groupIdx = 0;
                int groupSize = group.size();
                for (Person p : remaining) {
                    if (groupIdx < groupSize && p == group.get(groupIdx)) {
                        ++groupIdx;
                    } else {
                        newRemaining.add(p);
                    }
                }
                processed.increment();
                long processedNow = processed.sum();
                if (processedNow % 10_000_000 == 0) {
                    synchronized (resultLock) {
                        Result best = resultPriorityQueue.peek();
                        if (best != null) {
                            long elapsed = System.nanoTime() - startTime;
                            System.out.printf("%1$,4dM | %4$,4d | %5$,4d | %6$,4ds | %7$,4d ns/p | %3$4.4f | %2$s %n", processedNow / 1000000, best, best.score(), pruned.sum(), tried_prune.sum(), elapsed / 1_000_000_000, elapsed / processedNow);
                        } else {
                            System.out.println(processedNow / 1000000 + "|" + pruned + "|" + tried_prune);
                        }
                    }
                }
                assignRoom(roomIdx + 1, newRemaining, current, newScore);

                current.removeLast();
            }
        }

        // --- STOP IF TOO BIG ---
        if (size >= room + 1) return;

        // --- EXTEND GROUP ---
        int remainingsize = remaining.size();
        List<Candidate> candidates = new ArrayList<>(remainingsize);
        for (int i = start; i < remainingsize; i++) {
            Person p = remaining.get(i);

            if (config.USE_EARLY_PRUNING() && roomIdx <= config.EARLY_PRUNING_LENGTH() && size != 0) { // prune early
                if (size <= config.EARLY_PRUNING_STRENGTH()) {
                    tried_prune.increment();
                    boolean cut = true;
                    for (Person q : group)
                        if (Person.prefers(p, q)) {
                            cut = false;
                            break;
                        }
                    if (cut) { pruned.increment(); continue; }
                }
                double estimate = remainingsize * 12 + currentScore;
                if (estimate < worst_best_score || estimate < 0) { pruned.increment(); return; }
            }

            double score = Person.branchOrderScore(group, p);
            candidates.add(new Candidate(i, score));
        }
        candidates.sort(Comparator.comparingDouble(Candidate::score).reversed());
        for (Candidate candidate : candidates) {
            group.add(remaining.get(candidate.index()));
            buildGroup(roomIdx, room, remaining, candidate.index() + 1, group, current, currentScore);
            group.removeLast();
        }
    }

    private static void submitResult(List<List<Person>> current, double score) {
        synchronized (resultLock) {
            if (score <= worst_best_score) return;

            resultPriorityQueue.add(new Result(current, score));

            if (resultPriorityQueue.size() >= 11) {
                resultPriorityQueue.remove();
            }

            Result worstBest = resultPriorityQueue.peek();
            if (worstBest != null) {
                worst_best_score = worstBest.score();
            }
        }
    }
}
