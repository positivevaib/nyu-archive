import java.io.*;
import java.util.*;

public class Solution {
    public static void main(String[] args) {
       Scanner input = new Scanner(System.in);

       Heap heap = new Heap();

       long tot_entries = input.nextLong();
       for (long i = 0; i < tot_entries; i++) {
           String name = input.next();
           long value = input.nextLong();
           
           heap.insert(name, value);
       }

       long tot_queries = input.nextLong();
       for (long i = 0; i < tot_queries; i++) {
           long type = input.nextLong();

           if (type == 1) {
               String name = input.next();
               long valueToAdd = input.nextLong();

               heap.improveValue(name, valueToAdd);
           }
           else {
               long evaluationValue = input.nextLong();

               System.out.println(heap.evaluate(evaluationValue));
           }
       }
    }
}

class Heap {
    HashMap<String, Long> map;
    ArrayList<String> tree;

    public Heap() {
        map = new HashMap<>();
        tree = new ArrayList<>();
    }

    public void floatUp(int index) {
        int parent_index = index/2;

        if (parent_index != 0)
            while ((parent_index != 0) && (map.get(tree.get(index - 1)) < map.get(tree.get(parent_index - 1)))) {
                String temp = tree.get(index - 1);
                tree.set(index - 1, tree.get(parent_index - 1));
                tree.set(parent_index - 1, temp);

                index = parent_index;
                parent_index = index/2;
            }
    }

    public void sinkDown(int index) {
        int leftChildIndex = 2*index;
        int rightChildIndex = 2*index + 1;
        long leftDiff;
        try {
            leftDiff = map.get(tree.get(index - 1)) - map.get(tree.get(leftChildIndex - 1));
        }
        catch (Exception e) {
            leftDiff = Long.MIN_VALUE;
        }
        
        long rightDiff;
        try {
            rightDiff = map.get(tree.get(index - 1)) - map.get(tree.get(rightChildIndex - 1));
        }
        catch (Exception e) {
            rightDiff = Long.MIN_VALUE;
        }

        while (leftDiff > 0 || rightDiff > 0) {
            if (leftDiff > rightDiff) {
                String temp = tree.get(index - 1);
                tree.set(index - 1, tree.get(leftChildIndex - 1));
                tree.set(leftChildIndex - 1, temp);

                index = leftChildIndex;
                leftChildIndex = 2*index;
                rightChildIndex = 2*index + 1;

                try {
                    leftDiff = map.get(tree.get(index - 1)) - map.get(tree.get(leftChildIndex - 1));
                }
                catch (Exception e) {
                    leftDiff = Long.MIN_VALUE;
                }

                try {
                    rightDiff = map.get(tree.get(index - 1)) - map.get(tree.get(rightChildIndex - 1));
                }
                catch (Exception e) {
                    rightDiff = Long.MIN_VALUE;
                }
            }
            else {
                String temp = tree.get(index - 1);
                tree.set(index - 1, tree.get(rightChildIndex  -1));
                tree.set(rightChildIndex - 1, temp);

                index = rightChildIndex;
                leftChildIndex = 2*index;
                rightChildIndex = 2*index + 1;

                try {
                    leftDiff = map.get(tree.get(index - 1)) - map.get(tree.get(leftChildIndex - 1));
                }
                catch (Exception e) {
                    leftDiff = Long.MIN_VALUE;
                }

                try {
                    rightDiff = map.get(tree.get(index - 1)) - map.get(tree.get(rightChildIndex - 1));
                }
               catch (Exception e) {
                   rightDiff = Long.MIN_VALUE;
               }
            }
        }
    }
    
    public void insert(String key, long value) {
        map.put(key, value);
        tree.add(key);

        int index = tree.size();
        this.floatUp(index);
    }

    public void deleteMin() {
        if (tree.size() == 0)
            return;

        map.remove(tree.get(0));
        tree.remove(0);

        if (tree.size() <= 1)
            return;
        
        tree.add(0, tree.get(tree.size() - 1));
        tree.remove(tree.size() - 1);

        int index = 1;
        this.sinkDown(index);
    }

    public void deleteLessThan(long value) {
        while (map.get(tree.get(0)) < value) {
            this.deleteMin();
        }
    }

    public void improveValue(String key, long value) {
        map.put(key, map.get(key) + value);

        int index = tree.indexOf(key) + 1;
        sinkDown(index);
    }

    public long evaluate(long value) {
        this.deleteLessThan(value);

        return tree.size();
    }
}