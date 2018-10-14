import java.io.*;
import java.util.*;

public class Solution {
    static Graph G;
    static char[] colorList;
    static int[] parentList;
    static int[] backEdges;

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

        Solution.DFS();
    }

    static void DFS() {
        Solution.colorList = new char[Solution.G.edgeList.length];
        Solution.parentList = new int[Solution.G.edgeList.length];
        for (int i = 0; i < Solution.G.edgeList.length; i++) {
            Solution.colorList[i] = 'w';
            Solution.parentList[i] = 0;
        }

        for (int i = 0; i < Solution.G.edgeList.length; i++) {
            if (Solution.colorList[i] == 'w')
                Solution.recDFS(i + 1);
        }
    }

    static void recDFS(int vertex) {
        Solution.colorList[vertex - 1] = 'g';

        Edge edge = Solution.G.edgeList[vertex - 1];
        while (edge.next != null) {
            if (Solution.colorList[edge.to - 1] == 'w') {
                Solution.parentList[edge.to - 1] = vertex;
                Solution.recDFS(edge.to);
            }
        }

        Solution.colorList[vertex - 1] = 'b';
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

    Node(int to, Edge next) {
        this.to = to;
        this.next = next;
    }
}