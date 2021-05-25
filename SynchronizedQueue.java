/** Amit Morag 208936229
 * A synchronized bounded-size queue for multithreaded producer-consumer applications.
 *
 * @param <T> Type of data items
 */
public class SynchronizedQueue<T> {

    private T[] buffer;
    private int producers;
    private int first;//index of first item
    private int size;//number of elements in the queue
    private int last;//index of last item

    /**
     * Constructor. Allocates a buffer (an array) with the given capacity and
     * resets pointers and counters.
     *
     * @param capacity Buffer capacity
     */
    @SuppressWarnings("unchecked")
    public SynchronizedQueue(int capacity) {
        this.buffer = (T[]) (new Object[capacity]);
        this.producers = 0;
        this.first = 0;
        this.size = 0;
        this.last = 0;
    }

    /**
     * Dequeues the first item from the queue and returns it.
     * If the queue is empty but producers are still registered to this queue,
     * this method blocks until some item is available.
     * If the queue is empty and no more items are planned to be added to this
     * queue (because no producers are registered), this method returns null.
     *
     * @return The first item, or null if there are no more items
     * @see #registerProducer()
     * @see #unregisterProducer()
     */
    public T dequeue() throws InterruptedException {
        synchronized (this) {
            while (this.getSize() == 0 && this.producers > 0) {//while there is no item in list but it will be in the future
                try {
                    this.notifyAll();
                    this.wait();//block the thread
                } catch (InterruptedException e) {
                    System.out.println("Interrupt while dequeuing");
                    return null;
                }
            }
            if (this.getSize() == 0 && this.producers == 0) {//if there are no available items to dequeue
                this.notifyAll();
                return null;
            }
            T item = this.buffer[first];//take the first item
            this.buffer[first] = null;
            this.size--;
            this.first++;
            if (this.first == this.getCapacity())//cyclic arr
                first = 0;
            this.notifyAll();
            return item;
        }
    }

    /**
     * Enqueues an item to the end of this queue. If the queue is full, this
     * method blocks until some space becomes available.
     *
     * @param item Item to enqueue
     */
    public void enqueue(T item) {
        synchronized (this) {
            while (this.size == this.getCapacity()) {//if its full
                try {
                    this.notifyAll();
                    this.wait();//block the thread
                } catch (InterruptedException e) {
                    System.out.println("Interrupt while enqueuing");
                    return;
                }
            }
            this.buffer[last] = item;//enter the item in the last place
            last++;
            if (last == this.getCapacity())//cyclic
                last = 0;
            this.size++;
            this.notifyAll();
        }
    }


    /**
     * Returns the capacity of this queue
     *
     * @return queue capacity
     */
    public int getCapacity() {
        return this.buffer.length;
    }

    /**
     * Returns the current size of the queue (number of elements in it)
     *
     * @return queue size
     */
    public int getSize() {
        return this.size;
    }

    /**
     * Registers a producer to this queue. This method actually increases the
     * internal producers counter of this queue by 1. This counter is used to
     * determine whether the queue is still active and to avoid blocking of
     * consumer threads that try to dequeue elements from an empty queue, when
     * no producer is expected to add any more items.
     * Every producer of this queue must call this method before starting to
     * enqueue items, and must also call <see>{@link #unregisterProducer()}</see> when
     * finishes to enqueue all items.
     *
     * @see #dequeue()
     * @see #unregisterProducer()
     */
    public void registerProducer() {
        synchronized (this) {
            this.producers++;
        }
    }

    /**
     * Unregisters a producer from this queue. See <see>{@link #registerProducer()}</see>.
     *
     * @see #dequeue()
     * @see #registerProducer()
     */
    public void unregisterProducer() {
        synchronized (this) {
            this.producers--;
        }
    }
}
