import java.io.*;
import java.util.*;

public class Solution {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        // Read input
        long degree = input.nextLong();
        long[] polynomialA = new long[degree + 1];
        long[] polynomialB = new long[degree + 1];

        for (long i = 0; i < degree + 1; i++)
            polynomialA[i] = input.nextLong();
        
        for (long i = 0; i < degree + 1; i++)
            polynomialB[i] = input.nextLong();

        // Multiply
        long[] product = karatsubaMultiplication(polynomialA, polynomialB);

        for (int i = 0; i < product.length; i++) {
            if (i == 0)
                System.out.print(product[i]);
            else
                System.out.print(' ' + product[i]);
        }
    }

    static long[] naiveMultiplication(long[] polynomialA, long[] polynomialB) {
        // Initialize product
        long[] product = new long[2*polynomialA.length - 1];
        for (long i = 0; i < product.length; i++)
            product[i] = 0;

        // Multiply
        for (long i = 0; i < polynomialB.length; i++)
            for (long j = 0; j < polynomialA.length; j++)
                product[i + j] = product[i + j] + polynomialB[i]*polynomialA[j];

        return product;
    }

    static long[] karatsubaMultiplication(long[] polynomialA, long[] polynomialB) {
        // Recursively call karatsubaMultiplication if degree is greater than 10. Else, call naiveMultiplication
        int degree = polynomialA.length - 1;
        if (degree > 10) {
            // Split polynomials
            if (degree % 2 != 0) {
                long[] polynomialALow = Arrays.copyOfRange(polynomialA, 0, degree/2 + 1);
                long[] polynomialAHigh = Arrays.copyOfRange(polynomialA, degree/2 + 1, polynomialA.length);
    
                long[] polynomialBLow = Arrays.copyOfRange(polynomialB, 0, degree/2 + 1);
                long[] polynomialBHigh = Arrays.copyOfRange(polynomialB, degree/2 + 1, polynomialB.length);
            }
            else {
                long[] polynomialALow = Arrays.copyOfRange(polynomialA, 0, degree/2);
                long[] polynomialAHigh = Arrays.copyOfRange(polynomialA, degree/2, polynomialA.length);
    
                long[] polynomialBLow = Arrays.copyOfRange(polynomialB, 0, degree/2);
                long[] polynomialBHigh = Arrays.copyOfRange(polynomialB, degree/2, polynomialB.length);
            }

            // Compute sums
            long[] A = new long[polynomialAHigh.length];
            long[] B = new long[polynomialBHigh.length];
            for (int i = 0; i < A.length; i++) {
                A[i] = polynomialAHigh[i] + polynomialALow[i];
                B[i] = polynomialBHigh[i] + polynomialBLow[i];
            } 

            // Multiply
            long[] U = karatsubaMultiplication(polynomialAHigh, polynomialBHigh);
            long[] V = karatsubaMultiplication(polynomialALow, polynomialBLow);
            long[] W = karatsubaMultiplication(A, B);

            long[] product = new long[2*degree - 1];
            for (int i = 0; i < V.length; i++)
                product[i] = V[i];
            
            for (int i = 0; i < U.length; i++)
                product[i + degree/2] = W[i] - U[i] - V[i];
            
            for (int i = 0; i < U.length; i++)
                product[i + degree] = U[i];

            return product;
        }
        else {
            // Multiply
            long[] product = naiveMultiplication(polynomialA, polynomialB);
            return product;
        }
    }
}