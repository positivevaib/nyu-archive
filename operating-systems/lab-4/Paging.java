import java.io.*;
import java.util.*;

// Define main program class
public class Paging {
    // Declare (and initialize/instantiate) class variables
    // Command line arguments
    static int M, P, S, J, N;
    static String R; 

    // Round-robin scheduling quantum and random numbers array
    static int quantum = 3;
    static ArrayList<Integer> randNums = new ArrayList<>();

    // Processes array
    static Process[] processes;

    // Frame table and frame queues for fifo and lru
    static ArrayList<Frame> frames = new ArrayList<>();
    static ArrayList<Frame> framesAddQueue = new ArrayList<>();
    static ArrayList<Frame> framesUseQueue = new ArrayList<>();

    // Define main method
    public static void main(String[] args) {
        // Check command line arguments
        if (args.length != 6) {
            System.out.println("Six command line arguments required.");
            System.exit(1);
        }

        // Parse command line arguments
        Paging.M = Integer.parseInt(args[0]);
        Paging.P = Integer.parseInt(args[1]);
        Paging.S = Integer.parseInt(args[2]);
        Paging.J = Integer.parseInt(args[3]);
        Paging.N = Integer.parseInt(args[4]);
        Paging.R = args[5];

        // Read random-numbers file
        try {
            Scanner fileIn = new Scanner(new File("random-numbers.txt"));

            while (fileIn.hasNext())
                Paging.randNums.add(fileIn.nextInt());

            fileIn.close();
        }
        catch(FileNotFoundException e) {
            System.out.println("'random-numbers.txt' file not found.");
            System.exit(1);
        }

        // Instantiate frame table
        for (int i = 0; i < (Paging.M/Paging.P); i++)
            Paging.frames.add(null);

        // Instantiate processes according to job mix
        switch (Paging.J) {
            case 1: Paging.processes = new Process[1];
                    Paging.processes[0] = new Process(1.000, 0.000, 0.000, 1, Paging.S);
                    break;
            case 2: Paging.processes = new Process[4];
                    for (int i = 0; i < Paging.processes.length; i++)
                        Paging.processes[i] = new Process(1.000, 0.000, 0.000, i + 1, Paging.S);
                    break;
            case 3: Paging.processes = new Process[4];
                    for (int i = 0; i < Paging.processes.length; i++)
                        Paging.processes[i] = new Process(0.000, 0.000, 0.000, i + 1, Paging.S);
                    break;
            case 4: Paging.processes = new Process[4];
                    Paging.processes[0] = new Process(0.750, 0.250, 0.000, 1, Paging.S);
                    Paging.processes[1] = new Process(0.750, 0.000, 0.250, 2, Paging.S);
                    Paging.processes[2] = new Process(0.750, 0.125, 0.125, 3, Paging.S);
                    Paging.processes[3] = new Process(0.500, 0.125, 0.125, 4, Paging.S);
        }

        // Simulate paging
        // Check termination condition 
        int referencesProcessed = 0;
        for (Process p: Paging.processes)
            referencesProcessed += p.referencesProcessed;

        // Loop over processes, if termination condition not met
        int time = 0;
        while (referencesProcessed != (Paging.N * Paging.processes.length)) {
            for (Process p: Paging.processes) {
                // Loop over quantums
                for (int q = 0; q < Paging.quantum; q++) {
                    // Deal with process demands, if all references haven't been dealt with
                    if (p.referencesProcessed < Paging.N) {
                        time++;

                        // Process reference
                        // Compute demanded page number
                        int page = p.lastReference/Paging.P;

                        // Check if demanded page is in frame table
                        int frameIdx = -1;
                        for (int i = 0; i < Paging.frames.size(); i++)
                            if (Paging.frames.get(i) != null)
                                if (Paging.frames.get(i).process == p && Paging.frames.get(i).page == page) {
                                    frameIdx = i;
                                    break;
                                }

                        // Update page use, if page hit and replacement algorithm is lru
                        if (frameIdx != -1) {
                            if (Paging.R.equals("lru")) {
                                Frame frameUsed = Paging.frames.get(frameIdx);
                                Paging.framesUseQueue.remove(frameUsed);
                                Paging.framesUseQueue.add(frameUsed);
                            }
                        }
                        // Process page fault
                        if (frameIdx == -1) {
                            p.pageFaults++;

                            // Instantiate Frame object to process page demand
                            Frame frame = new Frame(p, page, time);

                            // Add frame to empty slot in frame table and update page tracking for fifo or lru
                            if (Paging.frames.contains(null)) {
                                Paging.frames.set(Paging.frames.lastIndexOf(null), frame);

                                if (Paging.R.equals("fifo"))
                                    Paging.framesAddQueue.add(frame);
                                else if (Paging.R.equals("lru"))
                                    Paging.framesUseQueue.add(frame);
                            }
                            // Evict oldest loaded frame and add new frame
                            else if (Paging.R.equals("fifo")) {
                                // Update process residency time and evictions
                                Paging.framesAddQueue.get(0).process.avgResidencyTime += time - Paging.framesAddQueue.get(0).loadTime;
                                Paging.framesAddQueue.get(0).process.evictions++;

                                // Add frame to table in place of the selected victim frame and update frame queue to track oldest loaded frame
                                Paging.frames.set(Paging.frames.indexOf(Paging.framesAddQueue.get(0)), frame);
                                Paging.framesAddQueue.remove(0);
                                Paging.framesAddQueue.add(frame);
                            }
                            // Evict randomly selected frame and add new frame
                            else if (Paging.R.equals("random")) {
                                int randNum = Paging.randNums.get(0);
                                Paging.randNums.remove(0);

                                // Choose random frame to evict and update process residency time and evictions
                                int idx = randNum % Paging.frames.size();
                                Paging.frames.get(idx).process.avgResidencyTime += time - Paging.frames.get(idx).loadTime;
                                Paging.frames.get(idx).process.evictions++;

                                // Add frame to table in place of the selected victim frame
                                Paging.frames.set(idx, frame);
                            }
                            // Evict least recently used frame and add new frame
                            else {
                                // Update process residency time and evictions
                                Paging.framesUseQueue.get(0).process.avgResidencyTime += time - Paging.framesUseQueue.get(0).loadTime;
                                Paging.framesUseQueue.get(0).process.evictions++;

                                // Add frame to table in place of the selected victim frame and update use queue to track leaset recently used frame
                                Paging.frames.set(Paging.frames.indexOf(Paging.framesUseQueue.get(0)), frame);
                                Paging.framesUseQueue.remove(0);
                                Paging.framesUseQueue.add(frame);
                            }
                        }

                        // Update process statistics
                        p.referencesProcessed++;

                        // Generate next reference according to process probabilities and update process reference
                        int randNum = Paging.randNums.get(0);
                        Paging.randNums.remove(0);

                        double prob = randNum/(Integer.MAX_VALUE + 1d);
                        if (prob < p.A)
                            p.lastReference = (p.lastReference + 1) % Paging.S;
                        else if (prob < (p.A + p.B))
                            p.lastReference = (p.lastReference - 5 + Paging.S) % Paging.S;
                        else if (prob < (p.A + p.B + p.C))
                            p.lastReference = (p.lastReference + 4) % Paging.S;
                        else {
                            p.lastReference = Paging.randNums.get(0) % Paging.S;
                            Paging.randNums.remove(0);
                        }
                    }
                }
            }

            // Check termination condition
            referencesProcessed = 0;
            for (Process p: Paging.processes)
                referencesProcessed += p.referencesProcessed;
        }

        // Print output
        print();
    }

