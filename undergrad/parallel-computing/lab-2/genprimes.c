#include <stdio.h>
#include <stdlib.h>

#include <omp.h>

// Main func.
int main(int argc, char * argv[]) {
    // Check arguments
    if (argc != 3) {
        printf("Incorrect number of arguments passed. 3 needed. %d passed.\n", argc);
        exit(1);
    }

    // Declare (and initialize) vars.
    // Initialize upper bound for prime number search space and total threads requested
    int range = atoi(argv[1]);
    int nb_threads = atoi(argv[2]);

    // Declare array for prime number search space
    int primes[range - 1];

    // Declare vars. for time tracking
    double start_time, time_taken;

    // Initialize vars. for output generation
    int rank = 1;
    int prev_prime = 2;
    char out_filename[100] = "";

    // Generate primes
    // Initialize start time var.
    start_time = omp_get_wtime();

    #pragma omp parallel num_threads(nb_threads)
    {
    // Step 1: Generate all nbs. in search space (as specified by range).
    #pragma omp for
    for (int i = 2; i <= range; i++)
        primes[i - 2] = i;

    // Step 2: Remove composite nbs.
    #pragma omp for
    for (int j = 2; j <= ((range + 1) / 2); j++)
        if (primes[j - 2] != 0)
            for (int k = (j - 1); k < (range - 1); k++)
                if ((primes[k] % j) == 0)
                    primes[k] = 0;
    }

    // Compute and print time taken for prime number generation
    time_taken = omp_get_wtime() - start_time;
    printf("Time taken for the main part: %f\n", time_taken);

    // Generate output file
    // Create output file name
    sprintf(out_filename, "%d.txt", range);

    // Create FILE object
    FILE * out_file = fopen(out_filename, "w");
    
    if (!out_file) {
        printf("Cannot create output file %s.\n", out_filename);
        exit(1);
    }

    // Write output to FILE object
    for (int i = 0; i < (range - 1); i++) {
        if (primes[i] != 0) {
            fprintf(out_file, "%d, %d, %d\n", rank++, primes[i], (primes[i] - prev_prime));
            prev_prime = primes[i];
        }
    }

    // Close FILE object
	fclose(out_file);
}