import java.io.*;
import java.util.*;

// Define main program class
public class Paging {
    // Declare (and initialize/instantiate) class variables
    static int M, P, S, J, N;
    static String R; 

    static int quantum = 3;
    static ArrayList<Integer> randNums = new ArrayList<>();

    static Process[] processes;

    static ArrayList<Frame> frames = new ArrayList<>();
    static ArrayList<Frame> framesAddQueue = new ArrayList<>();
    static ArrayList<Frame> framesUseStack = new ArrayList<>();

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
            System.out.println("'random-number.txt' file not found.");
            System.exit(1);
        }

        // Instantiate frame table
        for (int i = 0; i < (Paging.M/Paging.P); i++)
            Paging.frames.add(null);

        // Instantiate processes
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
        int referencesProcessed = 0;
        for (Process p: Paging.processes)
            referencesProcessed += p.referencesProcessed;

        int time = 0;
        while (referencesProcessed != (Paging.N * Paging.processes.length)) {
            for (Process p: Paging.processes) {
                if (p.referencesProcessed != Paging.N) {
                    for (int q = 0; q < Paging.quantum; q++) {
                        time++;

                        // Process reference
                        int page = p.lastReference/Paging.P;
                        int frameIdx = -1;
                        for (int i = 0; i < Paging.frames.size(); i++)
                            if (Paging.frames.get(i).process == p && Paging.frames.get(i).page == page) {
                                frameIdx = i;
                                break;
                            }
                        
                        if (frameIdx != -1)
                            break;
                        else {
                            p.pageFaults++;

                            Frame frame = new Frame(p, page, time);
                            if (Paging.frames.contains(null)) {
                                Paging.frames.add(Paging.frames.indexOf(null), frame);
                                Paging.framesAddQueue.add(frame);
                            }
                            else if (Paging.R.equals("fifo")) {
                                Paging.framesAddQueue.get(0).process.avgResidencyTime += time - Paging.framesAddQueue.get(0).loadTime;

                                Paging.frames.set(Paging.frames.indexOf(Paging.framesAddQueue.get(0)), frame);
                                Paging.framesAddQueue.remove(0);
                            }
                            else if (Paging.R.equals("random")) {
                                int randNum = Paging.randNums.get(0);
                                Paging.randNums.remove(0);

                                int idx = randNum % Paging.frames.size();
                                Paging.frames.get(idx).process.avgResidencyTime += time - Paging.frames.get(idx).loadTime;

                                Paging.frames.set(idx, frame);
                            }
                            else {
                                int lastIdx = Paging.framesUseStack.size() - 1;
                                Paging.framesUseStack.get(lastIdx).process.avgResidencyTime += time - Paging.framesUseStack.get(lastIdx).loadTime;

                                Paging.frames.set(Paging.frames.indexOf(Paging.framesUseStack.get(lastIdx)), frame);
                                Paging.framesUseStack.remove(lastIdx);
                            }
                        }

                        // Generate reference
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
        double totAvgResidencyTime = 0;

        for (int i = 0; i < Paging.processes.length; i++) {
            totFaults += Paging.processes[i].pageFaults;
            totAvgResidencyTime += Paging.processes[i].avgResidencyTime;

            double avgResidencyTime = Paging.processes[i].avgResidencyTime/Paging.processes[i].pageFaults; 
            System.out.println("Process " + i + " had " + Paging.processes[i].pageFaults + " and " + avgResidencyTime + " average residency.");
        }

        System.out.println("\nThe total number of faults is " + totFaults + " and the overall average residency is " + totAvgResidencyTime + ".");
    }
}

// Define process class
class Process {
    // Declare (and initialize) instance variables
    double A, B, C;

    int lastReference;
    int referencesProcessed = 0;

    int pageFaults = 0;
    int avgResidencyTime = 0;

    // Define constructor
    public Process(double A, double B, double C, int idx, int S) {
        this.A = A;
        this.B = B;
        this.C = C;

        this.lastReference = (111 * idx) % S;
    }
}

// Define frame class
class Frame {
    // Declare instance variables
    Process process;
    int page;

    int loadTime;

    // Define constructor
    public Frame(Process process, int page, int loadTime) {
        this.process = process;
        this.page = page;

        this.loadTime = loadTime;
    }
}