    // Define method to print output
    public static void print() {
        // Print input read
        System.out.println("The machine size is " + Paging.M + ".");
        System.out.println("The page size is " + Paging.P + ".");
        System.out.println("The process size is " + Paging.S + ".");
        System.out.println("The job mix number is " + Paging.J + ".");
        System.out.println("The number of references per process is " + Paging.N + ".");
        System.out.println("The replacement algorithm is " + Paging.R + ".\n");

        // Print statistics
        int totFaults = 0;

        int totEvictions = 0;
        double totAvgResidencyTime = 0;

        // Loop over processes to print process specific statistics
        for (int i = 0; i < Paging.processes.length; i++) {
            totFaults += Paging.processes[i].pageFaults;

            totEvictions += Paging.processes[i].evictions;
            totAvgResidencyTime += Paging.processes[i].avgResidencyTime;

            // Print process statistics, if no evictions
            if (Paging.processes[i].evictions == 0) {
                System.out.println("Process " + (i + 1) + " had " + Paging.processes[i].pageFaults + " faults.");
                System.out.println("    With no evictions, the average residence is undefined.");
            }
            // Print process statistics with average residency time
            else {
                double avgResidencyTime = (double)Paging.processes[i].avgResidencyTime / Paging.processes[i].evictions; 
                System.out.println("Process " + (i + 1) + " had " + Paging.processes[i].pageFaults + " faults and " + avgResidencyTime + " average residency.");
            }
        }

        // Print overall statistics, if no evictions
        if (totEvictions == 0) {
            System.out.println("\nThe total number of faults is " + totFaults + ".");
            System.out.println("    With no evictions, the overall average residence is undefined.");
        }
        // Print overall statistics with overall average residency time
        else {
            totAvgResidencyTime /= totEvictions;
            System.out.println("\nThe total number of faults is " + totFaults + " and the overall average residency is " + totAvgResidencyTime + ".");
        }
    }
}

// Define process class
class Process {
    // Declare (and initialize) instance variables
    // Probabilities
    double A, B, C;

    // Reference to be processed and total references processed already
    int lastReference;
    int referencesProcessed = 0;

    // Total page faults
    int pageFaults = 0;

    // Total page evictions and running sum to track residency time
    int evictions = 0;
    int avgResidencyTime = 0;

    // Define constructor
    public Process(double A, double B, double C, int idx, int S) {
        this.A = A;
        this.B = B;
        this.C = C;

        // Initial reference to be processed
        this.lastReference = (111 * idx) % S;
    }
}

// Define frame class
class Frame {
    // Declare instance variables
    // Calling process and page id
    Process process;
    int page;

    // Timestamp to track start of residency
    int loadTime;

    // Define constructor
    public Frame(Process process, int page, int loadTime) {
        this.process = process;
        this.page = page;

        this.loadTime = loadTime;
    }
}