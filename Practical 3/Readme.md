# COS226 Practical 3: TAS/TTAS Locks and Contention

u23533413 Zamokuhle Zwane
(add your student numbers here)

## what's in here

- `SpinLock.java`: interface, added `getTestAndSetCount()` so both locks can report how many times testAndSet() actually got called
- `TASLock.java`: task 1, basic test-and-set lock
- `TTASLock.java`: task 2, optimised test-and-test-and-set lock
- `Main.java`: basic demo, proves mutual exclusion works (counter comes out exact)
- `ContentionExperiment.java`: task 3, runs both locks across increasing thread counts and logs avg time + avg testAndSet() calls

## task 1: TASLock

- just keeps calling testAndSet() in a loop until it returns false
- works fine for correctness
- every single loop iteration is an atomic op, so under contention it spams the bus hard

## task 2: TTASLock

- before trying the atomic testAndSet(), spin on a plain read (`.get()`) first
- plain reads hit your own cache and don't touch the bus
- only bother with the expensive atomic call once the lock actually looks free
- this is "local spinning" from the textbook (herlihy, ch7)
- doesn't fix contention itself, just cuts down how many wasted atomic ops happen while waiting

## task 3: contention experiment

- ran both locks at 2/4/8/16/32 threads
- 5 runs each
- same total workload every config (8,000,000 increments split evenly) so it's a fair comparison

results:

| Implementation | Threads | Avg Time (ms) | Avg testAndSet() calls |
|---|---|---|---|
| TASLock | 2 | 448.00 | 13,723,761.20 |
| TASLock | 4 | 843.20 | 21,479,995.20 |
| TASLock | 8 | 1288.60 | 38,916,781.80 |
| TASLock | 16 | 2172.40 | 66,298,965.00 |
| TASLock | 32 | 3303.00 | 105,380,294.00 |
| TTASLock | 2 | 426.40 | 9,374,583.40 |
| TTASLock | 4 | 846.20 | 11,913,883.40 |
| TTASLock | 8 | 1431.60 | 13,269,179.80 |
| TTASLock | 16 | 2254.00 | 12,638,362.80 |
| TTASLock | 32 | 2822.40 | 11,315,441.60 |

### what this actually shows

- TASLock's testAndSet() count grows almost in step with thread count
 : roughly 8x more calls going from 2 to 32 threads
 : makes sense since every failed attempt is another testAndSet() and more threads means more failed attempts
- TTASLock's testAndSet() count barely moves
 : stays around 9-13 million no matter how many threads you throw at it
 : even at 32 threads it's only 11.3M, way less than TASLock's 105M at the same thread count
 : that's because most of the waiting happens as cheap local reads instead of atomic calls
- execution time still goes up for both as threads increase
 : but TTASLock ends up faster than TASLock at every thread count
 : and the gap gets bigger as threads increase (448ms vs 426ms at 2 threads, but 3303ms vs 2822ms at 32 threads)
- TTAS doesn't remove contention for the critical section itself, it just makes the spinning while you wait way cheaper
- matches what the textbook says:
 : TTAS is better because it does local spinning (reading a cached value, no bus traffic)
 : TASLock hammers the bus every single iteration instead
 : tradeoff: TTAS still gets a "storm" of atomic calls right after the lock is released, since everyone's cache gets invalidated at once and they all try testAndSet() together

## how to run

```
javac *.java
java Main
java ContentionExperiment > results.txt
```
reference
Herlihy, M. and Shavit, N. (2012) The Art of Multiprocessor Programming. Revised 1st edn. Waltham, MA: Morgan Kaufmann, Chapter 7 (Spin Locks and Contention).

## notes to self

- counter in ContentionExperiment is a plain int on purpose, not atomic, so correctness comes entirely from the lock being tested
- 32 threads takes way longer than the rest to run, especially TASLock, since it's the worst case for bus traffic
