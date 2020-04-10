#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include <cuda.h>
#include <cudnn.h>

#define NANO 1e9

#define CUDNN_CALL(x) do                                                                            \
{                                                                                                   \
    cudnnStatus_t ___s = (x);                                                                       \
    if (___s != CUDNN_STATUS_SUCCESS)                                                               \
    {                                                                                               \
        fprintf(stderr, "%s:%d ERROR: %s\n", __FILE__,                                              \
                        __LINE__, cudnnGetErrorString(___s));                                       \
        exit(-1);                                                                                   \
    }                                                                                               \
} while (0);                                                    


// Forward declarations
typedef struct dimensions
{
    const int C, H, W, PH, PW;
    const int K, FH, FW;
};

void initArrs(double *, double *, double *, struct dimensions *);
void C1(struct dimensions *, double *, double *, double *, double *, double *);
void C2(struct dimensions *, double *, double *, double *, double *, double *);

__global__ void convKernel(double *, double *, double *,                                           \
                            int, int, int, int, int, int, int);


int main(int argc, char * argv[])
{
    // Initialize dimensions struct
    struct dimensions dims = {3, 1024, 1024, 1026, 1026, 64, 3, 3};
    
    // Initialize image (with and without padding) and filter arrays
    double hostI[dims.C * dims.H * dims.W], hostPI[dims.C * dims.PH * dims.PW], hostF[dims.K * dims.C * dims.FH * dims.FW];
    initArrs(hostI, hostPI, hostF, &dims);

    // Declare arrays to store time measurements
    double copyToDevTimes[10], convTimes[10], copyToHostTimes[10];

    // Execute programs
    printf("C1");
    C1(&dims, hostPI, hostF, copyToDevTimes, convTimes, copyToHostTimes);

    printf("\n\nC2");
    C2(&dims, hostI, hostF, copyToDevTimes, convTimes, copyToHostTimes);

    // Compute averages
    double avg[2];

    int i;
    for (i = 0; i < 2; i++)
        avg[i] = 0;

    for (i = 0; i < (2 * 5); i++)
        avg[i / 5] += (convTimes[i] / 5);

    // Print output
    printf("\n\n<Time>: Conv %lf s. cuDNN %lf s.\n", avg[0], avg[1]);

    return 0;
}


void initArrs(double * hostI, double * hostPI, double * hostF, struct dimensions * dims)
{
    int c, x, y, k, i, j; 
    for (c = 0; c < dims->C; c++)
    {
        for (x = 0; x < dims->PH; x++)
            for (y = 0; y < dims->PH; y++)
                hostPI[(c * dims->PH * dims->PW) + (x * dims->PW) + y] = 0;

        for (x = 0; x < dims->H; x++)
            for (y = 0; y < dims->W; y++)
            {
                hostI[(c * dims->H * dims->W) + (x * dims->W) + y] = c * (x + y);
                hostPI[(c * dims->PH * dims->PW) + ((x + 1) * dims->PW) + (y + 1)] = c * (x + y);
            }

        for (k = 0; k < dims->K; k++)
            for (i = 0; i < dims->FH; i++)
                for (j = 0; j < dims->FW; j++)
                    hostF[(k * dims->C * dims->FH * dims->FW) + (c * dims->FH * dims->FW) + (j * dims->FW) + i] = (c + k) * (i + j);
    }
}


