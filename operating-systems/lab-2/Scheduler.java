import java.io.*;
import java.lang.*;
import java.util.*;

// Scheduler class
public class Scheduler {
    // Class vars
    static boolean showRandom = false;
    static boolean verbose = false;

    static String inputFileName = null;

    static ArrayList<Integer> randNums = new ArrayList<>();
    static int randIdx = -1;

    // Main method
    public static void main(String[] args) {
        System.out.println();

        // Read random-numbers file
        try {
            Scanner fileIn = new Scanner(new File("random-numbers.txt"));

            while (fileIn.hasNext())
                Scheduler.randNums.add(fileIn.nextInt());

            fileIn.close();
        }
        catch(FileNotFoundException e) {
            System.out.println("'random-number.txt' file not found.");
            System.exit(1);
        }

        // Read command line args
        try {
            Scheduler.inputFileName = args[args.length - 1];

            if (args.length == 2) {
                if (!args[0].equals("--verbose"))
                    throw (new Exception());
                else   
                    Scheduler.verbose = true;
            }
        }
        catch(Exception e) {
            System.out.println("None or incorrect args given.");
            System.exit(2);
        }

        // Call scheduling algorithms
        FCFS();
        System.out.println();

        RR();
        System.out.println();

        Uniprocessor();
        System.out.println();
        
        SJF();
    }

    // Method to implement FCFS scheduling algorithm
    public static void FCFS() {
        // Reset randIdx
        Scheduler.randIdx = -1;

        // Instantiate processes
        Process[] processes = instantiateProcesses();

        // Check processes' states
        boolean allProcessesTerminated = true;
        for (Process p: processes) {
            allProcessesTerminated = allProcessesTerminated && p.terminated;
        }

        // Implement FCFS scheduling algorithm
        if (Scheduler.verbose)
            System.out.println("\n" + "This detailed printout gives the state and remaining burst for each process." + "\n");

        Process running = null;
        Process ready = null;

        int currentCycle = 0;
        while (!allProcessesTerminated) {
            // Print detailed output, if verbose
            if (Scheduler.verbose) {
                System.out.print("Before cycle    " + currentCycle + ": ");

                for (Process p: processes) {
                    System.out.printf(" %10s ", p.currentState);
                    if (p.currentState.equals("running"))
                        System.out.print(p.remCPUBurst);
                    else if (p.currentState.equals("blocked"))
                        System.out.print(p.remIOBurst);
                    else
                        System.out.print(0);
                }

                System.out.println();
            }

            // Check on the running process
            if (running != null) {
                running.remCPUBurst--;
                running.remCPUTime--;

                if (running.remCPUBurst == 0) {
                    if (running.remCPUTime == 0) {
                        running.currentState = "terminated";
                        running.terminated = true;

                        running.finishTime = currentCycle;

                        running = null;
                    }
                    else {
                        running.currentState = "blocked";
                        running.remIOBurst = randomOS(running.IO, "IO") + 1;
                        running.IOTime--;

                        running = null;
                    }
                }
            }

            // Check on blocked processes
            for (Process p: processes) {
                if (p.currentState.equals("blocked")) {
                    p.remIOBurst--;
                    p.IOTime++;

                    if (p.remIOBurst == 0) {
                        p.currentState = "ready";
                        p.readyBurst = -1;
                        p.waitTime--;
                    }
                }
            }

            // Check on unstarted processes
            for (Process p: processes) {
                if (p.currentState.equals("unstarted") && p.A == currentCycle) {
                    p.currentState = "ready";
                    p.readyBurst = -1;
                    p.waitTime = -1;
                }
            }

            // Check on ready processes
            for (Process p: processes) {
                if (p.currentState.equals("ready")) {
                    p.readyBurst++;
                    p.waitTime++;

                    if (ready == null)
                        ready = p;
                    else if (p.readyBurst > ready.readyBurst)
                        ready = p;
                }
            }

            if (running == null && ready != null) {
                running = ready;
                ready = null;

                running.readyBurst = 0;

                running.currentState = "running";

                int randomBurst = randomOS(running.B, "CPU");
                if (randomBurst > running.remCPUTime)
                    randomBurst = running.remCPUTime;

                running.remCPUBurst = randomBurst;
            }

            // Re-check processes' states
            allProcessesTerminated = true;
            for (Process p: processes)
                allProcessesTerminated = allProcessesTerminated && p.terminated;

            // Update currentCycle
            currentCycle++;
        }

        System.out.println("\nThe scheduling algorithm used was First Come First Served.\n");

        // Print results and summary
        double CPUUtilTime = 0;
        double IOUtilTime = 0;
        int totTurnaroundTime = 0;
        int totWaitTime = 0;

        int processNum = 0;
        for (Process p: processes) {
            System.out.println("Process " + processNum + ":");
            System.out.println("        (A, B, C, IO) = (" + p.A + ", " + p.B + ", " + p.C + ", " + p.IO + ")");
            System.out.println("        Finishing time: " + p.finishTime);
            System.out.println("        Turnaround time: " + (p.finishTime - p.A));
            System.out.println("        I/O time: " + p.IOTime);
            System.out.println("        Waiting time: " + p.waitTime);
            System.out.println();

            CPUUtilTime += (p.finishTime - p.A - p.IOTime - p.waitTime);
            IOUtilTime += (p.IOTime);
            totTurnaroundTime += (p.finishTime - p.A);
            totWaitTime += p.waitTime;

            processNum++;
        }

        CPUUtilTime /= (currentCycle - 1);
        IOUtilTime /= (currentCycle - 1);

        double avgTurnaroundTime = (totTurnaroundTime + 0.0)/processes.length;
        double avgWaitTime = (totWaitTime + 0.0)/processes.length;

        System.out.println("Summary data:");
        System.out.println("        Finishing time: " + (currentCycle - 1));
        System.out.printf("        CPU utilization: %1.3f\n", CPUUtilTime);
        System.out.printf("        IO utilization: %1.3f\n", IOUtilTime);
        System.out.printf("        Throughput: %1.3f processes per hundred cycles.\n", ((processes.length/(currentCycle - 1.0)) * 100));
        System.out.printf("        Average turnaround time: %1.3f\n", avgTurnaroundTime);
        System.out.printf("        Average waiting time: %1.3f\n\n", avgWaitTime);
    }

