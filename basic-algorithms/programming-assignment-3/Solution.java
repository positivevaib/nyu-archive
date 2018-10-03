import java.io.*;
import java.util.*;

public class Solution {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        int totalEntries = input.nextInt();
        Heap heap = new Heap(totalEntries);

        for (long i = 0; i < totalEntries; i++) {
            String name = input.next();
            long value = input.nextLong();
            
            heap.insert(name, value);
        }

        int totalQueries = input.nextInt();
        for (long i = 0; i < totalQueries; i++) {
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
    String[] tree;
    int size;

    HashMap<String, Long> valueMap;
    HashMap<String, Integer> indexMap;

    public Heap(int size) {
        this.tree = new String[size];
        this.size = 0;

        this.valueMap = new HashMap<>();
        this.indexMap = new HashMap<>();
    }

    public void swap(int index1, int index2) {
        String temp = this.tree[index1];

        this.tree[index1] = this.tree[index2];
        this.indexMap.put(this.tree[index1], index1);

        this.tree[index2] = temp;
        this.indexMap.put(this.tree[index2], index2);
    }

    public void floatUp(int position) {
        while (position > 1) {
            int parentPosition = position/2;

            if (this.valueMap.get(this.tree[parentPosition - 1]) < this.valueMap.get(this.tree[position - 1]))
                break;

            this.swap(position - 1, parentPosition - 1);

            position = parentPosition;
        }
    }

    public void sinkDown(int position) {
        int leftChildPosition = 2*position;

        while (leftChildPosition <= this.size) {
            int swapPosition = leftChildPosition;

            int rightChildPosition = 2*position + 1;
            if (rightChildPosition <= this.size && this.valueMap.get(this.tree[rightChildPosition - 1]) < this.valueMap.get(this.tree[leftChildPosition - 1]))
                swapPosition = rightChildPosition;

            if (this.valueMap.get(this.tree[position - 1]) > this.valueMap.get(this.tree[swapPosition - 1]))
                this.swap(position - 1, swapPosition - 1);

            position = swapPosition;
            leftChildPosition = 2*position;
        }
    }
    
    public void insert(String key, long value) {
        this.valueMap.put(key, value);

        this.tree[this.size] = key;
        this.indexMap.put(key, this.size);

        this.size++;

        this.floatUp(this.size - 1);
    }

    public void deleteMin() {
        this.swap(0, this.size - 1);

        this.size--;

        sinkDown(1);
    }

    public void deleteLessThan(long value) {
        while (this.valueMap.get(this.tree[0]) < value)
            this.deleteMin();
    }

    public void improveValue(String key, long value) {
        this.valueMap.put(key, this.valueMap.get(key) + value);

        int index = this.indexMap.get(key);
        sinkDown(index + 1);
    }

    public long evaluate(long value) {
        this.deleteLessThan(value);

        return this.size;
    }
}