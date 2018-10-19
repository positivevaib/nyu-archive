import java.io.*;
import java.util.*;

public class Solution {
    static Graph G;
    static char[] colorList;
    static int[] parentList;
    static int loopStart;
    static int loopEnd;

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        int totVertices = input.nextInt();
        int totEdges = input.nextInt();

        Solution.G = new Graph(totVertices);
        for (int i = 0; i < totEdges; i++) {
            int from = input.nextInt();
            int to = input.nextInt();

            Solution.G.addEdge(from, to);
        }

        for (int i = 0; i < G.edgeList.length; i++) {
            System.out.print("\n"+ (i + 1) +" --> ");
            Edge edge = G.edgeList[i];
            if (edge != null) {
            while (edge.next != null) {
                System.out.print(edge.to + " ");
                edge = edge.next;
            }
            System.out.print(edge.to + " ");         }
        }

        Solution.DFS();

        if (Solution.loopStart == -1)
            System.out.println(0);
        else {
            System.out.println(1);
            
            ArrayList<Integer> loopVertices = new ArrayList<>();
            System.out.println(loopStart + " " + loopEnd);
            while (Solution.loopEnd != Solution.loopStart) {
                loopVertices.add(Solution.loopEnd);
                System.out.println(parentList[loopEnd - 1]);
                Solution.loopEnd = Solution.parentList[Solution.loopEnd - 1];
            }
            loopVertices.add(Solution.loopEnd);

            Collections.sort(loopVertices);

            for (int i = 0; i < loopVertices.size(); i++)
                System.out.print(loopVertices.get(i) + " ");
        }
    }

    static void DFS() {
        Solution.colorList = new char[Solution.G.edgeList.length];
        Solution.parentList = new int[Solution.G.edgeList.length];

        Solution.loopStart = -1;
        Solution.loopEnd = -1;

        for (int i = 0; i < Solution.G.edgeList.length; i++) {
            Solution.colorList[i] = 'w';
            Solution.parentList[i] = 0;
        }

        boolean recurse = true;
        for (int i = 0; i < Solution.G.edgeList.length; i++) {
            if (Solution.colorList[i] == 'w' && recurse)
                recurse = Solution.recDFS(i + 1);
        }
    }

    static boolean recDFS(int vertex) {
        Solution.colorList[vertex - 1] = 'g';
        boolean recurse = true;
        Edge edge = Solution.G.edgeList[vertex - 1];
        while (edge != null) {
            if (Solution.colorList[edge.to - 1] == 'w') {
                Solution.parentList[edge.to - 1] = vertex;
                recurse = Solution.recDFS(edge.to);
            }
            else if (Solution.colorList[edge.to - 1] == 'g') {
                Solution.loopStart = edge.to;
                Solution.loopEnd = vertex;

                return false;
            }
            edge = edge.next;
        }

        Solution.colorList[vertex - 1] = 'b';
        return recurse;
    }
}

class Graph {
    Edge[] edgeList;

    Graph(int totVertices) {
        this.edgeList = new Edge[totVertices];
        for (int i = 0; i < this.edgeList.length; i++)
            this.edgeList[i] = null;
    }

    void addEdge(int from, int to) {
        this.edgeList[from - 1] = new Edge(to, this.edgeList[from - 1]);
    }
}

class Edge {
    int to;
    Edge next;

    Edge(int to, Edge next) {
        this.to = to;
        this.next = next;
    }
}