    // Method to implement RR scheduling algorithm with quantum 2
    public static void RR() {
        // Reset randIdx
        Scheduler.randIdx = -1;

        // Instantiate processes
        Process[] processes = instantiateProcesses();

        // Check processes' states
        boolean allProcessesTerminated = true;
        for (Process p: processes) {
            allProcessesTerminated = allProcessesTerminated && p.terminated;
        }

        // Implement RR scheduling algorithm
        if (Scheduler.verbose)
            System.out.println("\n" + "This detailed printout gives the state and remaining burst for each process." + "\n");

        Process running = null;
        Process ready = null;

        int quantum = 2;
        int currentCycle = 0;

        while (!allProcessesTerminated) {
            // Print detailed output, if verbose
            if (Scheduler.verbose) {
                System.out.print("Before cycle    " + currentCycle + ": ");

                for (Process p: processes) {
                    System.out.printf(" %10s ", p.currentState);
                    if (p.currentState.equals("running"))
                        System.out.print(p.remCPUBurst);
                    else if (p.currentState.equals("blocked"))
                        System.out.print(p.remIOBurst);
                    else
                        System.out.print(0);
                }

                System.out.println();
            }

            // Check on the running process
            if (running != null) {
                quantum--;

                running.remCPUBurst--;
                running.remCPUTime--;

                if (running.remCPUBurst == 0) {
                    if (running.remCPUTime == 0) {
                        running.currentState = "terminated";
                        running.terminated = true;

                        running.finishTime = currentCycle;

                        running = null;
                    }
                    else {
                        running.currentState = "blocked";
                        running.remIOBurst = randomOS(running.IO, "IO") + 1;
                        running.IOTime--;

                        running = null;
                    }
                }
                else if (quantum == 0) {
                    running.currentState = "ready";
                    running.readyBurst = -1;
                    running.waitTime--;

                    running = null;
                }
            }

            // Check on blocked processes
            for (Process p: processes) {
                if (p.currentState.equals("blocked")) {
                    p.remIOBurst--;
                    p.IOTime++;

                    if (p.remIOBurst == 0) {
                        p.currentState = "ready";
                        p.readyBurst = -1;
                        p.waitTime--;
                    }
                }
            }

            // Check on unstarted processes
            for (Process p: processes) {
                if (p.currentState.equals("unstarted") && p.A == currentCycle) {
                    p.currentState = "ready";
                    p.readyBurst = -1;
                    p.waitTime = -1;
                }
            }

            // Check on ready processes
            for (Process p: processes) {
                if (p.currentState.equals("ready")) {
                    p.readyBurst++;
                    p.waitTime++;

                    if (ready == null)
                        ready = p;
                    else if (p.readyBurst > ready.readyBurst)
                        ready = p;
                }
            }

            if (running == null && ready != null) {
                running = ready;
                ready = null;

                quantum = 2;

                running.readyBurst = 0;

                running.currentState = "running";

                if (running.remCPUBurst == 0) {
                    int randomBurst = randomOS(running.B, "CPU");
                    if (randomBurst > running.remCPUTime)
                        randomBurst = running.remCPUTime;
    
                    running.remCPUBurst = randomBurst;
                }
            }

            // Re-check processes' states
            allProcessesTerminated = true;
            for (Process p: processes)
                allProcessesTerminated = allProcessesTerminated && p.terminated;

            // Update currentCycle
            currentCycle++;
        }

        System.out.println("\nThe scheduling algorithm used was Round Robin with a quantum of 2.\n");

        // Print results and summary
        double CPUUtilTime = 0;
        double IOUtilTime = 0;
        int totTurnaroundTime = 0;
        int totWaitTime = 0;

        int processNum = 0;
        for (Process p: processes) {
            System.out.println("Process " + processNum + ":");
            System.out.println("        (A, B, C, IO) = (" + p.A + ", " + p.B + ", " + p.C + ", " + p.IO + ")");
            System.out.println("        Finishing time: " + p.finishTime);
            System.out.println("        Turnaround time: " + (p.finishTime - p.A));
            System.out.println("        I/O time: " + p.IOTime);
            System.out.println("        Waiting time: " + p.waitTime);
            System.out.println();

            CPUUtilTime += (p.finishTime - p.A - p.IOTime - p.waitTime);
            IOUtilTime += (p.IOTime);
            totTurnaroundTime += (p.finishTime - p.A);
            totWaitTime += p.waitTime;

            processNum++;
        }

        CPUUtilTime /= (currentCycle - 1);
        IOUtilTime /= (currentCycle - 1);

        double avgTurnaroundTime = (totTurnaroundTime + 0.0)/processes.length;
        double avgWaitTime = (totWaitTime + 0.0)/processes.length;

        System.out.println("Summary data:");
        System.out.println("        Finishing time: " + (currentCycle - 1));
        System.out.printf("        CPU utilization: %1.3f\n", CPUUtilTime);
        System.out.printf("        IO utilization: %1.3f\n", IOUtilTime);
        System.out.printf("        Throughput: %1.3f processes per hundred cycles.\n", ((processes.length/(currentCycle - 1.0)) * 100));
        System.out.printf("        Average turnaround time: %1.3f\n", avgTurnaroundTime);
        System.out.printf("        Average waiting time: %1.3f\n\n", avgWaitTime);
    }

