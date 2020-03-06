#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
#include <time.h>

#include <mkl_cblas.h>

#define GIGA pow(2, 30) 
#define NANO 1e9

float bdp(long, float *, float *);

int main(int argc, char * argv[])
{
    // Declare and initialize vars.
    long i;
    float res = 0.;

    // Parse command line args.
    if (argc != 3)
    {
        printf("Usage: dp3 N M\n");
        printf("N: Vector space dimension\n");
        printf("M: Number of repetitions for the measurement\n");
        exit(1);
    }

    long N = atol(argv[1]);
    int M = atoi(argv[2]);

    // Declare and initialize vecs.
    float pA[N], pB[N];
    for (i = 0; i < N; i++)
    {
        pA[i] = 1.;
        pB[i] = 1.;
    }

    // Declare timespec structs and array to store exec. time measurements
    struct timespec start, end;
    double times[M];

    // Measure exec. times and store measurements in secs.
    for (i = 0; i < M; i++)
    {
        clock_gettime(CLOCK_MONOTONIC, &start);
        res = bdp(N, pA, pB);
        clock_gettime(CLOCK_MONOTONIC, &end);

        times[i] = (end.tv_sec - start.tv_sec) + ((end.tv_nsec - start.tv_nsec)/NANO);
    }

    // Compute avgerage exec. time and use the value to compute bandwidth and FLOPS in GB/sec. and GFLOPS, respectively
    double avgTime = 0.;
    for (i = M/2; i < M; i++)
        avgTime += times[i];

    avgTime /= (M/2);

    double bw = (((double) N)*(2.*((double) sizeof(float))))/(avgTime*GIGA);

    double flops = ((double) (N*2L))/(avgTime*GIGA);

    // Print output
    printf("N: %ld <T>: %f sec. B: %lf GB/sec. F: %lf GFLOPS result: %.3f\n", N, avgTime, bw, flops, res);

    return 0;
}

// Function to benchmark
float bdp(long N, float *pA, float *pB) {
  float R = cblas_sdot(N, pA, 1, pB, 1);
  return R;
}
