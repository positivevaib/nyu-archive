#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>

#include <cuda.h>
#include <cudnn.h>


#define NANO 1e9

#define CUDNN_CALL(x) do                                                                        \
{                                                                                               \
    cudnnStatus_t ___s = (x);                                                                   \
    if (___s != CUDNN_STATUS_SUCCESS)                                                           \
    {                                                                                           \
        fprintf(stderr, "%s:%d ERROR: %s\n", __FILE__, __LINE__, cudnnGetErrorString(___s));    \
        exit(-1);                                                                               \
    }                                                                                           \
} while(0);                                                                                     \


// Forward declarations
void init_I(double *, int, int, int);
void init_F(double *, int, int, int, int);

double get_checksum(double *, int, int, int);

double c1(int, double *, int, int, int, double *, int, int, int);
double c2(int, double *, int, int, int, double *, int, int, int); 

__device__ void convolve(int, double *, int, int, int, double *, int, int, double *);
__global__ void convolve_tiles_with_shared_mem(int, double *, int, int, int, double *, int, int, int, double *);


int main(int argc, char * argv[]) 
{
    // Initialize dimensions and I and F arrays
    int C = 3, H = 1024, W = 1024, P = 1;
    int K = 64, FH = 3, FW = 3;

    double I[C * H * W], F[K * C * FH * FW];
    init_I(I, C, H, W);
    init_F(F, K, C, FH, FW);

    // Execute programs and output results
    double c1_kernel_time = 0; 
    double c2_kernel_time = 0; 
    
    int runs = 5;
    
    int i;

    printf("C2");
    for (i = 0; i < runs; i++)
    {
        c2_kernel_time += c2(C, I, H, W, P, F, K, FH, FW);
    }

    printf("\n\nC1");
    for (i = 0; i < runs; i++)
    {
        c1_kernel_time += c1(C, I, H, W, P, F, K, FH, FW);
    }

    printf("\n\n<Time>: Conv %lf s. cuDNN %lf s.\n", c1_kernel_time / runs, c2_kernel_time / runs);
}


// Function to initialize I array
void init_I(double * I, int C, int H, int W)
{
    int c, h, w;
    for (c = 0; c < C; c++)
        for (h = 0; h < H; h++)
            for (w = 0; w < W; w++)
                I[(c * H * W) + (h * W) + w] = c * (h + w);
}


// Function to initialize F array
void init_F(double * F, int K, int C, int H, int W)
{
    int k, c, h, w;
    for (k = 0; k < K; k++)
        for (c = 0; c < C; c++)
            for (h = 0; h < H; h++)
                for (w = 0; w < W; w++)
                    F[(k * C * H * W) + (c * H * W) + (h * W) + w] = (c + k) * (h + w);
}


double c1(int C, double * I, int H, int W, int P, double * F, int K, int FH, int FW)
{
    // Determine array sizes, declare device arrays and allocate device memory
    size_t I_size = C * H * W * sizeof(double); 
    size_t F_size = K * C * FH * FW * sizeof(double); 
    size_t O_size = K * H * W * sizeof(double);

    double O[O_size], * dev_I, * dev_F, * dev_O;

    cudaMalloc(&dev_I, I_size);
    cudaMalloc(&dev_F, F_size);
    cudaMalloc(&dev_O, O_size);

    struct timespec start, end;

    clock_gettime(CLOCK_MONOTONIC, &start);
    cudaMemcpy(dev_I, I, I_size, cudaMemcpyHostToDevice);
    cudaDeviceSynchronize();
    clock_gettime(CLOCK_MONOTONIC, &end);

    double to_dev_time = (end.tv_sec - start.tv_sec) + ((end.tv_nsec - start.tv_nsec) / NANO);

    cudaMemcpy(dev_F, F, F_size, cudaMemcpyHostToDevice);
        
    // Set device properties and call kernel
    int block_size = 4;
    dim3 dimGrid(ceil(H / block_size), ceil(W / block_size));
    dim3 dimBlock(block_size, block_size, K);

    size_t tile_size = C * (block_size + (2 * P)) * (block_size + (2 * P));

    clock_gettime(CLOCK_MONOTONIC, &start);
    convolve_tiles_with_shared_mem<<<dimGrid, dimBlock, tile_size>>>(C, dev_I, H, W, P, dev_F, K, FH, FW, dev_O);
    cudaDeviceSynchronize();
    clock_gettime(CLOCK_MONOTONIC, &end);

    double kernel_time = (end.tv_sec - start.tv_sec) + ((end.tv_nsec - start.tv_nsec) / NANO);

    // Copy output array to host, free device memory and output results
    clock_gettime(CLOCK_MONOTONIC, &start);
    cudaMemcpy(O, dev_O, O_size, cudaMemcpyDeviceToHost);
    cudaDeviceSynchronize();
    clock_gettime(CLOCK_MONOTONIC, &end);

    double to_host_time = (end.tv_sec - start.tv_sec) + ((end.tv_nsec - start.tv_nsec) / NANO);

    cudaFree(dev_I);
    cudaFree(dev_F);
    cudaFree(dev_O);

    double I_checksum = get_checksum(I, C, H, W);
    double O_checksum = get_checksum(O, K, H, W);

    printf("\n\nI = checksum: %lf\nCopy host -> dev kernel: %lf s.\ntime kernel: %lf s.\nCopy dev -> host kernel: %lf s.\nCUDA O = checksum:%lf", I_checksum, to_dev_time, kernel_time, to_host_time, O_checksum);

    return kernel_time;
}


