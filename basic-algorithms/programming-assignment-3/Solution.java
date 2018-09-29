import java.io.*;
import java.util.*;

public class Solution {
    public static void main(String[] args) {
       Scanner input = new Scanner(System.in);

       Heap heap = new Heap();

       int tot_entries = input.nextInt();
       for (int i = 0; i < tot_entries; i++) {
           String name = input.next();
           int value = input.nextInt();
           
           heap.insert(name, value);
       }

       int tot_queries = input.nextInt();
       for (int i = 0; i < tot_queries; i++) {
           int type = input.nextInt();

           if (type == 1) {
               String name = input.next();
               int valueToAdd = input.nextInt();

               heap.improveValue(name, valueToAdd);
           }
           else {
               int evaluationValue = input.nextInt();

               System.out.println(heap.evaluate(evaluationValue));
           }
       }
    }
}

class Heap {
    HashMap<String, Integer> map;
    ArrayList<String> tree;

    public Heap() {
        map = new HashMap<>();
        tree = new ArrayList<>();
    }

    public void insert(String key, int value) {
        map.put(key, value);
        tree.add(key);

        int index = tree.size();
        int parent_index = index/2;

        while (map.get(key) < map.get(tree.get(parent_index))) {
            String temp = tree.get(index - 1);
            tree.set(index - 1, tree.get(parent_index - 1));
            tree.set(parent_index - 1, temp);

            index = parent_index;
            parent_index = index/2;
        }
    }

    public void deleteMin() {
        map.remove(tree.get(0));
        tree.remove(0);

        tree.add(0, tree.get(tree.size() - 1));
        tree.remove(tree.size() - 1);

        int index = 1;
        int leftChildIndex = 2*index;
        int rightChildIndex = 2*index + 1;

        int leftDiff = map.get(tree.get(index - 1)) - map.get(tree.get(leftChildIndex - 1));
        int rightDiff = map.get(tree.get(index - 1)) - map.get(tree.get(rightChildIndex - 1));

        while (leftDiff > 0 || rightDiff > 0) {
            if (leftDiff > rightDiff) {
                String temp = tree.get(index - 1);
                tree.set(index - 1, tree.get(leftChildIndex - 1));
                tree.set(leftChildIndex - 1, temp);

                index = leftChildIndex;
                leftChildIndex = 2*index;
                rightChildIndex = 2*index + 1;

                leftDiff = map.get(tree.get(index - 1)) - map.get(tree.get(leftChildIndex - 1));
                rightDiff = map.get(tree.get(index - 1)) - map.get(tree.get(rightChildIndex - 1));
            }
            else {
                String temp = tree.get(index - 1);
                tree.set(index - 1, tree.get(rightChildIndex  -1));
                tree.set(rightChildIndex - 1, temp);

                index = rightChildIndex;
                leftChildIndex = 2*index;
                rightChildIndex = 2*index + 1;

                leftDiff = map.get(tree.get(index - 1)) - map.get(tree.get(leftChildIndex - 1));
                rightDiff = map.get(tree.get(index - 1)) - map.get(tree.get(rightChildIndex - 1));
            }
        }
    }

    public void deleteLessThan(int value) {
        while (map.get(tree.get(0)) < value) {
            this.deleteMin();
        }
    }

    public void improveValue(String key, int value) {
        map.put(key, map.get(key) + value);
    }

    public int evaluate(int value) {
        this.deleteLessThan(value);

        return tree.size();
    }
}