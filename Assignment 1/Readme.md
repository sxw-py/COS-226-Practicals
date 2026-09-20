# COS 226 Assignment 1, Task 3, Experimental Investigation
## TTAS, CLH and MCS under increasing contention

Group: 

u23533413 Zamokuhle Zwane

u25072235 Ashley Mthemba

u25114469 Thandolwethu Jantjies


Textbook ref: Herlihy and Shavit, The Art of Multiprocessor Programming, Revised 1st Ed,
Morgan Kaufmann 2012, Chapter 7, Spin Locks and Contention. TTAS in Section 7.3 (Fig. 7.4),
CLH queue lock in Section 7.5.2 (Figs. 7.10-7.11), MCS queue lock in Section 7.5.3.

---

### 1. Setup

Run with `java Experiment 5000 3` on a teammate's machine.

| Aspect | Setting |
|---|---|
| Locks | TTAS, CLH, MCS |
| Thread counts | 2, 4, 8, 16 |
| Iterations per thread | 5000, constant across every lock and thread count |
| Repeats per config | 3, averaged below |
| Reported CPU count | 4 (`Runtime.getRuntime().availableProcessors()`) |
| JVM | 17.0.20 |

Correctness was verified on every single run: `totalBids` always came out exactly equal to
`threads * iterations`, meaning no bid was ever lost to a race, which is only possible if all
three locks provided proper mutual exclusion over the read-compute-write critical section in
`Runner.bidder()`.

### 2. Table 1, averaged results per lock and thread count

| Lock | Threads | Exec time (ms) | Total bids | Final highest bid | Avg wait (us) | Max wait (us) |
|---|---|---|---|---|---|---|
| TTAS | 2 | 3.849 | 10000 | 60313.61 | 0.387 | 80.900 |
| CLH | 2 | 4.277 | 10000 | 59962.99 | 0.394 | 38.367 |
| MCS | 2 | 3.319 | 10000 | 60027.54 | 0.360 | 20.367 |
| TTAS | 4 | 4.160 | 20000 | 119958.86 | 0.535 | 156.533 |
| CLH | 4 | 4.249 | 20000 | 120258.83 | 0.617 | 48.800 |
| MCS | 4 | 4.673 | 20000 | 119449.75 | 0.729 | 50.367 |
| TTAS | 8 | 7.560 | 40000 | 239699.54 | 1.086 | 500.433 |
| CLH | 8 | 12.224 | 40000 | 239648.50 | 2.115 | 71.167 |
| MCS | 8 | 8.951 | 40000 | 240394.03 | 1.542 | 92.667 |
| TTAS | 16 | 14.373 | 80000 | 480036.80 | 2.060 | 336.300 |
| CLH | 16 | 174.094 | 80000 | 479738.69 | 34.042 | 292.767 |
| MCS | 16 | 171.119 | 80000 | 479941.28 | 33.580 | 801.233 |

Raw per-run numbers are in `results.csv`.

### 3. A note on the fairness measurement

The `bidsWon` count came back identical across every thread in every single run (e.g. every
2-thread config gave `[5000, 5000]`, every 16-thread config gave sixteen 5000s). At first that
looked like a great fairness result for all three locks, but it's actually a flaw in how the
metric was defined, not a real finding. Every bid submitted is `currentHighest + 1 + random`, so
it is mathematically guaranteed to raise the highest bid every single time. That means every
bidder "wins" on every one of its own iterations, by construction, regardless of which lock is
used or how contended it is. `bidsWon` was always going to equal `iterations` for every thread,
so a fairness gap of 0 across the board tells us nothing about the locks.

The measurement that does show a real difference is the spread in max wait time across repeated
runs of the same config. For MCS at 16 threads, one run had a max wait of 204.6 us while another
had 1443.8 us, a roughly 7x difference for the same workload. That variance is the actual sign of
unfairness/inconsistency, not the bidsWon count. This is flagged here rather than hidden, since
being upfront about a flawed metric in the report is better than presenting a fairness result
that doesn't mean anything.

### 4. Analysis

**Low contention (2 to 4 threads).** All three locks perform almost identically, a few
milliseconds difference at most. This matches the textbook's expectation: with only two or four
threads spread across four cores, none of the locks are under real pressure yet, so the extra
bookkeeping CLH and MCS do on every acquisition (allocating/managing a queue node) barely shows
up against TTAS's simpler compare-and-swap.

**8 threads.** CLH's execution time (12.2ms) is noticeably worse than TTAS (7.6ms) and MCS
(9.0ms) at this point, and CLH's average wait time (2.1us) is roughly double TTAS's (1.1us). This
is where CLH's queue overhead starts to cost more than it saves, since with 8 threads on 4 cores
there is already some oversubscription starting.

**16 threads, the interesting result.** TTAS finishes in 14.4ms. CLH takes 174ms and MCS takes
171ms, over 10 times slower. This is not what the textbook chapter would predict for a
cache-coherent machine with light contention, and it is not simply "TTAS is the better lock." The
tested machine reported only 4 available processors, so running 16 threads means each core is
juggling 4 threads at once. TTAS has no ordering guarantee at all, any waiting thread that
happens to be scheduled can grab the lock the instant it is free. CLH and MCS enforce strict FIFO
order (Section 7.5.2 to 7.5.3), so if the thread currently at the front of the queue gets
preempted by the OS scheduler mid critical-section (or, for MCS, mid-way through linking itself
into the queue), every other thread queued behind it is stuck waiting, since none of them are
allowed to skip ahead. That single stall can cascade through the whole queue. TTAS has no queue to
get stuck behind, so a preempted thread simply gets skipped by whichever other thread wins the
next compare-and-swap.

In other words, the 16-thread row is measuring oversubscription (16 threads on 4 logical
processors) as much as it is measuring contention, and the two queue locks are the ones that pay
for it, because FIFO ordering is exactly what turns "one thread got preempted" into "everyone
behind it stalls too."

### 5. Conclusion

Under light contention (2 to 4 threads on 4 cores) all three locks are practically
interchangeable. As contention rises but stays within the core count, TTAS's simplicity keeps it
cheap, while CLH's extra bookkeeping starts to show a real cost. Once thread count exceeds the
core count (16 threads on 4 cores), the FIFO fairness that is normally CLH and MCS's advantage
becomes a liability, since a single preempted thread at the head of the queue stalls every thread
behind it, while TTAS's lack of ordering guarantees lets it route around a stalled thread. The
practical lesson is that a queue lock's fairness guarantee is only free when there are enough
cores to keep the queue actually moving, this is exactly the kind of trade-off Section 1 of the
spec asks us to look for, rather than just picking whichever lock finished fastest.