double c2(int C, double * I, int H, int W, int P, double * F, int K, int FH, int FW)
{
    // Determine array sizes, declare device arrays and workspace and allocate device memory
    size_t I_size = C * H * W * sizeof(double); 
    size_t F_size = K * C * FH * FW * sizeof(double); 
    size_t O_size = K * H * W * sizeof(double);

    double O[O_size], * dev_I, * dev_F, * dev_O;
    void * workspace;

    cudaMalloc(&dev_I, I_size);
    cudaMalloc(&dev_F, F_size);
    cudaMalloc(&dev_O, O_size);

    struct timespec start, end;

    clock_gettime(CLOCK_MONOTONIC, &start);
    cudaMemcpy(dev_I, I, I_size, cudaMemcpyHostToDevice);
    cudaDeviceSynchronize();
    clock_gettime(CLOCK_MONOTONIC, &end);

    double to_dev_time = (end.tv_sec - start.tv_sec) + ((end.tv_nsec - start.tv_nsec) / NANO);

    cudaMemcpy(dev_F, F, F_size, cudaMemcpyHostToDevice);

    // Setup and execute CUDNN based convolution 
    cudnnHandle_t cudnn;
    CUDNN_CALL(cudnnCreate(&cudnn));

    cudnnTensorDescriptor_t in_desc;
    CUDNN_CALL(cudnnCreateTensorDescriptor(&in_desc));
    CUDNN_CALL(cudnnSetTensor4dDescriptor(in_desc, CUDNN_TENSOR_NCHW, CUDNN_DATA_DOUBLE, 1, C, H, W));

    cudnnFilterDescriptor_t filter_desc;
    CUDNN_CALL(cudnnCreateFilterDescriptor(&filter_desc));
    CUDNN_CALL(cudnnSetFilter4dDescriptor(filter_desc, CUDNN_DATA_DOUBLE, CUDNN_TENSOR_NCHW, K, C, FH, FW));

    cudnnTensorDescriptor_t out_desc;
    CUDNN_CALL(cudnnCreateTensorDescriptor(&out_desc));
    CUDNN_CALL(cudnnSetTensor4dDescriptor(out_desc, CUDNN_TENSOR_NCHW, CUDNN_DATA_DOUBLE, 1, K, H, W));

    cudnnConvolutionDescriptor_t conv_desc;
    CUDNN_CALL(cudnnCreateConvolutionDescriptor(&conv_desc));
    CUDNN_CALL(cudnnSetConvolution2dDescriptor(conv_desc, P, P, 1, 1, 1, 1, CUDNN_CONVOLUTION, CUDNN_DATA_DOUBLE));

    cudnnConvolutionFwdAlgo_t conv_algo;
    CUDNN_CALL(cudnnGetConvolutionForwardAlgorithm(cudnn, in_desc, filter_desc, conv_desc, out_desc, CUDNN_CONVOLUTION_FWD_PREFER_FASTEST, 0, &conv_algo));

    size_t workspace_size = 0;
    CUDNN_CALL(cudnnGetConvolutionForwardWorkspaceSize(cudnn, in_desc, filter_desc, conv_desc, out_desc, conv_algo, &workspace_size));
	cudaMallocManaged(&workspace, workspace_size);

    double alpha = 1, beta = 0;

    clock_gettime(CLOCK_MONOTONIC, &start);
    CUDNN_CALL(cudnnConvolutionForward(cudnn, &alpha, in_desc, dev_I, filter_desc, dev_F, conv_desc, conv_algo, workspace, workspace_size, &beta, out_desc, dev_O));
    cudaDeviceSynchronize();
    clock_gettime(CLOCK_MONOTONIC, &end);

    double kernel_time = (end.tv_sec - start.tv_sec) + ((end.tv_nsec - start.tv_nsec) / NANO);

    // Copy output array to host, free device memory and output results
    clock_gettime(CLOCK_MONOTONIC, &start);
    cudaMemcpy(O, dev_O, O_size, cudaMemcpyDeviceToHost);
    cudaDeviceSynchronize();
    clock_gettime(CLOCK_MONOTONIC, &end);

    double to_host_time = (end.tv_sec - start.tv_sec) + ((end.tv_nsec - start.tv_nsec) / NANO);

    cudaFree(dev_I);
    cudaFree(dev_F);
    cudaFree(dev_O);
    cudaFree(workspace);

	cudnnDestroyTensorDescriptor(in_desc);
	cudnnDestroyFilterDescriptor(filter_desc);
	cudnnDestroyTensorDescriptor(out_desc);
	cudnnDestroyConvolutionDescriptor(conv_desc);
	cudnnDestroy(cudnn);

    double I_checksum = get_checksum(I, C, H, W);
    double O_checksum = get_checksum(O, K, H, W);

    printf("\n\nI = checksum: %lf\nCopy host -> dev kernel: %lf s.\ntime cudnn: %lf s.\nCopy dev -> host kernel: %lf s.\nCUDA O = checksum:%lf", I_checksum, to_dev_time, kernel_time, to_host_time, O_checksum);

    return kernel_time;
}


