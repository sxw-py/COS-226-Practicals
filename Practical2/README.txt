COS 226
Practical 2
N-Thread Mutual Exclusion

group members:
Zamokuhle Zwane u23533413

0. reference

both locks in this submission are implemented following the algorithms
as presented in:

Herlihy, M. and Shavit, N. (2012). The Art of Multiprocessor Programming,
Revised Reprint. Morgan Kaufmann. Chapter 2, "Mutual Exclusion".

- filter lock: section 2.4, "The Filter Lock", pages 27-29
- bakery lock: section 2.5, "Fair Locks", pages 29-33 (Lamport's bakery
  algorithm)

1. how the filter lock works

the filter lock generalises the two thread algorithms to n threads by making
each thread climb through n-1 "levels" before it's allowed into the critical
section. think of it like a funnel, level 0 is where everyone starts and
level n-1 is where only one thread is allowed to be at a time.

variables:
- level[i]  : what level thread i is currently sitting at. starts at 0
              (not trying to lock), goes up to n-1 (has the lock)
- victim[l] : for each level l, this holds the thread id of whoever was
              the last one to arrive at that level. it's used to break
              ties, if two threads arrive at the same level at the same
              time, whoever set victim[l] last is the one who has to wait

a thread moves up one level at a time. at each level it sets itself as the
victim, then waits while there's still another thread at that level or
higher AND it is still the victim. as soon as either condition becomes
false (either everyone else has dropped back down, or someone else became
the new victim) the thread can move up to the next level. by the time a
thread reaches level n-1 it's guaranteed to be alone there.

unlocking just means dropping back to level 0.
 
2. how the bakery lock works

the bakery lock is based on the "take a number" system you'd see in an
actual bakery. every thread that wants the lock draws a ticket number
bigger than anyone else's, then waits for its turn based on that number.

variables:
- flag[i]  : true if thread i is currently trying to get the lock (or
             holds it), false otherwise
- label[i] : the ticket number thread i drew. 0 means it hasn't drawn one

to lock, a thread sets its flag to true and takes a label one bigger than
the max label currently out there. then it checks every other thread, if
that thread also has its flag up and has priority (smaller label, or same
label but smaller thread id, which is how ties get broken) it waits.

once it's passed every other thread it's allowed in. unlocking just drops
the flag back to false.

3. comparing the two

both give mutual exclusion for n threads but they go about it differently.

filter lock uses levels and a shared victim variable per level to filter
threads down until only one is left. it doesn't guarantee threads get in
in the order they arrived, so it can technically let a thread starve if
it's unlucky, since anyone can become the "next" victim.

bakery lock uses ticket numbers to give a strict first-come-first-served
ordering (or as close as you can get with concurrent ticket draws, ties
are broken by thread id). this means bakery lock is fair, a thread that
asked first will always get in first, filter lock doesn't promise that.

filter lock is a bit more compact conceptually (levels), bakery lock needs
that extra step of scanning all other threads' labels to compute the max
before it even starts waiting.

4. results

ran on 4 threads, 5 increments each (expected = 20) using Main.java as
provided. both locks produced the correct final counter value every run,
confirming mutual exclusion held, no updates were lost to interleaving.

filter lock run:
Expected: 20
Actual:   20

bakery lock run:
Expected: 20
Actual:   20

worth noting from the console output, threads do not always acquire the
lock strictly in the order they printed "attempting to acquire lock",
this is expected and fine, that print statement happens before the lock
is actually granted, so multiple threads can print that line while they
are all still contending. what matters is that only one "acquired the
lock" line appears at a time and the counter increments cleanly from 1
to 20 with no repeats or skips, which both logs confirm.

we also noticed the bakery lock run showed threads resolving contention
in an order consistent with ticket numbers even when several threads
printed their "attempting" line back to back, which lines up with the
fairness property described in the textbook (a strict ticket ordering,
ties broken by thread id) and is a good difference to point out versus
the filter lock, since the filter lock makes no such fairness guarantee.
 
5. demo flow (what we're planning to show the tutor)

1. explain the shared Counter class and why increment() is not atomic on
   its own (read value, add 1, write back, three separate steps that can
   interleave between threads without a lock)

2. run Main.java, this fires up 4 threads, each doing 5 increments,
   first using FilterLock then BakeryLock

3. point out in the console output that "Expected" and "Actual" match at
   the end of each run (4 threads x 5 increments = 20), showing mutual
   exclusion actually worked, no lost updates

4. walk through FilterLock.java line by line if asked:
   - show the level array and explain the climbing
   - show victim array and explain how it breaks ties
   - explain why level n-1 guarantees exclusivity

5. walk through BakeryLock.java line by line if asked:
   - show flag and label arrays
   - explain taking a ticket (max + 1)
   - explain the smaller() tie-break method
   - explain why this gives fairness that filter lock doesn't

6. be ready to answer questions individually about any part of either
   implementation, since the whole group needs to understand both locks,
   not just whoever wrote which file

6. How to run

javac *.java
java Main

7. files included
 
- FilterLock.java
- BakeryLock.java
- README.textbook
