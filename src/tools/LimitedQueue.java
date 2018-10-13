package tools;

import java.util.LinkedList;

/**
 * Custom implementation of a FIFO Queue with fixed maximum size.
 */
@SuppressWarnings("serial")
public class LimitedQueue<E> extends LinkedList<E> {
    /**
     * Queue size. If a new element is added to a full queue then the first
     * element is automatically removed and the new element is added to the end
     * of the queue.
     */
    private final int limit;

    /**
     * Default constructor which takes the queue limit as parameter.
     *
     * @param limit of the queue
     */
    public LimitedQueue(int limit) {
        this.limit = limit;
    }

    /**
     * If a new element is added to a full queue then the first element is
     * automatically removed and the new element is added to the end of the
     * queue.
     */
    @Override
    public boolean add(E o) {
        super.add(o);
        while (size() > limit) {
            remove();
        }
        return true;
    }

    /**
     * If a new element is added to a full queue then the first element is
     * automatically removed and the new element is added to the end of the
     * queue.
     */
    @Override
    public void add(int pos, E o) {
        super.add(pos, o);
        while (size() > limit) {
            remove();
        }
    }

    /**
     * Returns the maximum queue size.
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Checks if the queue is full.
     *
     * @return {@code true} if the queue is full, {@code false} otherwise
     */
    public boolean isFull() {
        return limit == size();
    }
}