void C1(struct dimensions * dims, double * hostPI, double * hostF, double * copyToDevTimes, double * convTimes, double * copyToHostTimes)
{
    // Declare timespec structs 
    struct timespec start, end;

    // Declare host output array and device arrays
    double hostO[dims->K * dims->H * dims->W], * devPI, * devF, * devO;

    int i, x, y, c, k;
    for (i = 0; i < 5; i++)
    {
        // Allocate device memory and transfer data to device
        cudaMalloc(&devPI, sizeof(hostPI));
        cudaMalloc(&devF, sizeof(hostF));
        cudaMalloc(&devO, sizeof(hostO));
        
        clock_gettime(CLOCK_MONOTONIC, &start);
        cudaMemcpy(devPI, hostPI, sizeof(hostPI), cudaMemcpyHostToDevice);
        clock_gettime(CLOCK_MONOTONIC, &end);

        copyToDevTimes[i] = (end.tv_sec - start.tv_sec) + ((end.tv_nsec - start.tv_nsec)/NANO);

        cudaMemcpy(devF, hostF, sizeof(hostF), cudaMemcpyHostToDevice);

        // Set device grid and block dimensions
        dim3 dimGrid(dims->K, dims->H);
        dim3 dimBlock(dims->W / 2);

        // Invoke kernel
        clock_gettime(CLOCK_MONOTONIC, &start);
        convKernel<<<dimGrid, dimBlock, ((dims->C * dims->FH * (dims->PW / 2)) + (dims->C * dims->FH * dims->FW)) * sizeof(double)>>>(devPI, devF, devO, dims->C, dims->H, dims->W, dims->PH, dims->PW, dims->FH, dims->FW);
        cudaDeviceSynchronize();
        clock_gettime(CLOCK_MONOTONIC, &end);

        convTimes[i] = (end.tv_sec - start.tv_sec) + ((end.tv_nsec - start.tv_nsec)/NANO);

        // Transfer output data from device
        clock_gettime(CLOCK_MONOTONIC, &start);
        cudaMemcpy(hostO, devO, sizeof(hostO), cudaMemcpyDeviceToHost);
        clock_gettime(CLOCK_MONOTONIC, &end);

        copyToHostTimes[i] = (end.tv_sec - start.tv_sec) + ((end.tv_nsec - start.tv_nsec)/NANO);

        // Free device memory
        cudaFree(devPI);
        cudaFree(devF);
        cudaFree(devO);

        // Compute checksums
        double checksumPI = 0;
        for (c = 0; c < dims->C; c++)
            for (x = 0; x < dims->PH; x++)
                for (y = 0; y < dims->PW; y++)
                    checksumPI += hostPI[(c * dims->PH * dims->PW) + (x * dims->PW) + y];

        double checksumO = 0;
        for (k = 0; k < dims->K; k++)
            for (x = 0; x < dims->H; x++)
                for (y = 0; y < dims->W; y++)
                    checksumO += hostO[(k * dims->H * dims->W) + (x * dims->W) + y];

        // Print output 
        printf("\n\nI = checksum: %lf\nCopy host -> dev kernel: %lf s.\ntime kernel: %lf s.\nCopy dev -> host kernel: %lf s.\nCUDA O = checksum: %lf", checksumPI, copyToDevTimes[i], convTimes[i], copyToHostTimes[i], checksumO);
    }
}

