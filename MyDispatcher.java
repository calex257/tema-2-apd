/* Implement this class. */

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


// strategia de selectare a host-ului, folosind design pattern-ul
// strategy
interface SchedulingStrategy {
    Host getHost(Task t);
}

class RoundRobinStrategy implements SchedulingStrategy {

    Dispatcher dispatcher;
    // am folosit un atomic integer pentru a nu avea probleme
    // de sincronizare
    AtomicInteger lastIndex = new AtomicInteger(0);

    public RoundRobinStrategy(Dispatcher d) {
        dispatcher = d;
    }

    @Override
    public Host getHost(Task t) {
        int noHosts = dispatcher.hosts.size();
        // selectez circular cate un host la care sa dau un task
        Host selectedHost = dispatcher.hosts.get(lastIndex.get());
        lastIndex.set((lastIndex.addAndGet(1)) % noHosts);
        return selectedHost;
    }

}

class ShortestQueueStrategy implements SchedulingStrategy {

    Dispatcher dispatcher;

    public ShortestQueueStrategy(Dispatcher d) {
        dispatcher = d;
    }

    @Override
    public Host getHost(Task t) {
        Host min = dispatcher.hosts.get(0);
        // selectez host-ul cu coada cea mai mica
        for (Host h : dispatcher.hosts) {
            if (h.getQueueSize() < min.getQueueSize()) {
                min = h;
            }
        }
        return min;
    }

}

class SITAStrategy implements SchedulingStrategy {

    Dispatcher dispatcher;

    public SITAStrategy(Dispatcher d) {
        dispatcher = d;
    }

    @Override
    public Host getHost(Task t) {
        // asignez fiecarui host un tip de task pe care
        // sa il primeasca
        switch (t.getType()) {
            case SHORT:
                return dispatcher.hosts.get(0);
            case MEDIUM:
                return dispatcher.hosts.get(1);
            case LONG:
                return dispatcher.hosts.get(2);
        }
        return null;
    }

}

class LeastWorkLeftStrategy implements SchedulingStrategy {

    Dispatcher dispatcher;

    public LeastWorkLeftStrategy(Dispatcher d) {
        dispatcher = d;
    }

    @Override
    public Host getHost(Task t) {
        // parcurg lista de hosts si il iau pe cel cu munca ramasa cea
        // mai putina
        Host min = dispatcher.hosts.get(0);
        for (Host h : dispatcher.hosts) {
            if (h.getWorkLeft() < min.getWorkLeft()) {
                min = h;
            }
        }
        return min;
    }

}

public class MyDispatcher extends Dispatcher {

    private SchedulingStrategy strategy;

    public MyDispatcher(SchedulingAlgorithm algorithm, List<Host> hosts) {
        super(algorithm, hosts);
        // in functie de algoritm se alege strategia de folosit
        switch(algorithm) {
            case ROUND_ROBIN:
            strategy = new RoundRobinStrategy(this);
            break;

            case SHORTEST_QUEUE:
            strategy = new ShortestQueueStrategy(this);
            break;

            case SIZE_INTERVAL_TASK_ASSIGNMENT:
            strategy = new SITAStrategy(this);
            break;

            case LEAST_WORK_LEFT:
            strategy = new LeastWorkLeftStrategy(this);
            break;
        }
    }

    @Override
    public void addTask(Task task) {
        // se ia host-ul conform stategia si i se da task-ul
        Host h = strategy.getHost(task);
        h.addTask(task);
    }
}
