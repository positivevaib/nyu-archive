import java.io.*;
import java.util.*;

// Linker class
public class Linker {
    // Class variables
    static int Size = 300;

    static Module[] modules = null;

    static ArrayList<Item> memMap = new ArrayList<>();
    static HashMap<String, Item> symbolList = new HashMap<>();

    static ArrayList<String> usedSymbolList = new ArrayList<>();
    static ArrayList<Integer> multiUseList = new ArrayList<>();
    
    public static void main(String[] args) {
        // Link program
        firstPass();
        secondPass();
        printOut();
    }

    // Method to perform Linker's first pass
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
        for (int mod = 0; mod < totModules; mod++) {
            // Instantiate and save Module
            Module module = new Module(baseAdd);
            Linker.modules[mod] = module;

            // Read definition list
            int totDefs = in.nextInt();

            module.defs = new Item[totDefs];
            for (int def = 0; def < totDefs; def++) {
                // Read and save symbols
                String name = in.next();
                int add = in.nextInt();

                Item symbol = new Item(name, add + baseAdd);
                symbol.defModule = mod;
                
                module.defs[def] = symbol;

                if (Linker.symbolList.containsKey(name))
                    symbol.error = "Error: This variable is multiply-defined. Last value used.";

                Linker.symbolList.put(name, symbol);
            }

            // Read use list
            int totUses = in.nextInt();

            module.uses = new HashMap<>();
            for (int use = 0; use < totUses; use++) {
                // Read and save symbol uses
                String symbol = in.next();
                int add = in.nextInt();

                if (module.uses.containsKey(add))
                    Linker.multiUseList.add(add + baseAdd);

                module.uses.put(add, symbol);

                Linker.usedSymbolList.add(symbol);
            }

            // Read program text
            int totWords = in.nextInt();

            module.words = new Item[totWords];
            for (int word = 0; word < totWords; word++) {
                // Read and save program text
                module.words[word] = new Item(in.next(), in.nextInt());

                if (Linker.multiUseList.contains(word + baseAdd))
                    module.words[word].error = "Error: Multiple symbols used here. Last one used.";
            }

            // Update baseAdd
            baseAdd += totWords;
        }
    }

    // Method to perform Linker's second pass
    public static void secondPass() {
        // Resolve relative and external addresses and store in memory map
        int totModules = Linker.modules.length;

        for (int mod = 0; mod < totModules; mod++) {
            Module module = Linker.modules[mod];

            // Resolve external addresses
            for (int add: module.uses.keySet()) {
                String symbol = module.uses.get(add);

                int pointer = module.words[add].val % 1000;

                boolean state = true;
                while (state) {
                    int symbolVal = 111;

                    if (!Linker.symbolList.containsKey(symbol))
                        module.words[add].error = "Error: " + symbol + " is not defined. 111 used.";
                    else   
                        symbolVal = Linker.symbolList.get(symbol).val;

                    module.words[add].val = ((module.words[add].val / 1000) * 1000) + symbolVal;

                    if (pointer == 777)
                        state = false;
                    else {
                        add = pointer;
                        pointer = module.words[add].val % 1000;
                    }
                }
            }

            // Check for and resolve errors
            for (int word = 0; word < module.words.length; word++) {
                if (module.words[word].name.equals("A") && (module.words[word].val % 1000) >= Linker.Size) {
                    module.words[word].val = ((module.words[word].val / 1000) * 1000) + (Linker.Size - 1);
                    module.words[word].error = "Error: A type address exceeds machine size. Max legal value used.";
                }
                else if (module.words[word].name.equals("R")) {
                    if ((module.words[word].val % 1000) >= module.words.length) {
                        module.words[word].val = module.baseAdd;
                        module.words[word].error = "Error: R type address exceeds module size. 0 (relative) used.";
                    }
                    else
                        module.words[word].val += module.baseAdd;
                }
                    
                Linker.memMap.add(module.words[word]);
            }
        }
    }

    // Method to print output
    public static void printOut() {
        // Print symbol list
        System.out.println("Symbol list:");
        for (String symbol: Linker.symbolList.keySet()) {
            if (Linker.symbolList.get(symbol).error != null)
                System.out.println(symbol + ": " + Linker.symbolList.get(symbol).val + " " + Linker.symbolList.get(symbol).error);
            else   
                System.out.println(symbol + ": " + Linker.symbolList.get(symbol).val);
        }

        // Print memory map
        System.out.println("\nMemory map:");
        for (int i = 0; i < Linker.memMap.size(); i++) {
            if (Linker.memMap.get(i).error != null)
                System.out.println(i + ": " + Linker.memMap.get(i).val + " " + Linker.memMap.get(i).error);
            else
                System.out.println(i + ": " + Linker.memMap.get(i).val);
        }

        // Print warnings
        boolean warnUsed = false;
        for (String symbol: Linker.symbolList.keySet()) {
            if (!Linker.usedSymbolList.contains(symbol)) {
                System.out.print("\nWarning: " + symbol + " was defined in module " + Linker.symbolList.get(symbol).defModule + " but never used.");
                warnUsed = true;
            }
        }

        if (warnUsed)
            System.out.println();

    }
}

// Module class
class Module {
    // Instance variables
    int baseAdd = 0;

    Item[] defs = null;
    HashMap<Integer, String> uses = null;
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

    String error = null;

    int defModule = 0;
    int totUses = 0;

    // Constructor
    public Item(String name, int val) {
        this.name = name;
        this.val = val;
    }
}