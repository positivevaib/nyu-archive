#include <stdio.h>
#include <stdlib.h>

#include <cuda.h>
#include <math.h>

#define CHUNK 1000
#define THREADS 1024

// Declare functions
unsigned int find_max(unsigned int *, unsigned int);
__global__ void kernel(unsigned int *, unsigned int);

/**************************************************************/
unsigned int maxseq(unsigned int num[], unsigned int size)
{
  unsigned int i;
  unsigned int max = num[0];

  for(i = 1; i < size; i++)
	if(num[i] > max)
	   max = num[i];

  return( max );
}
/**************************************************************/
 
// Define main function
int main(int argc, char ** argv) {
    // Declare (and initialize) variables
    // Declare original array and its length
    unsigned int * array;
    unsigned int len;

    // Declare sub-array and temporary storage variable and initialize max to hold per block maxes
    unsigned int sub_array[CHUNK];
    unsigned int temp;
    unsigned int max = 0;

    // Declare for loop iterator
    unsigned int i, j;
    
    // Validate command line arguments
    if(argc !=2) {
       printf("Incorrect number of arguments passed.\n\nUsage: a.out num\nnum = array length");
       exit(1);
    }
   
    // Get input through command line arguments and initiate array
    len = atol(argv[1]);
    array = (unsigned int *)malloc(len * sizeof(unsigned int));
    if(!array) {
       printf("Unable to allocate array of size %u.\n", len);
       exit(1);
    }    

    // Set random seed and fill array
    srand(time(NULL));
    for(i = 0; i < len; i++)
       array[i] = rand() % len;

    // Print array max using sequential algorithm 
    printf("The maximum seq number in the array is: %u\n", maxseq(array, len));

    // Find and print the max by processing sub-arrays over multiple kernel invocations
    for(i = 0; i < len; i += CHUNK) {
        // Fill sub-array
        for (j = 0; j < CHUNK; j++)
            sub_array[j] = array[i + j];

        // Process sub-array to find max and update best max
        temp = find_max(sub_array, CHUNK);
        if (temp > max)
            max = temp; 
    }

    printf("The maximum number in the array is: %u\n", max);

    // Free allocated memory
    free(array);

    exit(0);
}

// Define function to invoke kernel and get max
unsigned int find_max(unsigned int * array, unsigned int len) {
    // Allocate device memory and transfer data
    unsigned int * array_dev;
    cudaMalloc(&array_dev, (len * sizeof(unsigned int)));
    cudaMemcpy(array_dev, array, (len * sizeof(unsigned int)), cudaMemcpyHostToDevice);

    // Setup blocks and threads and invoke kernel
    int tot_blocks = ceil((double)len/(THREADS * 2));
    kernel<<<tot_blocks, THREADS>>>(array_dev, len);

    // Transfer results to host memory
    cudaMemcpy(array, array_dev, (len * sizeof(int)), cudaMemcpyDeviceToHost);

    // Free device memory
    cudaFree(array_dev);

    // Return max
    return(array[0]);
}

__global__ void kernel(unsigned int * array_dev, unsigned int len) {
    while (len > 1) {
        // Use threads to compare at most two array elements and store the higher element in the lower indexed array slot
        if ((threadIdx.x + (blockIdx.x * blockDim.x)) < (len/2))
            if (array_dev[threadIdx.x + (blockIdx.x * blockDim.x)] < array_dev[threadIdx.x + (blockIdx.x * blockDim.x) + len/2])
                array_dev[threadIdx.x + (blockIdx.x * blockDim.x)] = array_dev[threadIdx.x + (blockIdx.x * blockDim.x) + len/2];
        
        __syncthreads();

        // Use thread 0 to compare and store the higher element among the first and the last in an odd sized array
        if ((threadIdx.x + (blockIdx.x * blockDim.x)) == 0)
            if (((len % 2) != 0) && (array_dev[0] < array_dev[len - 1]))
                array_dev[0] = array_dev[len - 1];

        __syncthreads();

        // Half the array length of interest for further comparisons
        len /= 2;
    }
}