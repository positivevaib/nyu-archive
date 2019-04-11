import java.io.*;
import java.lang.*;
import java.util.*;


// Define main program class
public class Banker {
    // Declare class variables to track tasks, total resources, available resources and resources released but not yet available (in limbo)
    static Task[] tasks;

    static int[] totResources;
    static int[] availResources;
    static int[] limboResources;


    // Define main method
    public static void main(String[] args) {
        System.out.println();

        // Check command line argument
        if (args.length != 1) {
            System.out.println("No input filename provided.");
            System.exit(1);
        }

        // Read input
        try {
            // Create Scanner object
            Scanner fileIn = new Scanner(new File(args[0]));

            // Read and store values
            while (fileIn.hasNext()) {
                // Read total number of tasks
                Banker.tasks = new Task[fileIn.nextInt()];

                // Read and store total number of resource types
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

                // Declare and initialize task activity info. variables
                String activity = "";
                int taskName = 0;

                // Read and store tasks' activities info.
                while (fileIn.hasNext()) {
                    activity = fileIn.next();
                    taskName = fileIn.nextInt();
                    Banker.tasks[taskName - 1].activities.add(activity);
                    Banker.tasks[taskName - 1].delays.add(fileIn.nextInt());
                    Banker.tasks[taskName - 1].resourceTypes.add(fileIn.nextInt());
                    Banker.tasks[taskName - 1].activityInfo.add(fileIn.nextInt());
                }
            }

            // Update tasks' cycles and claims arrays
            for (Task task: Banker.tasks) {
                task.cycles.add(task.delays.get(0));

                task.fillClaims();
            }

            // Close Scanner object
            fileIn.close();
        }
        catch (FileNotFoundException e) {
            System.out.println(args[0] + " file not found.");
            System.exit(1);
        }

        // Simulate optimistic resource allocator and print results
        fifoAlloc();
        printOut();
        
        // Reset tasks, simulate banker's resource allocator and print results
        for (Task task: Banker.tasks)
            task.reset();

        bankerAlloc();
        printOut();
    }


    // Define method to simulate optimistic resource allocator
    public static void fifoAlloc() {
        System.out.println("FIFO");

        // Declare and instantiate ArrayLists to track tasks currently computing or waiting for resources
        ArrayList<Task> computingTasks = new ArrayList<>();
        ArrayList<Task> waitingTasks = new ArrayList<>();

        // Declare and initialize current cycle
        int currentCycle = 0;
        
        // Simulate runtime
        while (!checkTermination()) {
            // Make resources in limbo available
            makeAvailable();
            
            // Terminate tasks
            for (Task task: Banker.tasks)
                if (!task.terminated && !task.aborted 
                && task.activities.get(task.currentActivity).equals("terminate") 
                && task.cycles.get(task.currentActivity) == currentCycle)
                    kill(task, "terminate");

            // Initiate tasks
            for (Task task: Banker.tasks)
                if (!task.terminated && !task.aborted 
                && task.activities.get(task.currentActivity).equals("initiate") 
                && task.cycles.get(task.currentActivity) == currentCycle)
                    task.initiate(currentCycle);

            // Process pending requests
            for (Task task: waitingTasks) {
                // Check for resource availability and grant request, if applicable
                if (checkAvail(task)) {
                    grant(task, currentCycle);
                    computingTasks.add(task);
                }
            }

            // Remove tasks from waitingTasks array if pending request granted
            for (Task task: computingTasks)
                if (waitingTasks.contains(task))
                    waitingTasks.remove(waitingTasks.indexOf(task));

            // Process tasks' activities
            for (Task task: Banker.tasks) {
                if (task.initiated && !task.terminated && !task.aborted) {
                    // Process requests
                    if (task.activities.get(task.currentActivity).equals("request") 
                    && task.cycles.get(task.currentActivity) <= currentCycle) {
                        // If task computing, change its state and remove from computingTasks array
                        if (task.computing) {
                            task.computing = false;
                            computingTasks.remove(computingTasks.indexOf(task));
                        }
                        
                        if (!task.waiting) {
                            // Grant request if resources available, else block
                            if (checkAvail(task)) {
                                grant(task, currentCycle);
                                computingTasks.add(task);
                            }
                            else {
                                task.waiting = true;
                                waitingTasks.add(task);
                            }
                        }
                    }
                    // Process releases and remove task from computingTasks array if no resources held
                    else if (task.activities.get(task.currentActivity).equals("release") 
                    && task.cycles.get(task.currentActivity) == currentCycle) {
                        release(task, currentCycle);

                        int totResourcesHeld = 0;
                        for (int i = 0; i < task.resourcesHeld.length; i++)
                            totResourcesHeld += task.resourcesHeld[i];

                        if (totResourcesHeld == 0)
                            computingTasks.remove(computingTasks.indexOf(task));
                    }
                }
            }

            // Check for deadlocks
            while (computingTasks.isEmpty() && !waitingTasks.isEmpty()) {
                Task minTask = checkDeadlocks(waitingTasks);
                if (minTask != null) {
                    System.out.println("Deadlock detected. Aborting task " + (minTask.name + 1));

                    kill(minTask, "abort");
                    waitingTasks.remove(waitingTasks.indexOf(minTask));
                }
                else    
                    break;
            }

            // Increment tasks' time
            for (Task task: Banker.tasks)
                task.incrementTime();

            // Increment currentCycle
            currentCycle++;
        }
    }


