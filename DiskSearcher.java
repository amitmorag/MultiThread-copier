//Amit Morag 208936229

import java.io.File;


public class DiskSearcher {
    public static final int DIRECTORY_QUEUE_CAPACITY = 50; // Capacity of the directories queue
    public static final int RESULTS_QUEUE_CAPACITY = 50;
    public static final int MILESTONES_QUEUE_CAPACITY = 1000;


    public static void main(String[] args) {
        long start = System.currentTimeMillis();//the start time
        if (args.length != 6) { // supposed to be 6 arguments.
            System.err.println("Illegal number of arguments. supposed to be 6.");
            return;
        }
        SynchronizedQueue<File> directoryQueue = new SynchronizedQueue<File>(DIRECTORY_QUEUE_CAPACITY);
        SynchronizedQueue<File> resultQueue = new SynchronizedQueue<File>(RESULTS_QUEUE_CAPACITY);
        SynchronizedQueue<String> milestoneQueue = new SynchronizedQueue<>(MILESTONES_QUEUE_CAPACITY);
        boolean isMilestone;
        if (args[0].equals("true") || args[0].equals("false")) {//accept only true or false
            isMilestone = Boolean.parseBoolean(args[0]);
        } else {
            System.err.println("milestone Flag must be true or false");
            return;
        }
        if (isMilestone) {
            milestoneQueue.registerProducer();
            milestoneQueue.enqueue("General, program has started the search");
            milestoneQueue.unregisterProducer();
        }
        int id = 0;
        String extension = args[1];
        File root = new File(args[2]);
        File destination = new File(args[3]);
        int searchersLen = Integer.parseInt(args[4]);
        int copiersLen = Integer.parseInt(args[5]);
        if (searchersLen <= 0 || copiersLen <= 0) {
            System.err.println("searchers and copiers must be positive");
            return;
        }
        if (!root.isDirectory()) {
            System.err.println("root path is not directory");
            return;
        }
        Scouter scouter = new Scouter(++id, directoryQueue, root, milestoneQueue, isMilestone);//create 1 scouter
        Thread scouterThread = new Thread(scouter);
        scouterThread.start();//first start the scouter thread
        Thread[] searcherArrThreads = new Thread[searchersLen];
        for (int i = 0; i < searcherArrThreads.length; i++) {//creates searchers threads and store them in arrays
            Searcher searcher = new Searcher(++id, extension, directoryQueue, resultQueue, milestoneQueue, isMilestone);
            Thread searcherThread = new Thread(searcher);
            searcherArrThreads[i] = searcherThread;
            searcherArrThreads[i].start();//start each searcher
        }
        Thread[] copierArrThreads = new Thread[copiersLen];
        for (int i = 0; i < copierArrThreads.length; i++) {//creates copiers threads and store them in arrays
            Copier copier = new Copier(++id, destination, resultQueue, milestoneQueue, isMilestone);
            Thread copierThread = new Thread(copier);
            copierArrThreads[i] = copierThread;
            copierArrThreads[i].start();//start each copier
        }
        try {
            scouterThread.join();//wait for scouter to finish
            for (int i = 0; i < searchersLen; i++) {//wait for searchers to finish
                searcherArrThreads[i].join();
            }
            for (int i = 0; i < copiersLen; i++) {//wait for copiers to finish
                copierArrThreads[i].join();
            }
            for (int i = 0; milestoneQueue.getSize() > 0; i++) {//print the milestonesQueue
                System.out.println(i + ": " + milestoneQueue.dequeue());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis() - start;
        System.out.println("Running time is " + end + " milliseconds");
    }
}