// Function to compute the sum of all elements of I and O arrays
double get_checksum(double * tensor, int C, int H, int W)
{
    double checksum = 0;

    int c, h, w;
    for (c = 0; c < C; c++)
        for (h = 0; h < H; h++)
            for (w = 0; w < W; w++)
                checksum += tensor[(c * H * W) + (h * W) + w];

    return checksum;
}


// CUDA kernel to perform convolution using tiles and shared memory
__global__ void convolve_tiles_with_shared_mem(int C, double * I, int H, int W, int P, double * F, int K, int FH, int FW, double * O)
{
    // Declare and populate tile array in shared memory 
    extern __shared__ double tile[];

    int TH = blockDim.x + (2 * P);
    int TW = blockDim.y + (2 * P);

    int k = threadIdx.z;
    int h = (blockIdx.x * blockDim.x) + threadIdx.x;
    int w = (blockIdx.y * blockDim.y) + threadIdx.y;

    if (k == 0 && h < H && w < W) 
    {
        int th = threadIdx.x + P;
        int tw = threadIdx.y + P;

        int c;
        for (c = 0; c < C; c++)
        {
            tile[(c * TH * TW) + (th * TW) + tw] = I[(c * H * W) + (h * W) + w];

            int pad;

            for (pad = 1; pad <= P; pad++)
            {
                // Fill top rows and corners
                if (threadIdx.x == 0)
                {
                    if (h > 0)
                        tile[(c * TH * TW) + ((th - pad) * TW) + tw] = I[(c * H * W) + ((h - pad) * W) + w];
                    else
                        tile[(c * TH * TW) + ((th - pad) * TW) + tw] = 0;

                    // Top left corner
                    if (threadIdx.y == 0)
                    {
                        int h_pad, w_pad;
                        for (h_pad = pad; h_pad > 0; h_pad--)
                            for (w_pad = pad; w_pad > 0; w_pad--)
                            {
                                if (h > 0 && w > 0)
                                    tile[(c * TH * TW) + ((th - h_pad) * TW) + (tw - w_pad)] = I[(c * H * W) + ((h - h_pad) * W) + (w - w_pad)];
                                else
                                    tile[(c * TH * TW) + ((th - h_pad) * TW) + (tw - w_pad)] = 0;
                            }
                    }

                    // Top right corner
                    if (threadIdx.y == (blockDim.y - 1))
                    {
                        int h_pad, w_pad;
                        for (h_pad = pad; h_pad > 0; h_pad--)
                            for (w_pad = pad; w_pad > 0; w_pad--)
                            {
                                if (h > 0 && w < (W - 1))
                                    tile[(c * TH * TW) + ((th - h_pad) * TW) + (tw + w_pad)] = I[(c * H * W) + ((h - h_pad) * W) + (w + w_pad)];
                                else
                                    tile[(c * TH * TW) + ((th - h_pad) * TW) + (tw + w_pad)] = 0;
                            }
                    }
                }

                // Fill bottom rows and corners
                if (threadIdx.x == (blockDim.x - 1))
                {
                    if (h < (H - 1))
                        tile[(c * TH * TW) + ((th + pad) * TW) + tw] = I[(c * H * W) + ((h + pad) * W) + w];
                    else
                        tile[(c * TH * TW) + ((th + pad) * TW) + tw] = 0;

                    // Bottom left corner
                    if (threadIdx.y == 0)
                    {
                        int h_pad, w_pad;
                        for (h_pad = pad; h_pad > 0; h_pad--)
                            for (w_pad = pad; w_pad > 0; w_pad--)
                            {
                                if (h < (H - 1) && w > 0)
                                    tile[(c * TH * TW) + ((th + h_pad) * TW) + (tw - w_pad)] = I[(c * H * W) + ((h + h_pad) * W) + (w - w_pad)];
                                else
                                    tile[(c * TH * TW) + ((th + h_pad) * TW) + (tw - w_pad)] = 0;
                            }
                    }

                    // Bottom right corner
                    if (threadIdx.y == (blockDim.y - 1))
                    {
                        int h_pad, w_pad;
                        for (h_pad = pad; h_pad > 0; h_pad--)
                            for (w_pad = pad; w_pad > 0; w_pad--)
                            {
                                if (h < (H - 1) && w < (W - 1))
                                    tile[(c * TH * TW) + ((th + h_pad) * TW) + (tw + w_pad)] = I[(c * H * W) + ((h + h_pad) * W) + (w - w_pad)];
                                else
                                    tile[(c * TH * TW) + ((th + h_pad) * TW) + (tw + w_pad)] = 0;
                            }
                    }
                }

                // Fill left columns
                if (threadIdx.y == 0)
                {
                    if (w > 0)
                        tile[(c * TH * TW) + (th * TW) + (tw - pad)] = I[(c * H * W) + (h * W) + (w - pad)];
                    else
                        tile[(c * TH * TW) + (th * TW) + (tw - pad)] = 0;
                }

                // Fill right columns
                if (threadIdx.y == (blockDim.y - 1))
                {
                    if (w < (W - 1))
                        tile[(c * TH * TW) + (th * TW) + (tw + pad)] = I[(c * H * W) + (h * W) + (w + pad)];
                    else
                        tile[(c * TH * TW) + (th * TW) + (tw + pad)] = 0;
                }
            }
        }
    }
    __syncthreads();

    // Perform convolution
    convolve(C, tile, H, W, P, F, FH, FW, O);
}


// CUDA kernel to perform individual convolution computations
__device__ void convolve(int C, double * I, int H, int W, int P, double * F, int FH, int FW, double * O)
{
    double val = 0;

    int k = threadIdx.z; 
    int h = (blockIdx.x * blockDim.x) + threadIdx.x - P;
    int w = (blockIdx.y * blockDim.y) + threadIdx.y - P;

    int c, fh, fw, i, j;
    for (c = 0; c < C; c++)
        for (fh = 0; fh < FH; fh++)
        {
            i = h + fh;
            for (fw = 0; fw < FW; fw++)
            {
                j = w + fw;

                if (i < 0 || i >= H || j < 0 || j >= W)
                    continue;

                val += I[(c * H * W) + (i * W) + j] * F[(k * C * FH * FW) + (c * FH * FW) + ((FH - 1 - fh) * FW) + (FW - 1 - fw)];
            }
        }

    h += P;
    w += P;
    O[(k * H * W) + (h * W) + w] = val;
}

