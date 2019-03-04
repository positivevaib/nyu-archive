import java.io.*;
import java.util.*;

// Scheduler class
public class Scheduler {
    // Class vars
    static boolean verbose = False;

    static String inputFileName = null;

    static Process[][] allProcesses = new Process[4][]; 

    static ArrayList<Integer> randNums = new ArrayList<>();
    static Random random = new Random(1);

    // Main method
    public static void main(String[] args) {
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
                    throw Exception;
                else   
                    Scheduler.verbose = True;
            }
        }
        catch(Exception e) {
            System.out.println("None or incorrect args given.");
            System.exit(2);
        }

        // Call scheduling algorithms
    }

    // Method to implement FCFS scheduling algorithm
    public static void FCFS() {
        // Instantiate processes
        Process[] processes = instantiateProcesses();
        Scheduler.allProcesses[0] = processes;

        // Check processes' states
        boolean allProcessesTerminated = True;
        for (Process p: Scheduler.processes)
            allProcessesTerminated = allProcessesTerminated && p.terminated;

        int currentCycle = 0;
        while (!allProcessesTerminated) {
            

            // Re-check processes' states
            allProcessesTerminated = True;
            for (Process p: Scheduler.processes)
                allProcessesTerminated = allProcessesTerminated && p.terminated;
        }
    }

    // Method to read input file and instantiate processes
    public static Process[] instantiateProcesses() {
        try {
            Scanner fileIn = new Scanner(new File(inputFileName));

            int totProcesses = fileIn.nextInt();
            Process[] processes = new int[totProcesses];

            for (int i = 0; i < totProcesses; i++) {
                int A = fileIn.nextInt();
                int B = fileIn.nextInt();
                int C = fileIn.nextInt();
                int IO = fileIn.nextInt();

                processes[i] = new Process(A, B, C, IO);
            }

            return processes;
        }
        catch (FileNotFoundException e) {
            System.out.println("Input file not found.");
            System.exit(1);
        }
    }

    // Method to select uniformly distributed random integers
    public static int randomOS(int U) {
        int idx = Scheduler.random.nextInt(Scheduler.randNums.size() - 1);

        return (1 + (Scheduler.randNums.get(idx) % U));
    }
}

// Process class
class Process {
    // Instance vars
    int A, B, C, IO;

    String currentState = "unstarted";
    boolean terminated = False;

    ArrayList<String> stateLog = new ArrayList<>();
    ArrayList<Integer> burstLog = new ArrayList<>();
    
    // Constructor
    public Process(int A, int B, int C, int IO) {
        this.A = A;
        this.B = B;
        this.C = C;
        this.IO = IO;

        self.stateLog.add(self.currentState);
        self.burstLog.add(0);
    }
}