    // Define method to simulate banker's resource allocator
    public static void bankerAlloc() {
        System.out.println("BANKER'S");

        // Declare and instantiate ArrayLists to track tasks currently computing or waiting for resources
        ArrayList<Task> computingTasks = new ArrayList<>();
        ArrayList<Task> waitingTasks = new ArrayList<>();

        // Check initial claim errors
        for (Task task: Banker.tasks)
            checkErrors(task, "claim");

        // Declare and initialize current cycle
        int currentCycle = 0;
        
        // Simulate runtime
        while (!checkTermination()) {
            // Make resources in limbo available
            makeAvailable();
            
            // Terminate tasks
            for (Task task: Banker.tasks)
                if (!task.terminated && !task.aborted 
                && task.activities.get(task.currentActivity).equals("terminate") 
                && task.cycles.get(task.currentActivity) == currentCycle)
                    kill(task, "terminate");

            // Initiate tasks
            for (Task task: Banker.tasks)
                if (!task.terminated && !task.aborted 
                && task.activities.get(task.currentActivity).equals("initiate") 
                && task.cycles.get(task.currentActivity) == currentCycle)
                    task.initiate(currentCycle);

            // Process pending requests
            for (Task task: waitingTasks) {
                // Check for safety and resource availability and grant request, if applicable
                if (checkAvail(task) && checkSafety(task)) {
                    grant(task, currentCycle);
                    computingTasks.add(task);
                }
            }

            // Remove tasks from waitingTasks array if pending request granted
            for (Task task: computingTasks)
                if (waitingTasks.contains(task))
                    waitingTasks.remove(waitingTasks.indexOf(task));

            // Process tasks' activities
            for (Task task: Banker.tasks) {
                if (task.initiated && !task.terminated && !task.aborted) {
                    // Process requests
                    if (task.activities.get(task.currentActivity).equals("request") 
                    && task.cycles.get(task.currentActivity) <= currentCycle) {
                        // Check greater than claim request error
                        if (checkErrors(task, "request"))
                            continue;

                        // If task computing, change its state and remove from computingTasks array 
                        if (task.computing) {
                            task.computing = false;
                            computingTasks.remove(computingTasks.indexOf(task));
                        }
                        
                        if (!task.waiting) {
                            // Grant request if its a safe state and if resources available, else block
                            if (checkAvail(task) && checkSafety(task)) {
                                grant(task, currentCycle);
                                computingTasks.add(task);
                            }
                            else {
                                task.waiting = true;
                                waitingTasks.add(task);
                            }
                        }
                    }
                    // Process releases and remove task from computingTasks array if no resources held
                    else if (task.activities.get(task.currentActivity).equals("release") 
                    && task.cycles.get(task.currentActivity) == currentCycle) {
                        release(task, currentCycle);

                        int totResourcesHeld = 0;
                        for (int i = 0; i < task.resourcesHeld.length; i++)
                            totResourcesHeld += task.resourcesHeld[i];

                        if (totResourcesHeld == 0)
                            computingTasks.remove(computingTasks.indexOf(task));
                    }
                }
            }

            // Increment tasks' time
            for (Task task: Banker.tasks)
                task.incrementTime();

            // Increment currentCycle
            currentCycle++;
        }
    }