void C2(struct dimensions * dims, double * hostI, double * hostF, double * copyToDevTimes, double * convTimes, double * copyToHostTimes)
{
    // Declare timespec structs 
    struct timespec start, end;

    // Declare host output array, workspace and device arrays
    double hostO[dims->K * dims->H * dims->W];
    void * workspace, * devI, * devF, * devO;

    int i, x, y, c, k;
    for (i = 0; i < 5; i++)
    {
        // Create cuDNN context
        cudnnHandle_t cudnn;
        CUDNN_CALL(cudnnCreate(&cudnn));

        // Create and configure descriptors
        // Input
        cudnnTensorDescriptor_t descI;
        CUDNN_CALL(cudnnCreateTensorDescriptor(&descI));
        CUDNN_CALL(cudnnSetTensor4dDescriptor(descI, CUDNN_TENSOR_NCHW, CUDNN_DATA_DOUBLE, 1, dims->C, dims->H, dims->W));
    
        // Filter
        cudnnFilterDescriptor_t descF;
        CUDNN_CALL(cudnnCreateFilterDescriptor(&descF));
        CUDNN_CALL(cudnnSetFilter4dDescriptor(descF, CUDNN_DATA_DOUBLE, CUDNN_TENSOR_NCHW, dims->K, dims->C, dims->FH, dims->FW));
        
        // Output
        cudnnTensorDescriptor_t descO;
        CUDNN_CALL(cudnnCreateTensorDescriptor(&descO));
        CUDNN_CALL(cudnnSetTensor4dDescriptor(descO, CUDNN_TENSOR_NCHW, CUDNN_DATA_DOUBLE, 1, dims->K, dims->H, dims->W));

        // Convolution
        cudnnConvolutionDescriptor_t descConv;
        CUDNN_CALL(cudnnCreateConvolutionDescriptor(&descConv));
        CUDNN_CALL(cudnnSetConvolution2dDescriptor(descConv, 1, 1, 1, 1, 1, 1, CUDNN_CONVOLUTION, CUDNN_DATA_DOUBLE));                             

        // Convolution algorithm
        cudnnConvolutionFwdAlgo_t algo;
        CUDNN_CALL(cudnnGetConvolutionForwardAlgorithm(cudnn, descI, descF, descConv, descO, CUDNN_CONVOLUTION_FWD_PREFER_FASTEST, 0, &algo));

        // Determine device memory requirement
        size_t workspaceSize;
        CUDNN_CALL(cudnnGetConvolutionForwardWorkspaceSize(cudnn, descI, descF, descConv, descO, algo, &workspaceSize));

        // Allocate device memory and transfer data to device
        cudaMalloc(&workspace, workspaceSize);
        cudaMalloc(&devI, sizeof(hostI));
        cudaMalloc(&devF, sizeof(hostF));
        cudaMalloc(&devO, sizeof(hostO));

        clock_gettime(CLOCK_MONOTONIC, &start);
        cudaMemcpy(devI, hostI, sizeof(hostI), cudaMemcpyHostToDevice);
        clock_gettime(CLOCK_MONOTONIC, &end);
        
        copyToDevTimes[5 + i] = (end.tv_sec - start.tv_sec) + ((end.tv_nsec - start.tv_nsec)/NANO);

        cudaMemcpy(devF, hostF, sizeof(hostF), cudaMemcpyHostToDevice);

        // Execute convolution
        const float alpha = 1;
        const float beta = 0;

        clock_gettime(CLOCK_MONOTONIC, &start);
        CUDNN_CALL(cudnnConvolutionForward(cudnn, &alpha, descI, devI, descF, devF, descConv, algo, &workspace, workspaceSize, &beta, descO, devO));
        clock_gettime(CLOCK_MONOTONIC, &end);

        convTimes[5 + i] = (end.tv_sec - start.tv_sec) + ((end.tv_nsec - start.tv_nsec)/NANO);

        // Transfer data from device
        clock_gettime(CLOCK_MONOTONIC, &start);
        cudaMemcpy(hostO, devO, sizeof(hostO), cudaMemcpyDeviceToHost);
        clock_gettime(CLOCK_MONOTONIC, &end);

        copyToHostTimes[5 + i] = (end.tv_sec - start.tv_sec) + ((end.tv_nsec - start.tv_nsec)/NANO);

        // Free device memory and destroy descriptors
        cudaFree(workspace);
        cudaFree(devI);
        cudaFree(devF);
        cudaFree(devO);

        cudnnDestroyTensorDescriptor(descI);
        cudnnDestroyFilterDescriptor(descF);
        cudnnDestroyTensorDescriptor(descO);
        cudnnDestroyConvolutionDescriptor(descConv);

        cudnnDestroy(cudnn);

        // Compute checksums
        double checksumI = 0;
        for (c = 0; c < dims->C; c++)
            for (x = 0; x < dims->H; x++)
                for (y = 0; y < dims->W; y++)
                    checksumI += hostI[(c * dims->H * dims->W) + (x * dims->W) + y];

        double checksumO = 0;
        for (k = 0; k < dims->K; k++)
            for (x = 0; x < dims->H; x++)
                for (y = 0; y < dims->W; y++)
                    checksumO += hostO[(k * dims->H * dims->W) + (x * dims->W) + y];

        // Print output 
        printf("\n\nI = checksum: %lf\nCopy host -> dev kernel: %lf s.\ntime kernel: %lf s.\nCopy dev -> host kernel: %lf s.\nCUDA O = checksum: %lf", checksumI, copyToDevTimes[i], convTimes[i], copyToHostTimes[i], checksumO);
    }
}

// Convolution kernel
__global__ void convKernel(double * PI, double * F, double * O, int C, int H, int W, int PH, int PW, int FH, int FW)
{
    // Declare shared arrays
    extern __shared__ double tile[];
    double * tilePI = tile;
    double * tileF = (double *) &tilePI[C * FH * (PW / 2)];

    // Transfer tiles to shared memory
    int half, c, h, w, i;
    for (half = 0; half < 2; half++)
    {
        for (c = 0; c < C; c++)
            for (h = 0; h < FH; h++)
            {
                i = threadIdx.x;
                while (i < (PW / 2))
                {
                    tilePI[(c * FH * (PW / 2)) + (h * (PW / 2)) + i] = PI[(c * PH * PW) + (((blockIdx.y * FH) + h) * PW) + ((half * blockDim.x) + i)];

                    if (i < FW)
                        tileF[(c * FH * FW) + (h * FW) + i] = F[(blockIdx.x * C * FH * FW) + (c * FH * FW) + (h * FW) + i];

                    i += blockDim.x;
                }
            }
        __syncthreads();

        // Perform convolution
        double o = 0;

        for (c = 0; c < C; c++)
            for (h = 0; h < FH; h++)
                for (w = 0; w < FW; w++)
                    o += tilePI[(c * FH * (PW / 2)) + (h * (PW / 2)) + ((threadIdx.x * FW) + w)] * tileF[(c * FH * FW) + (h * FW) + w];

        O[(blockIdx.x * H * W) + (blockIdx.y * W) + ((half * blockDim.x) + threadIdx.x)] = o;
        __syncthreads();
    }
}
