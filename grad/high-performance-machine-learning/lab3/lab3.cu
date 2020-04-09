#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include <cuda.h>
#include <cudnn.h>

#define C 3
#define H 1024
#define W 1024
#define P 1

#define K 64
#define FH 3
#define FW 3

#define NANO 1e9
#define PROGS 2
#define RUNS 5

#define CUDNN_CALL(x) do                                                                        \
{                                                                                               \
    cudnnStatus_t ___s = (x);                                                                   \
    if (___s != CUDNN_STATUS_SUCCESS)                                                           \
    {                                                                                           \
        fprintf(stderr, "%s:%d ERROR: %s\n", __FILE__,                                          \
                        __LINE__, cudnnGetErrorString(___s));                                   \
        exit(-1);                                                                               \
    }                                                                                           \
} while (0);                                                    

// Forward kernel declaration
__global__ void convolution(double *, double *, double *, long, long, long, long, long);

int main(int argc, char * argv[])
{
    // Declare and initialize data structures
    // Initialize image (with and without padding) and filter arrays
    long PH = H + (2 * P);
    long PW = W + (2 * P);
    //-------------------------double hI[C][H][W], hPI[C][PH][PW], hF[K][C][FH][FW];
    double hI[C * H * W], hPI[C * PH * PW], hF[K * C * FH * FW];

    long c, x, y, k, i, j; 
    for (c = 0; c < C; c++)
    {
        for (x = 0; x < PH; x++)
            for (y = 0; y < PW; y++)
            {
                if (x == 0 || x == (PH - 1) || y == 0 || y == (PW - 1))
                    hPI[(c * PH * PW) + (x * PW) + y] = 0;//-----------------hPI[c][x][y] = 0;
                else
                {
                    hI[(c * H * W) + (x * W) + y] = c * (x + y);//----------------hI[c][x][y] = c * (x + y);
                    hPI[(c * PH * PW) + (x * PW) + y] = c * (x + y);//----------------hPI[c][x][y] = c * (x + y);
                }
            }

        for (k = 0; k < K; k++)
            for (i = 0; i < FH; i++)
                for (j = 0; j < FW; j++)
                    hF[(k * C * FH * FW) + (c * FH * FW) + (i * FW) + j] = (c + k) * (i + j);//-----------------------hF[k][c][i][j] = (c + k) * (i + j);
    }

    // Declare timespec structs and array to store time measurements
    struct timespec start, end;
    double ICopyTimes[PROGS * RUNS];
    double convTimes[PROGS * RUNS];
    double OCopyTimes[PROGS * RUNS];

    // Execute C1
    int prog = 0;
    for (i = 0; i < RUNS; i++)
    {
        // Declare host output array and device arrays
        double hO[K * H * W], * I, * F, * O;//--------------------------double hO[K][H][W], I[C][H + (2 * P)][W + (2 * P)], F[K][C][FH][FW], O[K][H][W];

        // Allocate device memory and transfer data to device
        cudaMalloc(&I, sizeof(hPI));
        cudaMalloc(&F, sizeof(hF));
        cudaMalloc(&O, sizeof(hO));
        
        clock_gettime(CLOCK_MONOTONIC, &start);
        cudaMemcpy(I, hPI, sizeof(hPI), cudaMemcpyHostToDevice);
        clock_gettime(CLOCK_MONOTONIC, &end);

        ICopyTimes[(prog * RUNS) + i] = (end.tv_sec - start.tv_sec)                             \
                                        + ((end.tv_nsec - start.tv_nsec)/NANO);

        cudaMemcpy(F, hF, sizeof(hF), cudaMemcpyHostToDevice);

        // Set device grid and block dimensions
        dim3 dimGrid(K, H);
        dim3 dimBlock(W);

        // Invoke kernel
        clock_gettime(CLOCK_MONOTONIC, &start);
        convolution<<<dimGrid, dimBlock>>>(I, F, O, C, W, P, FH, FW);
        cudaDeviceSynchronize();
        clock_gettime(CLOCK_MONOTONIC, &end);

        convTimes[(prog * RUNS) + i] = (end.tv_sec - start.tv_sec)                              \
                                       + ((end.tv_nsec - start.tv_nsec)/NANO);

        // Transfer output data from device
        clock_gettime(CLOCK_MONOTONIC, &start);
        cudaMemcpy(hO, O, sizeof(hO), cudaMemcpyDeviceToHost);
        clock_gettime(CLOCK_MONOTONIC, &end);

        OCopyTimes[(prog * RUNS) + i] = (end.tv_sec - start.tv_sec)                             \
                                        + ((end.tv_nsec - start.tv_nsec)/NANO);

        // Free device memory
        cudaFree(I);
        cudaFree(F);
        cudaFree(O);

        // Compute checksums
        double ISum = 0;
        double OSum = 0;

        for (x = 0; x < H; x++)
            for (y = 0; y < W; y++)
            {
                for (c = 0; c < C; c++)
                    ISum += hPI[(c * PH * PW) + ((x + 1) * PW) + (y + 1)];//--------------------hPI[c][x + 1][y + 1];

                for (k = 0; k < K; k++)
                    OSum += hO[(k * H * W) + (x * W) + y];//----------------hO[k][x][y];
            }

        // Print output 
        printf("\nI = checksum: %lf                                                             \
                \nCopy host -> dev kernel: %lf s.                                               \
                \ntime kernel: %lf s.                                                           \
                \nCopy dev -> host kernel: %lf s.                                               \
                \nCUDA O = checksum: %lf",                                                      \
                ISum,                                                                           \
                ICopyTimes[(prog * RUNS) + i],                                                  \
                convTimes[(prog * RUNS) + i],                                                   \
                OCopyTimes[(prog * RUNS) + i],                                                  \
                OSum);
    }

    // Execute C2
    prog = 1;
    for (i = 0; i < RUNS; i++)
    {
        // Create cuDNN context
        cudnnHandle_t cudnn;
        CUDNN_CALL(cudnnCreate(&cudnn));

        // Create and configure descriptors
        // Input
        cudnnTensorDescriptor_t inDesc;
        CUDNN_CALL(cudnnCreateTensorDescriptor(&inDesc));
        CUDNN_CALL(cudnnSetTensor4dDescriptor(inDesc,                                           \
                                              CUDNN_TENSOR_NCHW,                                \
                                              CUDNN_DATA_DOUBLE,                                \
                                              1, C, H, W));
    
        // Filter
        cudnnFilterDescriptor_t filterDesc;
        CUDNN_CALL(cudnnCreateFilterDescriptor(&filterDesc));
        CUDNN_CALL(cudnnSetFilter4dDescriptor(filterDesc,                                       \
                                              CUDNN_DATA_DOUBLE,                                \
                                              CUDNN_TENSOR_NCHW,                                \
                                              K, C, FH, FW));
        
        // Output
        cudnnTensorDescriptor_t outDesc;
        CUDNN_CALL(cudnnCreateTensorDescriptor(&outDesc));
        CUDNN_CALL(cudnnSetTensor4dDescriptor(outDesc,                                          \
                                              CUDNN_TENSOR_NCHW,                                \
                                              CUDNN_DATA_DOUBLE,                                \
                                              1, K, H, W));

        // Convolution
        cudnnConvolutionDescriptor_t convDesc;
        CUDNN_CALL(cudnnCreateConvolutionDescriptor(&convDesc));
        CUDNN_CALL(cudnnSetConvolution2dDescriptor(convDesc,                                    \
                                                   1, 1,                                        \
                                                   1, 1,                                        \
                                                   1, 1,                                        \
                                                   CUDNN_CROSS_CORRELATION,                     \
                                                   CUDNN_DATA_DOUBLE));                         \
        // Convolution algorithm
        cudnnConvolutionFwdAlgo_t algo;
        CUDNN_CALL(cudnnGetConvolutionForwardAlgorithm(cudnn,                                   \
                                                      inDesc,                                   \
                                                      filterDesc,                               \
                                                      convDesc,                                 \
                                                      outDesc,                                  \
                                                      CUDNN_CONVOLUTION_FWD_PREFER_FASTEST,     \
                                                      0,                                        \
                                                      &algo));

        // Determine device memory requirement
        size_t workspaceSize;
        CUDNN_CALL(cudnnGetConvolutionForwardWorkspaceSize(cudnn,                               \
                                                           inDesc,                              \
                                                           filterDesc,                          \
                                                           convDesc,                            \
                                                           outDesc,                             \
                                                           algo,                                \
                                                           &workspaceSize));

        // Declare host output array, workspace and device arrays
        double hOut[K * H * W];//----------------double hOut[K][H][W];
        void * workspace, * in, * filter, * out;

        // Allocate device memory and transfer data to device
        cudaMalloc(&workspace, workspaceSize);
        cudaMalloc(&in, sizeof(hI));
        cudaMalloc(&filter, sizeof(hF));
        cudaMalloc(&out, sizeof(hOut));

        clock_gettime(CLOCK_MONOTONIC, &start);
        cudaMemcpy(in, hI, sizeof(hI), cudaMemcpyHostToDevice);
        clock_gettime(CLOCK_MONOTONIC, &end);
        
        ICopyTimes[(prog * RUNS) + i] = (end.tv_sec - start.tv_sec)                             \
                                        + ((end.tv_nsec - start.tv_nsec)/NANO);

        cudaMemcpy(filter, hF, sizeof(hF), cudaMemcpyHostToDevice);

        // Execute convolution
        const float alpha = 1;
        const float beta = 0;

        clock_gettime(CLOCK_MONOTONIC, &start);
        CUDNN_CALL(cudnnConvolutionForward(cudnn,                                               \
                                           &alpha,                                              \
                                           inDesc, hI,                                          \
                                           filterDesc, hF,                                      \
                                           convDesc,                                            \
                                           algo,                                                \
                                           &workspace, workspaceSize,                           \
                                           &beta,                                               \
                                           outDesc, out));
        clock_gettime(CLOCK_MONOTONIC, &end);

        convTimes[(prog * RUNS) + i] = (end.tv_sec - start.tv_sec)                              \
                                       + ((end.tv_nsec - start.tv_nsec)/NANO);

        // Transfer data from device
        clock_gettime(CLOCK_MONOTONIC, &start);
        cudaMemcpy(hOut, out, sizeof(hOut), cudaMemcpyDeviceToHost);
        clock_gettime(CLOCK_MONOTONIC, &end);

        OCopyTimes[(prog * RUNS) + i] = (end.tv_sec - start.tv_sec)                             \
                                        + ((end.tv_nsec - start.tv_nsec)/NANO);

        // Free device memory and destroy descriptors and cuDNN context
        cudaFree(workspace);
        cudaFree(in);
        cudaFree(filter);
        cudaFree(out);

        CUDNN_CALL(cudnnDestroyTensorDescriptor(inDesc));
        CUDNN_CALL(cudnnDestroyFilterDescriptor(filterDesc));
        CUDNN_CALL(cudnnDestroyTensorDescriptor(outDesc));
        CUDNN_CALL(cudnnDestroyConvolutionDescriptor(convDesc));

        CUDNN_CALL(cudnnDestroy(cudnn));

        // Compute checksums
        double inSum = 0;
        double outSum = 0;

        for (x = 0; x < H; x++)
            for (y = 0; y < W; y++)
            {
                for (c = 0; c < C; c++)
                    inSum += hI[(c * H * W) + (x * W) + y];//--------------------hI[c][x][y];

                for (k = 0; k < K; k++)
                    outSum += hOut[(k * H * W) + (x * W) + y];//----------------------hOut[k][x][y];
            }

        // Print output 
        printf("\nI = checksum: %lf                                                             \
                \nCopy host -> dev kernel: %lf s.                                               \
                \ntime kernel: %lf s.                                                           \
                \nCopy dev -> host kernel: %lf s.                                               \
                \nCUDA O = checksum: %lf",                                                      \
                inSum,                                                                          \
                ICopyTimes[(prog * RUNS) + i],                                                  \
                convTimes[(prog * RUNS) + i],                                                   \
                OCopyTimes[(prog * RUNS) + i],                                                  \
                outSum);
    }

    // Compute averages
    double avg[PROGS] = {0, 0};
    for (i = 0; i < (PROGS * RUNS); i++)
        avg[i / RUNS] += (convTimes[i] / RUNS);

    // Print output
    printf("\n\n<Time>: Conv %lf s. cuDNN %lf s.\n", avg[0], avg[1]);

    return 0;
}

