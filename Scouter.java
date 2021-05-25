//Amit Morag 208936229

import java.io.File;

public class Scouter implements Runnable {
    int id;//the id of the thread running the instance
    SynchronizedQueue<File> directoryQueue;//queue for directories to be searched
    File root;//Root directory to start from
    SynchronizedQueue<String> milestonesQueue;//a synchronizedQueue to write milestones to
    boolean isMilestones;//indicating whether or not the running thread should write to the milestonesQueue

    public Scouter(int id, SynchronizedQueue<File> directoryQueue, File root, SynchronizedQueue<String> milestonesQueue, boolean isMilestones) {
        this.id = id;
        this.directoryQueue = directoryQueue;
        this.root = root;
        this.milestonesQueue = milestonesQueue;
        this.isMilestones = isMilestones;
    }

    /**
     * Starts the scouter thread. Lists directories under root directory and adds them to queue,
     * then lists directories in the next level and enqueues them and so on. This method begins by registering to the directory queue as a producer and when finishes,
     * it unregisters from it. If the isMilestones was set in the constructor (and therefore the milstonesQueue was sent to it as well, it should write every "important" action to this queue.
     */
    public void run() {
        this.directoryQueue.registerProducer();
        // If the root is a directory
        if (this.root.isDirectory()) {
            // Enqueue the root
            this.directoryQueue.enqueue(this.root);
            if (isMilestones) {
                milestonesQueue.registerProducer();
                milestonesQueue.enqueue("Scouter on thread id " + this.id + ": directory named " + root.getName() + " was scouted");
                milestonesQueue.unregisterProducer();
            }
        }
        ListAllSubDir(this.root);
        this.directoryQueue.unregisterProducer();
    }

    /**
     * recursively put in the directory queue all the subdirectories of the root.
     * @param root - File
     */

    public void ListAllSubDir(File root) {
        if (root.isDirectory()) {
            //put all the files inside the root in list
            File[] lst = root.listFiles();
            if (lst != null) {
                for (File file : lst) {
                    //for all the files inside the list
                    if (file.isDirectory()) {
                        // if it is directory, add to the queue and continue recursively
                        this.directoryQueue.enqueue(file);
                        if (isMilestones) {
                            milestonesQueue.registerProducer();
                            milestonesQueue.enqueue("Scouter on thread id " + id + ": directory named " + file.getName() + " was scouted");
                            milestonesQueue.unregisterProducer();
                            ListAllSubDir(file);
                        }
                    }
                }
            } else {
                throw new NullPointerException("The array " + root + " is null");
            }
        } else {
            throw new IllegalArgumentException("Illegal path: " + root);
        }
    }

}  