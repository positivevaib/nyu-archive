import java.io.*;
import java.util.*;

public class Solution {
    public static void main(String[] args) {

    }
}

class Graph {
    Edge[] edgeList;

    public Graph(int totEdges) {
        this.edgeList = new Edge[totEdges];
        for (int i = 0; i < this.edgeList.length; i++)
            this.edgeList[i] = null;
    }

    public void addEdge(int from, int to) {
        this.edgeList[from - 1] = new Edge(to, this.edgeList[from - 1]);
    }
}

class Edge {
    int vertex;
    Edge next;

    public Node(int vertex, Edge next) {
        this.vertex = vertex;
        this.next = next;
    }
}