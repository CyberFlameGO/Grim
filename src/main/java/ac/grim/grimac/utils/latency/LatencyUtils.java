package ac.grim.grimac.utils.latency;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.data.Pair;

import java.util.LinkedList;
import java.util.ListIterator;

public class LatencyUtils {
    private final LinkedList<Pair<Integer, Runnable>> transactionMap = new LinkedList<>();
    private final GrimPlayer player;

    public LatencyUtils(GrimPlayer player) {
        this.player = player;
    }

    public void addRealTimeTask(int transaction, Runnable runnable) {
        if (player.lastTransactionReceived.get() >= transaction) { // If the player already responded to this transaction
            runnable.run();
            return;
        }
        synchronized (this) {
            transactionMap.add(new Pair<>(transaction, runnable));
        }
    }

    public void handleNettySyncTransaction(int transaction) {
        synchronized (this) {
            for (ListIterator<Pair<Integer, Runnable>> iterator = transactionMap.listIterator(); iterator.hasNext(); ) {
                Pair<Integer, Runnable> pair = iterator.next();

                // We are at most a tick ahead when running tasks based on transactions, meaning this is too far
                if (transaction + 1 < pair.getFirst())
                    return;

                // This is at most tick ahead of what we want
                if (transaction == pair.getFirst() - 1)
                    continue;

                // Run the task
                pair.getSecond().run();
                // We ran a task, remove it from the list
                iterator.remove();
            }
        }
    }
}
