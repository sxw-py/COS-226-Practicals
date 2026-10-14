# COS 226 Assignment 2

Concurrent queues and producer-consumer system.

**Group members (student numbers):**
u23533413 Zamokuhle Zwane
u25072235 Ashley Mthemba
u25114469 Thandolwethu Jantjies

## files

- `BoundedQueue.java`: bounded blocking queue (two locks, size counter, notFull/notEmpty conditions)
- `LockFreeQueue.java`: unbounded lock-free queue (sentinel node, compareAndSet, helping)
- `EmptyException.java`: thrown by the lock-free deq() when the queue is empty
- `Job.java`: a unit of work with a unique id
- `Producer.java`: makes its share of the jobs and enq()s them
- `Consumer.java`: deq()s jobs and counts them until all are processed
- `Main.java`: runs everything and prints the results

## how to run

Compile:

```
javac *.java
```

Run:

```
java Main <bounded|lockfree> <producers> <consumers>
```

e.g. 
`java Main bounded 4 4`,
`java Main lockfree 6 2`,
`java Main bounded 6 2`,
`java Main bounded 2 6`,
`java Main lockfree 6 2`,
`java Main lockfree 2 6`

`for q in bounded lockfree; do for c in "4 4" "6 2" "2 6"; do
  echo "== $q $c"; for i in 1 2 3; do java Main $q $c | grep -E "Time|Throughput"; done
done; done`

 With no arguments it runs the lock-free queue with 4 producers and 4 consumers.

Each run prints the total jobs processed, the execution time and the throughput.

## how it works

Producers split the total jobs between them and enq() them. Consumers deq() jobs and add to a shared AtomicInteger until every job is processed. When the producers are done, main enqueues one null (poison pill) per consumer so consumers stuck on an empty queue can stop. The lock-free deq() throws EmptyException on an empty queue, so the consumer just yields and tries again.

## experiment

Total jobs: 12000 for every run. Bounded queue capacity: 100 for every run. Each row is the average of 3 runs.

| Queue | Producers | Consumers | Avg time (ms) | Avg throughput (jobs/s) |
|---|---|---|---|---|
| Bounded | 4 | 4 | 23.7 | 532,000 |
| Bounded | 6 | 2 | 24.7 | 493,000 |
| Bounded | 2 | 6 | 25.0 | 498,000 |
| Lock-free | 4 | 4 | 25.7 | 471,000 |
| Lock-free | 6 | 2 | 18.0 | 672,000 |
| Lock-free | 2 | 6 | 22.7 | 571,000 |

## observations

- the runs are really short (about 20 ms) and vary a lot between runs, so the differences are small and could be partly noise from JVM warm-up and the 1 ms timer
- lock-free was fastest with 6 producers and 2 consumers; with 4 and 4 it was about the same as the bounded queue
- in the bounded queue, producers fight over enqLock and consumers fight over deqLock, and threads wait on the conditions when the queue is full or empty. every call also does getAndIncrement/getAndDecrement on the shared size field, which can be a bottleneck (Herlihy and Shavit, section 10.3)
- the lock-free queue never blocks; a thread that loses a compareAndSet just retries, and threads help finish each other's tail updates (section 10.5). when there are lots of consumers and the queue is often empty, they spin and yield instead of sleeping

## reference

Herlihy, M. and Shavit, N. (2012). The Art of Multiprocessor Programming, Revised First Edition. Morgan Kaufmann (Elsevier). Chapter 10, sections 10.3 and 10.5.