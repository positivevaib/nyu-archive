import java.io.*;
import java.lang.*;
import java.util.*;

// Define main program class
public class Paging {
    // Declare (and initialize/instantiate) class variables
    static int M, P, S, J, N;
    static String R; 

    static int quantum = 3;
    static ArrayList<Integer> randNums = new ArrayList<>();

    static Program[] programs;

    static ArrayList<Frame> frames = new ArrayList<>();
    static ArrayList<Frame> framesQueue = new ArrayList<>();

    static ArrayList<Program> processes = new ArrayList<>();
    static ArrayList<Integer> pages = new ArrayList<>();

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

        // Instantiate programs
        switch (Paging.J) {
            case 1: Paging.programs = new Program[1];
                    Paging.programs[0] = new Program(1, 0, 0);
                    break;
            case 2: Paging.programs = new Program[4];
                    for (int i = 0; i < Paging.programs.length; i++)
                        Paging.programs[i] = new Program(1, 0, 0);
                    break;
            case 3: Paging.programs = new Program[4];
                    for (int i = 0; i < Paging.programs.length; i++)
                        Paging.programs[i] = new Program(0, 0, 0);
            case 4: Paging.programs = new Program[4];
                    Paging.programs[0] = new Program(0.75, 0.25, 0);
                    Paging.programs[1] = new Program(0.75, 0, 0.25);
                    Paging.programs[2] = new Program(0.75, 0.125, 0.125);
                    Paging.programs[3] = new Program(0.5, 0.125, 0.125);
        }
    }
}

// Define program class
class Program {
    // Declare instance variables
    float A, B, C;
    int lastReference;

    // Define constructor
    public Program(float A, float B, float C) {
        this.A = A;
        this.B = B;
        this.C = C;
    }
}

// Define frame class
class Frame {
    // Declare instance variables
    Program program;
    int page;

    // Define constructor
    public Frame(Program program, int page) {
        this.program = program;
        this.page = page;
    }
}