    // Define method to check for resource availability to satisfy request
    public static boolean checkAvail(Task task) {
        if (task.activityInfo.get(task.currentActivity) > Banker.availResources[task.resourceTypes.get(task.currentActivity) - 1])
            return false;
        
        return true;
    }


    // Define method to check for deadlocks
    public static Task checkDeadlocks(ArrayList<Task> waitingTasks) {
        // Variable to track minimum numbered task for future abortion
        Task minTask = null;

        for (Task task: waitingTasks) {
            // Track minimum numbered task for future abortion
            if (minTask == null || task.name < minTask.name)
                minTask = task;

            // Check for ability to satisfy pending requests
            if (task.initiated && !task.terminated && task.waiting)
                if (task.activityInfo.get(task.currentActivity) 
                <= (Banker.availResources[task.resourceTypes.get(task.currentActivity) - 1] 
                    + Banker.limboResources[task.resourceTypes.get(task.currentActivity) - 1]))
                    return null;
        }

        return minTask;
    }

    
    // Define method to check for errors in resource claims and requests
    public static boolean checkErrors(Task task, String mode) {
        // Check for initial claim error
        if (mode.equals("claim")) {
            for (int i = 0; i < task.activities.size(); i++)
                if (task.activities.get(i).equals("initiate") && (task.activityInfo.get(i) > Banker.totResources[task.resourceTypes.get(i) - 1])) {
                    kill(task, "abort");

                    System.out.println("Task " + (task.name + 1) + "'s initial claim was larger than available resources. Task aborted before run begins.");
                    System.out.println("Claim for resource " + task.resourceTypes.get(i) 
                    + " (" + task.activityInfo.get(i) + ") was higher than the amount present (" + Banker.totResources[task.resourceTypes.get(i) - 1] + ").");

                    return true;
                }
        }
        // Check for requests exceeding claims errors
        else if (mode.equals("request")) {
            if ((task.activityInfo.get(task.currentActivity) + task.resourcesHeld[task.resourceTypes.get(task.currentActivity) - 1]) 
            > task.claims[task.resourceTypes.get(task.currentActivity) - 1]) {
                kill(task, "abort");
                System.out.println("Task " + (task.name + 1) + "'s request exceeded the initial claim. Task aborted.");

                return true;
            }
        }
        else {
            System.out.println("Given mode not supported for error check.");
            System.exit(1);
        }

        return false;
    }


