import java.io.*;
import java.util.*;

// Linker class
public class Linker {
    // Class variables
    static int Size = 300;

    static Module[] modules = null;

    static ArrayList<Integer> memMap = new ArrayList<>();
    static ArrayList<Item> symbolList = new ArrayList<>();
    
    public static void main(String[] args) {
        
    }

    // Method to perform linker's first pass
    public static void firstPass() {
        // Instantiate Scanner
        Scanner in = new Scanner(System.in);

        // Get total number of modules
        int totModules = in.nextInt();

        // Instantiate modules array
        Linker.modules = new Module[totModules];
        for (int i = 0; i < totModules; i++)
            Linker.modules[i] = null;

        // Current base address
        int baseAdd = 0;

        // Read data
        for (int mod = 0; i < totModules; i++) {
            // Instantiate and save Module
            Module module = new Module(baseAdd);
            Linker.modules[mod] = module;

            // Read definition list
            module.totDefs = in.nextInt();
            if (module.totDefs != 0)
                for (int def = 0; def < module.totDefs; def++) {
                    // Read, instantiate and save symbols
                    String name = in.next();
                    int add = in.nextInt();

                    module.
                }
        }
    }
}

// Module class
class Module {
    // Instance variables
    int baseAdd = 0;

    int totDefs = 0;
    int totUses = 0;
    int totWords = 0;

    
    Item[] words = null;

    // Constructor
    public Module(int baseAdd) {
        this.baseAdd = baseAdd;
    }
}

// Item class
class Item {
    // Instance variables
    String name = null;
    int val = 0;

    // Constructor
    public Item(String name, int val) {
        // Initialize instance variables
        this.name = name;
        this.val = val;
    }
}