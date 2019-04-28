import java.io.*;
import java.lang.*;
import java.util.*;


// Define main program class
public class Paging {
    // Declare class variables
    static int M, P, S, J, N;
    static String R;

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


    }

    
}