#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
#include <time.h>

float dp(long, float *, float *);

int main(int argc, char * argv[])
{
    int i;
    float res; 

    if (argc != 3)
    {
        printf("Usage: dp1 D R\n");
        printf("D: Vector space dimension\n");
        printf("R: Number of repetitions for the measurement\n");
        exit(1);
    }

    long vecDim = atol(argv[1]);
    int numReps = atoi(argv[2]);

    float vecA[vecDim], vecB[vecDim];
    for (i = 0; i < vecDim; i++)
    {
        vecA[i] = 1.0;
        vecB[i] = 1.0;
    }

    struct timespec start, end;
    double execTimes[numReps/2];

    for (i = 0; i < numReps; i++)
    {
        clock_gettime(CLOCK_MONOTONIC, &start);
        res = dp(vecDim, vecA, vecB);
        clock_gettime(CLOCK_MONOTONIC, &end);

        if (i >= numReps/2)
            execTimes[i - numReps/2] = ((double)end.tv_sec*1000000 + (double)end.tv_nsec/1000) - ((double)start.tv_sec*1000000 + (double)start.tv_nsec/1000);
    }

    double avgExecTime = 0.0;
    for (i = 0; i < numReps/2; i++)
        avgExecTime += execTimes[i];

    avgExecTime /= (numReps/2);

    float bandwidth = ((vecDim*(3*4 + 2) + 4)/avgExecTime)/pow(2, 30);

    float flops = (vecDim*2)/avgExecTime;

    printf("N: %d <T>: %-.6f sec. B: %-.3f GB/sec. F: %-.3f FLOPS\n", vecDim, avgExecTime, bandwidth, flops);
}

float dp(long vecDim, float * vecA, float * vecB)
{
    float res = 0.0;

    int i;
    for (i = 0; i < vecDim; i++)
        res += vecA[i]*vecB[i];

    return res;
}
