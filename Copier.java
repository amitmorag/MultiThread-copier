//Amit Morag 208936229


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Copier implements Runnable {
    int id;
    File destination;
    SynchronizedQueue<File> resultsQueue;
    SynchronizedQueue<String> milestonesQueue;
    boolean isMilestones;
    static final int COPY_BUFFER_SIZE = 4096;

    public Copier(int id, File destination, SynchronizedQueue<File> resultsQueue, SynchronizedQueue<String> milestonesQueue, boolean isMilestones) {
        this.id = id;
        this.destination = destination;
        this.resultsQueue = resultsQueue;
        this.milestonesQueue = milestonesQueue;
        this.isMilestones = isMilestones;
    }


    @Override
    public void run() {
        File curFile;
        this.destination.mkdir();//make new directory
        try {
            while ((curFile = this.resultsQueue.dequeue()) != null) {//for each file in the result queue
                File copyFile = new File(destination, curFile.getName());//create the copy
                Files.copy(curFile.toPath(), copyFile.toPath(), StandardCopyOption.REPLACE_EXISTING);//copy content
                if (isMilestones) {
                    milestonesQueue.registerProducer();
                    milestonesQueue.enqueue("Copier from thread id " + id + ": file named " + curFile.getName() + " was found");
                    milestonesQueue.unregisterProducer();
                }
            }
        } catch (NullPointerException | IOException | InterruptedException e) {
            e.getStackTrace();
        }

    }
}

