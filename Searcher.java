//Amit Morag 208936229

import java.io.File;

public class Searcher implements Runnable {

    int id;
    String extension;
    SynchronizedQueue<File> directoryQueue;
    SynchronizedQueue<File> resultsQueue;
    SynchronizedQueue<String> milestonesQueue;
    boolean isMilestones;

    public Searcher(int id, String extension, SynchronizedQueue<File> directoryQueue, SynchronizedQueue<File> resultsQueue, SynchronizedQueue<String> milestonesQueue, boolean isMilestones) {
        this.id = id;
        this.extension = extension;
        this.directoryQueue = directoryQueue;
        this.resultsQueue = resultsQueue;
        this.milestonesQueue = milestonesQueue;
        this.isMilestones = isMilestones;
    }

    /**
     * Runs the searcher thread. Thread will fetch a directory to search in from the directory queue, then search all files inside it (but will not recursively search subdirectories!).
     * Files that have the wanted extension are enqueued to the results queue. This method begins by registering to the results queue as a producer and when finishes, it unregisters from it.
     * If the isMilestones was set in the constructor (and therefore the milstonesQueue was sent to it as well, it should write every "important" action to this queue.
     */
    @Override
    public void run() {
        this.resultsQueue.registerProducer();
        File curDir;

        // While the directory queue is not empty
        try {
            while ((curDir = this.directoryQueue.dequeue()) != null) {
                //put all the files inside the root in list
                File[] lst = curDir.listFiles();
                for (File file : lst) {
                    // If the file contain the pattern
                    if (file.getName().endsWith(extension)) {
                        // Enqueue the file to resultsQueue
                        this.resultsQueue.enqueue(file);
                        if (isMilestones) {
                            milestonesQueue.registerProducer();
                            milestonesQueue.enqueue("Searcher on thread id " + id + ": file named " + file.getName() + " was found");
                            milestonesQueue.unregisterProducer();
                        }
                    }
                }
            }
        } catch (NullPointerException | InterruptedException e) {
            e.getStackTrace();
        }
        this.resultsQueue.unregisterProducer();
    }
}

