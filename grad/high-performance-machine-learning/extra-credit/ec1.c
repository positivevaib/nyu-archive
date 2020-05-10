#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
#include <time.h>

#define GIGA pow(2, 30) 
#define NANO 1e9

float ecunroll(long, float *, float *);

int main(int argc, char * argv[])
{
    // Declare and initialize vars.
    long i, j, row, col;

    // Parse command line args.
    if (argc != 3)
    {
        printf("Usage: ec1 N M\n");
        printf("N: Vector space dimension\n");
        printf("M: Number of repetitions for the measurement\n");
        exit(1);
    }

    long N = atol(argv[1]);
    int M = atoi(argv[2]);

    // Declare vecs. and result matrix
    float pA[N], pB[N], res[N][N];

    // Declare timespec structs and array to store exec. time measurements
    struct timespec start, end;
    double times[M];

    // Measure exec. times and store measurements in secs.
    for (i = 0; i < M; i++)
    {
        times[i] = 0.0;

        for (row = 0; row < N; row++)
            for (col = 0; col < N; col++)
            {
                for (j = 0; j < N; j++)
                {
                    if (row*j % 2)
                        pA[j] = 0.6;
                    else
                        pA[j] = 0.2;

                    if (j*col % 2)
                        pB[j] = 0.6;
                    else
                        pB[j] = 0.2;
                }

                clock_gettime(CLOCK_MONOTONIC, &start);
                res[row][col] = ecunroll(N, pA, pB);
                clock_gettime(CLOCK_MONOTONIC, &end);

                times[i] += (end.tv_sec - start.tv_sec) + ((end.tv_nsec - start.tv_nsec)/NANO);
            }
    }

    // Compute avgerage exec. time and use the value to compute bandwidth and FLOPS in GB/sec. and GFLOPS, respectively
    double avgTime = 0.;
    for (i = M/2; i < M; i++)
        avgTime += times[i];

    avgTime /= (M/2);

    double bw = (((double) N)*(2.*((double) sizeof(float))))/(avgTime*GIGA);

    double flops = ((double) (N*2L))/(avgTime*GIGA);

    // Print output
    printf("N: %ld <T>: %f sec. B: %lf GB/sec. F: %lf GFLOPS\n", N, avgTime, bw, flops);

    return 0;
}

// Function to benchmark
float ecunroll(long N, float *pA, float *pB) {
  float R = 0.0;
  int j;
  for (j=0;j<N;j+=4)
    R += pA[j]*pB[j] + pA[j+1]*pB[j+1] + pA[j+2]*pB[j+2] + pA[j+3] * pB[j+3];
  return R;
}