    // Method to implement Uniprocessor scheduling algorithm
    public static void Uniprocessor() {
        // Reset randIdx
        Scheduler.randIdx = -1;

        // Instantiate processes
        Process[] processes = instantiateProcesses();

        // Implement Uniprocessor scheduling algorithm
        if (Scheduler.verbose)
            System.out.println("\n" + "This detailed printout gives the state and remaining burst for each process." + "\n");

        int currentProcessIdx = 0;
        Process currentProcess = processes[currentProcessIdx];

        int currentCycle = 0;
        while (!processes[processes.length - 1].terminated) {
            // Print detailed output, if verbose
            if (Scheduler.verbose) {
                System.out.print("Before cycle    " + currentCycle + ": ");

                for (Process p: processes) {
                    System.out.printf(" %10s ", p.currentState);
                    if (p.currentState.equals("running"))
                        System.out.print(p.remCPUBurst);
                    else if (p.currentState.equals("blocked"))
                        System.out.print(p.remIOBurst);
                    else
                        System.out.print(0);
                }

                System.out.println();
            }

            for (Process p: processes) {
                // Check on current process, if running
                if (p.currentState.equals("running")) {
                    p.remCPUBurst--;
                    p.remCPUTime--;

                    if (p.remCPUBurst == 0) {
                        if (p.remCPUTime == 0) {
                            p.currentState = "terminated";
                            p.terminated = true;

                            p.finishTime = currentCycle;

                            if (currentProcessIdx != (processes.length - 1)) {
                                currentProcessIdx += 1;
                                currentProcess = processes[currentProcessIdx];
                            }
                        }
                        else {
                            p.currentState = "blocked";
                            p.remIOBurst = randomOS(p.IO, "IO") + 1;
                            p.IOTime--;
                        }
                    }
                }

                // Check on current process, if blocked
                if (p.currentState.equals("blocked")) {
                    p.remIOBurst--;
                    p.IOTime++;

                    if (p.remIOBurst == 0) {
                        p.currentState = "ready";
                        p.readyBurst = -1;
                        p.waitTime--;
                    }
                }

                // Check on process, if unstarted
                if (p.currentState.equals("unstarted") && p.A == currentCycle) {
                    p.currentState = "ready";
                    p.readyBurst = -1;
                    p.waitTime = -1;
                }

                // Check on process, if ready
                if (p.currentState.equals("ready")) {
                    p.readyBurst++;
                    p.waitTime++;

                    if (p == currentProcess) {
                        p.readyBurst = 0;

                        p.currentState = "running";
    
                        int randomBurst = randomOS(p.B, "CPU");
                        if (randomBurst > p.remCPUTime)
                            randomBurst = p.remCPUTime;
    
                        p.remCPUBurst = randomBurst;
                    }
                }
            }

            // Update currentCycle
            currentCycle++;
        }

        System.out.println("\nThe scheduling algorithm used was Uniprocessor.\n");

        // Print results and summary
        double CPUUtilTime = 0;
        double IOUtilTime = 0;
        int totTurnaroundTime = 0;
        int totWaitTime = 0;

        int processNum = 0;
        for (Process p: processes) {
            System.out.println("Process " + processNum + ":");
            System.out.println("        (A, B, C, IO) = (" + p.A + ", " + p.B + ", " + p.C + ", " + p.IO + ")");
            System.out.println("        Finishing time: " + p.finishTime);
            System.out.println("        Turnaround time: " + (p.finishTime - p.A));
            System.out.println("        I/O time: " + p.IOTime);
            System.out.println("        Waiting time: " + p.waitTime);
            System.out.println();

            CPUUtilTime += (p.finishTime - p.A - p.IOTime - p.waitTime);
            IOUtilTime += (p.IOTime);
            totTurnaroundTime += (p.finishTime - p.A);
            totWaitTime += p.waitTime;

            processNum++;
        }

        CPUUtilTime /= (currentCycle - 1);
        IOUtilTime /= (currentCycle - 1);

        double avgTurnaroundTime = (totTurnaroundTime + 0.0)/processes.length;
        double avgWaitTime = (totWaitTime + 0.0)/processes.length;

        System.out.println("Summary data:");
        System.out.println("        Finishing time: " + (currentCycle - 1));
        System.out.printf("        CPU utilization: %1.3f\n", CPUUtilTime);
        System.out.printf("        IO utilization: %1.3f\n", IOUtilTime);
        System.out.printf("        Throughput: %1.3f processes per hundred cycles.\n", ((processes.length/(currentCycle - 1.0)) * 100));
        System.out.printf("        Average turnaround time: %1.3f\n", avgTurnaroundTime);
        System.out.printf("        Average waiting time: %1.3f\n\n", avgWaitTime);
    }