    // Define method to check for safe states
    public static boolean checkSafety(Task task) {
        // Copy available resources for safety check
        int[] availResourcesCopy = new int[Banker.availResources.length];
        for (int i = 0; i < availResourcesCopy.length; i++)
            availResourcesCopy[i] = Banker.availResources[i];

        // Assign task its requested resources, making them unavailable
        availResourcesCopy[task.resourceTypes.get(task.currentActivity) - 1] -= task.activityInfo.get(task.currentActivity);

        // Declare and initialize array to track safe tasks
        boolean[] safeTasks = new boolean[Banker.tasks.length];
        for (int i = 0; i < safeTasks.length; i++) {
            if (Banker.tasks[i].terminated || Banker.tasks[i].aborted)
                safeTasks[i] = true;
            else
                safeTasks[i] = false;
        }

        // Loop until there are no tasks to be considered for safety check or until no safe task can be found in an iteration
        while (!abortSafetyCheck(safeTasks)) {
            // Boolean flag to mark if task terminated in safety simulation
            boolean terminated = false;
            // Loop over all tasks to find one that can be satisfied to its max claims, if not already considered before
            for (int i = 0; i < Banker.tasks.length; i++) {
                if (!safeTasks[i]) {
                    // Boolean flag to mark if all resource claims can be satisfied for chosen task
                    boolean allResourcesSafe = true;
                    // Loop over all resources and try to satisfy max claims for chosen task
                    for (int j = 0; j < availResourcesCopy.length; j++) {
                        // If resource is the one requested in current cycle
                        if ((j == (task.resourceTypes.get(task.currentActivity) - 1))) { 
                            if (((Banker.tasks[i] == task) 
                            && ((Banker.tasks[i].claims[j] - Banker.tasks[i].resourcesHeld[j] - task.activityInfo.get(task.currentActivity)) 
                            > availResourcesCopy[j])) 
                            || ((Banker.tasks[i] != task) && ((Banker.tasks[i].claims[j] - Banker.tasks[i].resourcesHeld[j]) > availResourcesCopy[j]))) {
                                allResourcesSafe = false;
                                break;
                            }
                        }
                        // If resource is not the one requested in current cycle
                        else
                            if ((Banker.tasks[i].claims[j] - Banker.tasks[i].resourcesHeld[j]) > availResourcesCopy[j]) {
                                allResourcesSafe = false;
                                break;
                            }
                    }
                    
                    // If chosen task's max claims can be satisfied, make its held resources available, terminating the task
                    if (allResourcesSafe) {
                        safeTasks[i] = true;
                        for (int j = 0; j < availResourcesCopy.length; j++) {
                            if ((j == (task.resourceTypes.get(task.currentActivity) - 1)) && Banker.tasks[i] == task)
                                availResourcesCopy[j] += Banker.tasks[i].resourcesHeld[j] + task.activityInfo.get(task.currentActivity);
                            else
                                availResourcesCopy[j] += Banker.tasks[i].resourcesHeld[j];
                        }

                        terminated = true;
                        break;
                    }
                }
            }

            // Return false to indicate unsafe state if no safe task can be found in the iteration
            if (!terminated)
                return false;
        }

        // Return true if all tasks terminated, thereby, declaring the state to be safe
        return true;
    }

    // Define method to decide about further checking safety
    public static boolean abortSafetyCheck(boolean[] safeTasks) {
        for (boolean safety: safeTasks)
            if (!safety)
                return false;

        return true;
    }


    // Define method to check if all tasks terminated or aborted
    public static boolean checkTermination() {
        for (Task task: Banker.tasks)
            if (!task.terminated && !task.aborted)
                return false;
        
        return true;
    }


    // Define method to grant tasks requested resources
    public static void grant(Task task, int currentCycle) {
        task.resourcesHeld[task.resourceTypes.get(task.currentActivity) - 1] += task.activityInfo.get(task.currentActivity);
        Banker.availResources[task.resourceTypes.get(task.currentActivity) - 1] -= task.activityInfo.get(task.currentActivity);

        task.waiting = false;
        task.computing = true;

        task.currentActivity++;
        task.updateCycles(currentCycle);
    }


    // Define method to increment tasks' time variables
    public static void incrementTime() {
        for (Task task: Banker.tasks)
            task.incrementTime();
    }


