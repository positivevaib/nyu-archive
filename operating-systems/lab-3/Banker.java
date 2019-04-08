import java.io.*;
import java.util.*;

// Banker class
public class Banker {
    // Class vars.
    static Task[] tasks;

    static int[] resources;

    // Main method
    public static void main(String[] args) {
        System.out.println();

        // Check command line argument
        if (args.length != 1)
            System.out.println("No input filename provided.");
            System.exit(1);

        // Read input
        try {
            Scanner fileIn = new Scanner(new File(args[0]));

            while (fileIn.hasNext()) {
                // Read total nb. of tasks and create Task objects
                Banker.tasks = new Task[fileIn.nextInt()];
                for (int i = 0; i < Banker.tasks.length; i++)
                    Banker.tasks[i] = new Task();

                // Read and store total nb. of resource types
                Banker.resources = new int[fileIn.nextInt()];
                for (int i = 0; i < Banker.resources.length; i++)
                    Banker.resources[i] = fileIn.nextInt();

                // Declare and initialize task activity info. vars.
                String activity = "";
                int taskNb = 0;

                // Read and store tasks' activities info.
                while (fileIn.hasNext()) {
                    activity = fileIn.next();
                    taskNb = fileIn.nextInt();
                    Banker.tasks[taskNb - 1].activities.add(activity);
                    Banker.tasks[taskNb - 1].delays.add(fileIn.nextInt());
                    Banker.tasks[taskNb - 1].resourceTypes.add(fileIn.nextInt());
                    Banker.tasks[taskNb - 1].activityInfo.add(fileIn.nextInt());
                }
            }

            // Close fileIn
            fileIn.close();
        }
        catch (FileNotFoundException e) {
            System.out.println(args[0] + " file not found.");
            System.exit(1);
        }

        // Simulate the optimistic resource alloc.
        optimAlloc();

        // Simulate the banker resource alloc.
        bankerAlloc();
    }

    // Method to simulate the optimistic resource alloc.
    public static void optimAlloc() {
        
    }

    // Method to simulate the banker resource alloc.
    public static void bankerAlloc() {

    }
}

class Task {
    // Instance vars.
    ArrayList<String> activities = new ArrayList<>();
    ArrayList<Integer> delays = new ArrayList<>();
    ArrayList<Integer> resourceTypes = new ArrayList<>();
    ArrayList<Integer> activityInfo = new ArrayList<>();

    // Constructor
    public Task(){}
}