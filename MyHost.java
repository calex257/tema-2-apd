/* Implement this class. */

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class MyHost extends Host {

    PriorityBlockingQueue<Task> tasks;
    volatile boolean running;

    public MyHost() {
        tasks = new PriorityBlockingQueue<>(100);
    }

    @Override
    public void run() {
        // task-urile se sorteaza descrescator dupa prioritate si
        // crescator dupa timpul de start
        tasks = new PriorityBlockingQueue<>(100, (Task t1, Task t2) -> {
            int prioDiff = t2.getPriority() - t1.getPriority();
            if (prioDiff == 0) {
                return t1.getStart() - t2.getStart();
            }
            return prioDiff;
        });
        running = true;
        long remainder = 0;
        while (running) {
            Task t;
            // aici este task-ul priortar
            t = tasks.peek();
            
            if (t == null) {
                continue;
            }
            if (!t.isPreemptible()) {
                remainder = 0;
                long start;
                long end;
                // simulez sleep-ul folosind nanoTime
                // asta a fost ideea mea initiala, stiam 
                // ca pot folosi sleep dar am decis sa fac
                // asa for some reason(un thread de pe
                // stack overflow a zis ca asta e mai precis
                // decat currentTimeMillis si mi-am zis ca daca
                // tot fac busy waiting practic might as well
                // fully commit to it).
                while (t.getLeft() > 0) {
                    start = System.nanoTime();
                    end = System.nanoTime();
                    while (end - start + remainder < 1000000) {
                        end = System.nanoTime();
                    }
                    remainder = end - start - 1000000;
                    t.setLeft(t.getLeft() - 1);    
                }
                t.finish();
                // nu folosesc poll pentru ca este posibil ca varful
                // cozii sa se fi schimbat intre timp
                tasks.remove(t);
                
                continue;
            }
            // aici este logica pentru rularea unui task preemptabil
            long start = System.nanoTime();
            long end = System.nanoTime();
            if (t.getLeft() < 0) {
                // acelasi rationament ca mai sus
                tasks.remove(t);
                t.finish();
            }
            // practic se asteapta cate cel putin o milisecunda
            // pentru a se putea scadea din left-ul taskului
            while (end - start + remainder < 1000000) {
                end = System.nanoTime();
            }
            remainder = end - start - 1000000;
            t.setLeft(t.getLeft() - 1);
        }
    }

    @Override
    public void addTask(Task task) {
        tasks.add(task);
    }

    @Override
    public int getQueueSize() {
        return tasks.size();
    }

    @Override
    public synchronized long getWorkLeft() {
        long totalDuration = 0;
        for (Task t : tasks) {
            totalDuration += t.getLeft();
        }
        return totalDuration;
    }

    @Override
    public void shutdown() {
        running = false;
    }
}