// Convolution kernel
__global__ void convolution(double * I, double * F, double * O, long C, long W, long P, long FH, long FW)
{
    // Compute image width with padding
    const long PH = H + (2 * P);
    const long PW = W + (2 * P);

    // Declare shared arrays
    __shared__ double ITile[C][FH][PW];
    __shared__ double FTile[C][FH][FW];

    // Transfer tiles to shared memory
    long c, h, w, i;
    for (c = 0; c < C; c++)
        for (h = 0; h < FH; h++)
        {
            i = threadIdx.x;
            while (i < PW)
            {
                ITile[c][h][i] = I[(c * PH * PW) + (((blockIdx.y * FH) + h) * PW) + i];//-------------------I[c][(blockIdx.y * FH) + h][i];

                if (i < FW)
                    FTile[c][h][i] = F[(blockIdx.x * C * FH * FW) + (c * FH * FW) + (h * FW) + i];//---------------------F[blockIdx.x][c][h][i];

                i += blockDim.x;
            }
        }
    __syncthreads();

    // Perform convolution
    double o = 0;
    for (c = 0; c < C; c++)
        for (h = 0; h < FH; h++)
            for (w = 0; w < FW; w++)
                o += ITile[c][h][(threadIdx.x * FW) + w] * FTile[c][h][w];

    O[(blockIdx.x * H * W) + (blockIdx.y * W) + threadIdx.x] = o;//---------------O[blockIdx.x][blockIdx.y][threadIdx.x] = o;
    __syncthreads();
}
