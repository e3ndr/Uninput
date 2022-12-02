package xyz.e3ndr.uninput;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import co.casterlabs.commons.async.queue.ExecutionQueue;
import co.casterlabs.commons.async.queue.ThreadExecutionQueue;

public class Downsampler<T> {
    private ExecutionQueue execQueue = new ThreadExecutionQueue();
    private List<T> samples = new ArrayList<>(); // add() & clear() is fast.

    // Our adding is nice and quick. We don't want to potentially
    // lock up the process of emptying. See the next comment.
    public void add(T sample) {
        this.execQueue.execute(() -> {
            this.samples.add(sample);
        });
    }

    // Emptying can lock up adding as much as it wants. Doesn't matter.
    public void empty(Consumer<List<T>> task) {
        this.execQueue.execute(() -> {
            task.accept(this.samples);
            this.samples.clear();
        });
    }

}
