import java.io.*;
import java.util.*;

public class Solution {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        // Read input
        int degree = input.nextInt();
        long[] f = new long[degree + 1];
        long[] g = new long[degree + 1];

        for (int i = 0; i < degree + 1; i++)
            f[i] = input.nextLong();
        
        for (int i = 0; i < degree + 1; i++)
            g[i] = input.nextLong();

        input.close();
        
        // Multiply
        long[] product = karatsubaMultiplication(f, g);

        for (int i = 0; i < product.length; i++) {
            System.out.print(product[i]);
            if (i != product.length - 1)
                System.out.print(' ');
        }
    }

    static long[] naiveMultiplication(long[] f, long[] g) {
        // Initialize product
        long[] product = new long[2*f.length - 1];
        for (int i = 0; i < product.length; i++)
            product[i] = 0;

        // Multiply
        for (int i = 0; i < g.length; i++)
            for (int j = 0; j < f.length; j++)
                product[i + j] = product[i + j] + g[i]*f[j];

        return product;
    }

    static long[] karatsubaMultiplication(long[] f, long[] g) {
        // Recursively call karatsubaMultiplication if degree is greater than 10. Else, call naiveMultiplication
        int degree = f.length - 1;
        if (degree > 64) {
            // Split polynomials
            long[] fLo = Arrays.copyOfRange(f, 0, degree/2 + 1);
            long[] fHi = Arrays.copyOfRange(f, degree/2 + 1, f.length);

            long[] gLo = Arrays.copyOfRange(g, 0, degree/2 + 1);
            long[] gHi = Arrays.copyOfRange(g, degree/2 + 1, g.length);

            // Compute sums
            long[] F = new long[fHi.length];
            long[] G = new long[gHi.length];
            for (int i = 0; i < F.length; i++) {
                F[i] = fHi[i] + fLo[i];
                G[i] = gHi[i] + gLo[i];
            } 

            // Multiply
            long[] U = karatsubaMultiplication(fHi, gHi);
            long[] V = karatsubaMultiplication(fLo, gLo);
            long[] W = karatsubaMultiplication(F, G);

            long[] product = new long[2*degree + 1];
            for (int i = 0; i < product.length; i++)
                product[i] = 0;

            for (int i = 0; i < V.length; i++)
                product[i] = V[i];
            
            for (int i = 0; i < U.length; i++)
                product[i + degree/2 + 1] += W[i] - U[i] - V[i];
            
            for (int i = 0; i < U.length; i++)
                product[i + degree + 1] += U[i];

            return product;
        }
        else {
            // Multiply
            return naiveMultiplication(f, g);
        }
    }
}