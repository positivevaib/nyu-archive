#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <cuda.h>

unsigned int getmax(unsigned int *, unsigned int);

int main(int argc, char *argv[])
{
    unsigned int size = 0;  // The size of the array
    unsigned int i;  // loop index
    unsigned int * numbers; //pointer to the array
    
    if(argc !=2)
    {
        printf("usage: maxgpu num\n");
        printf("num = size of the array\n");
        exit(1);
    }
   
    size = atol(argv[1]);

    numbers = (unsigned int *)malloc(size * sizeof(unsigned int));
    if( !numbers )
    {
        printf("Unable to allocate mem for an array of size %u\n", size);
        exit(1);
    }    

    srand(time(NULL)); // setting a seed for the random number generator
    // Fill-up the array with random numbers from 0 to size-1 
    for( i = 0; i < size; i++)
        numbers[i] = rand()  % size;    
   
    printf(" The maximum number in the array is: %u\n", getmax(numbers, size));

    free(numbers);
    exit(0);
}

// kernel
__global__ void getmaxcu(unsigned int num[], unsigned int size, unsigned int offset) {
    __shared__ unsigned int block_num[1000];
    unsigned int t = threadIdx.x + (blockIdx.x * 1000);
    unsigned int boundary;

    if (offset != 1)
        offset = 1000;

    if (t < size) {
        block_num[threadIdx.x] = num[t*offset];

        boundary = 1000;
        if (t > (size - 1 - (size % 1000)))
            boundary = size % 1000;
        
        __syncthreads();

        while (boundary > 1) {
            if ((threadIdx.x < boundary/2) && (block_num[threadIdx.x] < block_num[threadIdx.x + (boundary+1)/2]))
                block_num[threadIdx.x] = block_num[threadIdx.x + (boundary+1)/2];

            boundary = (boundary+1)/2;

            __syncthreads();
        }

        if (threadIdx.x == 0)
            num[t] = block_num[0];
    }
}

/*
   input: pointer to an array of long int
          number of elements in the array
   output: the maximum number of the array
*/
unsigned int getmax(unsigned int num[], unsigned int size)
{
    unsigned int i;

    unsigned int * device_num;
    cudaMalloc(&device_num, size * sizeof(unsigned int));
    cudaMemcpy(device_num, num, size * sizeof(unsigned int), cudaMemcpyHostToDevice);

    unsigned int threads_per_block = 1000;
    unsigned int tot_blocks = ceil((double)size/(threads_per_block));
    for (i = 0; i < ceil((double)log10(size)/log10(1000)); i++)
        getmaxcu<<<tot_blocks, threads_per_block>>>(device_num, (int)size*(1000/pow(1000, (i+1))), i+1);

    cudaMemcpy(num, device_num, size * sizeof(unsigned int), cudaMemcpyDeviceToHost);

    cudaFree(device_num);

    return num[0];
}