    // Method to implement SJF scheduling algorithm
    public static void SJF() {
        // Reset randIdx
        Scheduler.randIdx = -1;

        // Instantiate processes
        Process[] processes = instantiateProcesses();

        // Check processes' states
        boolean allProcessesTerminated = true;
        for (Process p: processes) {
            allProcessesTerminated = allProcessesTerminated && p.terminated;
        }

        // Implement SJF scheduling algorithm
        if (Scheduler.verbose)
            System.out.println("\n" + "This detailed printout gives the state and remaining burst for each process." + "\n");

        Process running = null;
        Process ready = null;

        int currentCycle = 0;
        while (!allProcessesTerminated) {
            // Print detailed output, if verbose
            if (Scheduler.verbose) {
                System.out.print("Before cycle    " + currentCycle + ": ");

                for (Process p: processes) {
                    System.out.printf(" %10s ", p.currentState);
                    if (p.currentState.equals("running"))
                        System.out.print(p.remCPUBurst);
                    else if (p.currentState.equals("blocked"))
                        System.out.print(p.remIOBurst);
                    else
                        System.out.print(0);
                }

                System.out.println();
            }

            for (Process p: processes) {
                // Check on running processes
                if (p.currentState.equals("running")) {
                    p.remCPUBurst--;
                    p.remCPUTime--;

                    if (p.remCPUBurst == 0) {
                        if (p.remCPUTime == 0) {
                            p.currentState = "terminated";
                            p.terminated = true;

                            p.finishTime = currentCycle;

                            running = null;
                        }
                        else {
                            p.currentState = "blocked";
                            p.remIOBurst = randomOS(p.IO, "IO") + 1;
                            p.IOTime--;

                            running = null;
                        }
                    }
                }

                // Check on blocked processes
                if (p.currentState.equals("blocked")) {
                    p.remIOBurst--;
                    p.IOTime++;

                    if (p.remIOBurst == 0) {
                        p.currentState = "ready";
                        p.readyBurst = -1;
                        p.waitTime--;
                    }
                }

                // Check on unstarted processes
                if (p.currentState.equals("unstarted") && p.A == currentCycle) {
                    p.currentState = "ready";
                    p.readyBurst = -1;
                    p.waitTime = -1;
                }

                // Check on ready processes
                if (p.currentState.equals("ready")) {
                    p.readyBurst++;
                    p.waitTime++;

                    if (ready == null)
                        ready = p;
                    else if (p.remCPUTime < ready.remCPUTime)
                        ready = p;
                }
            }

            // Run shortest process, if none other running
            for (Process p: processes) {
                if (running == null && ready != null) {
                    running = ready;
                    ready = null;

                    running.readyBurst = 0;

                    running.currentState = "running";

                    int randomBurst = randomOS(running.B, "CPU");
                    if (randomBurst > running.remCPUTime)
                        randomBurst = running.remCPUTime;

                    running.remCPUBurst = randomBurst;
                }
            }

            // Re-check processes' states
            allProcessesTerminated = true;
            for (Process p: processes)
                allProcessesTerminated = allProcessesTerminated && p.terminated;

            // Update currentCycle
            currentCycle++;
        }

        System.out.println("\nThe scheduling algorithm used was Shortest Job First.\n");

        // Print results and summary
        double CPUUtilTime = 0;
        double IOUtilTime = 0;
        int totTurnaroundTime = 0;
        int totWaitTime = 0;

        int processNum = 0;
        for (Process p: processes) {
            System.out.println("Process " + processNum + ":");
            System.out.println("        (A, B, C, IO) = (" + p.A + ", " + p.B + ", " + p.C + ", " + p.IO + ")");
            System.out.println("        Finishing time: " + p.finishTime);
            System.out.println("        Turnaround time: " + (p.finishTime - p.A));
            System.out.println("        I/O time: " + p.IOTime);
            System.out.println("        Waiting time: " + p.waitTime);
            System.out.println();

            CPUUtilTime += (p.finishTime - p.A - p.IOTime - p.waitTime);
            IOUtilTime += (p.IOTime);
            totTurnaroundTime += (p.finishTime - p.A);
            totWaitTime += p.waitTime;

            processNum++;
        }

        CPUUtilTime /= (currentCycle - 1);
        IOUtilTime /= (currentCycle - 1);

        double avgTurnaroundTime = (totTurnaroundTime + 0.0)/processes.length;
        double avgWaitTime = (totWaitTime + 0.0)/processes.length;

        System.out.println("Summary data:");
        System.out.println("        Finishing time: " + (currentCycle - 1));
        System.out.printf("        CPU utilization: %1.3f\n", CPUUtilTime);
        System.out.printf("        IO utilization: %1.3f\n", IOUtilTime);
        System.out.printf("        Throughput: %1.3f processes per hundred cycles.\n", ((processes.length/(currentCycle - 1.0)) * 100));
        System.out.printf("        Average turnaround time: %1.3f\n", avgTurnaroundTime);
        System.out.printf("        Average waiting time: %1.3f\n\n", avgWaitTime);
    }

