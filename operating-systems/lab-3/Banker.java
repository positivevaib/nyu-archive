import java.io.*;
import java.util.*;

// Banker class
public class Banker {
    // Class vars.
    static Task[] tasks;

    static int[] totResources;
    static int[] availResources;
    static int[] limboResources;

    // Main method
    public static void main(String[] args) {
        System.out.println();

        // Check command line argument
        if (args.length != 1)
            System.out.println("No input filename provided.");
            System.exit(1);

        // Read input
        try {
            // Create Scanner object
            Scanner fileIn = new Scanner(new File(args[0]));

            // Read and store vals.
            while (fileIn.hasNext()) {
                // Read total nb. of tasks
                Banker.tasks = new Task[fileIn.nextInt()];

                // Read and store total nb. of resource types
                Banker.totResources = new int[fileIn.nextInt()];
                for (int i = 0; i < Banker.totResources.length; i++)
                    Banker.totResources[i] = fileIn.nextInt();

                // Initialize availResources array
                Banker.availResources = new int[Banker.totResources.length];
                for (int i = 0; i < Banker.availResources.length; i++)
                    Banker.availResources[i] = Banker.totResources[i];

                // Initialize limboResources array
                Banker.limboResources = new int[Banker.totResources.length];
                for (int i = 0; i < Banker.limboResources.length; i++)
                    Banker.limboResources[i] = 0;

                // Create Task objects
                for (int i = 0; i < Banker.tasks.length; i++)
                    Banker.tasks[i] = new Task(i, Banker.totResources.length);

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

            /*// Create task activity cycle correspondence
            for (Task task: Banker.tasks) {
                int offset = 0;
                for (int i = 0; i < task.delays.length; i++) {
                    task.cycles[i] = offset + task.delays[i];
                    offset = task.cycles[i];
                }
            }*/ // need to check the need for this code block

            // Close fileIn
            fileIn.close();
        }
        catch (FileNotFoundException e) {
            System.out.println(args[0] + " file not found.");
            System.exit(1);
        }

        // Simulate the optimistic resource alloc.
        fifoAlloc();

        // Simulate the banker resource alloc.
        bankerAlloc();
    }

    // Method to simulate the optimistic resource alloc.
    public static void fifoAlloc() {
        // Declare and instantiate an ArrayList to track pending requests
        ArrayList<Integer> waitingTasks = new ArrayList<>();

        // Check tasks' termination states
        boolean allTasksTerminated = true;
        for (Task task: Banker.tasks)
            allTasksTerminated = allTasksTerminated && task.terminated;

        // Declare and initialize flag to indicate need for deadlock check
        boolean allTasksWaiting = false;

        // Declare and initialize current cycle
        int currentCycle = 0;
        
        // Simulate runtime
        while (!allTasksTerminated) {
            // Check for deadlocks
            if (allTasksWaiting) {
                // try to allocate resources to the first task
                // if not possible then remove a task and repeat
            }

            // Terminate tasks

            // Initiate tasks
            for (Task task: Banker.tasks) {
                if (!task.initialized && task.delays[0] == currentCycle) {
                    task.initialized = true;

                    task.currentActivity++;
                } 
            }

            // Check and update tasks' termination states
            allTasksTerminated = true;
            for (Task task: Banker.tasks)
                allTasksTerminated = allTasksTerminated && task.terminated;

            // Increment currentCycle
            currentCycle++;
        }
    }

    // Method to simulate the banker resource alloc.
    public static void bankerAlloc() {

    }

    // Method to terminate or abort tasks and put released resources in limbo
    public static void kill(Task task, String mode) {
        task.currentActivity++;

        task.using = false;

        // Terminate or abort the task as per given mode
        if (mode.equals("terminate"))
            task.terminated = true;
        else if (mode.equals("abort"))
            task.aborted = true;
        else {
            System.out.println("Given mode not supported for task termination.");
            System.exit(1);
        }

        // Release task's resources and put them in limbo to be made available next cycle
        for (int i = 0; i < task.resourcesHeld.length; i++) {
            Banker.limboResources[i] += task.resourcesHeld[i];
            task.resourcesHeld[i] = 0;
        }
    }

    // Method to make the resources in limbo available
    public static void makeAvailable() {
        for (int i = 0; i < Banker.limboResources; i++) {
            Banker.availResources[i] += Banker.limboResources[i];
            Banker.limboResources[i] = 0;
        }
    }

    // Method to check for deadlocks
    public static int checkDeadlocks(ArrayList<Task> waitingTasks) {
        // Var. to track minimum numbered task for future abortion
        int minTask = Banker.tasks.length;

        for (Task task: waitingTasks) {
            // Track minimum numbered task for future abortion
            if (task.name < minTask)
                minTask = task.name;

            // Check for ability to satisfy pending requests
            if (task.initiated && !task.terminated && task.waiting)
                if (task.activityInfo.get(task.currentActivity) <= Banker.availResources[task.resourceTypes.get(task.currentActivity)])
                    return Banker.tasks.length;
        }

        return minTask;
    }

    // Method to check for errors in resource claims and requests
    public static boolean checkErrs(Task task, String mode) {
        // Check for initial claim err.
        if (mode.equals("claim")) {
            if (task.activities[task.currentActivity].equals("initiate") && (task.activityInfo.get(task.currentActivity) > Banker.totResources[task.resourceTypes[task.currentActivity]])) {
                kill(task, "abort");
                System.out.println("Task " + task.name + "'s initial claim was larger than available resources. Task aborted.");

                return true;
            }
        }
        // Check for request errs.
        else if (mode.equals("request")) {
            if (task.activities[task.currentActivity].equals("request") && (task.activityInfo.get(task.currentActivity) > task.claims[task.resourceTypes[task.currentActivity]])) {
                kill(task, "abort");
                System.out.println("Task " + task.name + "'s request was larger than the initial claim. Task aborted.");

                return true;
            }
        }
        else {
            System.out.println("Given mode not supported for error check.");
            System.exit(1);
        }

        return false;
    }

    // Method to increment tasks' time vars.
    public static void incrementTime() {
        for (Task task: Banker.tasks)
            if (task.initialized && !task.terminated && !task.aborted) {
                if (task.waiting)
                    task.waitTime++;

                task.totTime++;
            }
    }

    // Method to release tasks' held resources and put them in limbo to be made available next cycle
    public static void release(Task task) {
        task.currentActivity++;

        task.using = false;
        
        for (int i = 0; i < task.resourcesHeld.length; i++) {
            Banker.limboResources[i] += task.resourcesHeld[i];
            task.resourcesHeld[i] = 0;
        }
    }
}

class Task {
    // Instance vars.
    int name;

    ArrayList<String> activities = new ArrayList<>();
    ArrayList<Integer> delays = new ArrayList<>();
    ArrayList<Integer> resourceTypes = new ArrayList<>();
    ArrayList<Integer> activityInfo = new ArrayList<>();

    ArrayList<Integer> cycles = new ArrayList<>();

    int currentActivity = 0;

    boolean initiated = false;

    boolean waiting = false;
    boolean using = false;

    boolean terminated = false;

    boolean aborted = false;

    int[] resourcesHeld;
    int[] claims;

    int totTime = 0;
    int waitTime = 0;

    // Constructor
    public Task(int name, int totResources){
        this.name = name;

        this.resourcesHeld = new int[totResources];
        for (int i = 0; i < this.resourcesHeld.length; i++)
            this.resourcesHeld[i] = 0;

        this.claims = new int[totResources];
        for (int i = 0; i < this.claims.length; i++)
            this.claims[i] = 0;
    }
}