    // Define method to terminate or abort tasks and put released resources in limbo
    public static void kill(Task task, String mode) {
        task.currentActivity++;

        // Terminate or abort task as per given mode
        if (mode.equals("terminate"))
            task.terminated = true;
        else if (mode.equals("abort")) {
            task.waiting = false;
            task.aborted = true;
        }
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


    // Define mthod to make resources in limbo available
    public static void makeAvailable() {
        for (int i = 0; i < Banker.limboResources.length; i++) {
            Banker.availResources[i] += Banker.limboResources[i];
            Banker.limboResources[i] = 0;
        }
    }


    // Define method to print output
    public static void printOut() {
        // Declare and initialize variables to track total values
        int totTime = 0;
        int waitTime = 0;

        // Print summary for each task
        for (Task task: Banker.tasks) {
            if (task.aborted)
                System.out.printf("Task %d %15s\n", (task.name + 1), "aborted");
            else {
                totTime += task.totTime;
                waitTime += task.waitTime;

                System.out.printf("Task %d %9d %d %d%%\n", (task.name + 1), task.totTime, task.waitTime, Math.round((task.waitTime * 100.0) / task.totTime));
            }
        }

        // Print overall summary
        System.out.printf("Total %10d %d %d%%\n\n", totTime, waitTime, Math.round((waitTime * 100.0) / totTime));
    }


    // Define method to release tasks' held resources and put them in limbo to be made available next cycle
    public static void release(Task task, int currentCycle) {
        Banker.limboResources[task.resourceTypes.get(task.currentActivity) - 1] += task.activityInfo.get(task.currentActivity);
        task.resourcesHeld[task.resourceTypes.get(task.currentActivity) - 1] -= task.activityInfo.get(task.currentActivity);

        task.computing = false;

        task.currentActivity++;
        task.updateCycles(currentCycle);
    }
}



// Define Task class
class Task {
    // Instance variables
    // Task number
    int name;

    // ArrayLists to track task's activities through time related by index
    ArrayList<String> activities = new ArrayList<>();
    ArrayList<Integer> delays = new ArrayList<>();
    ArrayList<Integer> resourceTypes = new ArrayList<>();
    ArrayList<Integer> activityInfo = new ArrayList<>();

    // ArrayList to track the cycles when task's activities first come into effect, given delays and pervious activities
    ArrayList<Integer> cycles = new ArrayList<>();

    // Iterator to track next activity to be performed
    int currentActivity = 0;

    // Boolean flags to track task's states
    boolean initiated = false;

    boolean computing = false;
    boolean waiting = false;

    boolean terminated = false;

    boolean aborted = false;

    // Arrays to track task's initial claims and current resources held
    int[] claims;
    int[] resourcesHeld;

    // Variables to track time spent initialized and blocked
    int totTime = 0;
    int waitTime = 0;


    // Define constructor
    public Task(int name, int totResources){
        this.name = name;

        this.resourcesHeld = new int[totResources];
        for (int i = 0; i < this.resourcesHeld.length; i++)
            this.resourcesHeld[i] = 0;

        this.claims = new int[totResources];
        for (int i = 0; i < this.claims.length; i++)
            this.claims[i] = 0;
    }


    // Define method to fill claims array
    public void fillClaims() {
        for (int i = 0; i < this.activities.size(); i++)
            if (this.activities.get(i).equals("initiate"))
                this.claims[this.resourceTypes.get(i) - 1] += this.activityInfo.get(i);
    }


    // Define method to increment time
    public void incrementTime() {
        if (this.initiated && !this.terminated && !this.aborted) {
            if (this.waiting)
                this.waitTime++;

            this.totTime++;
        }
    }


    // Define method to initiate task
    public void initiate(int currentCycle) {
        this.initiated = true;

        this.currentActivity++;
        this.updateCycles(currentCycle);
    }


    // Define method to reset instance variables for another simulation
    public void reset() {
        this.cycles.clear();
        this.cycles.add(this.delays.get(0));

        this.currentActivity = 0;

        this.initiated = false;

        this.computing = false;
        this.waiting = false;

        this.terminated = false;

        this.aborted = false;

        for (int i = 0; i < this.resourcesHeld.length; i++)
            this.resourcesHeld[i] = 0;

        this.totTime = 0;
        this.waitTime = 0;
    }


    // Define method to update next activity's cycle correspondence
    public void updateCycles(int currentCycle) {
        this.cycles.add(currentCycle + this.delays.get(this.currentActivity) + 1);
    }
}