    // Method to read and print the input file and instantiate and sort processes
    public static Process[] instantiateProcesses() {
        try {
            // Read input file and instantiate processes
            Scanner fileIn = new Scanner(new File(inputFileName));

            int totProcesses = fileIn.nextInt();
            System.out.print("The original input was: " + totProcesses);

            int latestArrivalTime = -1; 

            Process[] processes = new Process[totProcesses];
            for (int i = 0; i < totProcesses; i++) {
                int A = fileIn.nextInt();
                if (A > latestArrivalTime)
                    latestArrivalTime = A;

                int B = fileIn.nextInt();
                int C = fileIn.nextInt();
                int IO = fileIn.nextInt();

                processes[i] = new Process(A, B, C, IO);

                System.out.print("  " + A + " " + B + " " + C + " " + IO);
            }
            System.out.println();

            fileIn.close();

            // Sort processes
            System.out.print("The (sorted) input is: " + totProcesses);

            int idx = 0;
            Process[] sortedProcesses = new Process[processes.length];
            for (int i = 0; i <= latestArrivalTime; i++) {
                for (Process p: processes) {
                    if (p.A == i) {
                        sortedProcesses[idx] = p;
                        idx++;

                        System.out.print("  " + p.A + " " + p.B + " " + p.C + " " + p.IO);
                    }
                }
            }
            System.out.println();

            return sortedProcesses;
        }
        catch (FileNotFoundException e) {
            // Print error and terminate the program
            System.out.println("Input file not found.");
            System.exit(1);
        }

        return null;
    }

    // Method to select uniformly distributed random integers
    public static int randomOS(int U, String mode) {
        // Update index and select random integer
        Scheduler.randIdx++;
        int randInt = 1 + (Scheduler.randNums.get(Scheduler.randIdx) % U);

        // Print selected random integers, if required
        if (Scheduler.showRandom) {
            if (mode.equals("IO"))
                System.out.println("Find I/O burst when blocking a process: " + Scheduler.randNums.get(Scheduler.randIdx));
            else
                System.out.println("Find burst when choosing ready process to run: " + Scheduler.randNums.get(Scheduler.randIdx));
        }

        return (randInt);
    }
}

// Process class
class Process {
    // Instance vars
    int A, B, C, IO;

    String currentState = "unstarted";
    boolean terminated = false;

    int readyBurst = 0;

    int remCPUTime; 
    int remCPUBurst = 0;
    int remIOBurst = 0;

    int IOTime = 0;
    int waitTime = 0;
    int finishTime = 0;
    
    // Constructor
    public Process(int A, int B, int C, int IO) {
        this.A = A;
        this.B = B;
        this.C = C;
        this.IO = IO;

        this.remCPUTime = this.C;
    }
}