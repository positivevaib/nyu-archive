#include <stdio.h>
#include <stdlib.h>

#include <omp.h>

// Main func.
int main(int argc, char * argv[]) {
    // Declare (and initialize) vars.
    int range, nb_threads;

    int i, j;

    double start_time, time_taken;

    int rank = 1;
    int prev_prime = 2;
    char out_filename[100] = "";

    // Read input
    if (argc != 3) {
        if (argc > 3) {
            printf("Cannot accept more than 2 arguments. %d  passed.\n", argc);
            exit(1);
        }
        else {
            printf("Need 2 command line arguments. %d passed.\n", argc);
            exit(1);
        }
    }

    range = atoi(argv[1]);
    if (range <= 2 || range > 100000) {
        printf("Cannot generate primes with a max range of %d. Range needs to be between 2 and 100,000.\n", range);
        exit(1);
    }

    nb_threads = atoi(argv[2]);
    if (nb_threads < 1 || nb_threads > 100) {
        printf("Cannot run with %d threads. Number of threads needs to be between 1 and 100.\n", nb_threads);
        exit(1);
    }

    // Declare array
    int primes[range - 1];

    // Set break_iter
    int break_iter = ((range + 1) / 2) - 2;

    // Generate primes
    start_time = omp_get_wtime();

    // Step 1: Generate all nbs. from 2 to the max (as specified by range).
    #pragma omp parallel num_threads(nb_threads)
    {
    #pragma omp for
    for (i = 0; i < (range - 1); i++)
        primes[i] = i + 2;

    // Step 2: Remove composite nbs.
    #pragma omp for
    for (i = 0; i < break_iter; i++)
        if (primes[i] != 0)
            for (j = (2 * (i + 1)); j < (range - 1); j += (i + 2))
                primes[j] = 0;
    }

    time_taken = omp_get_wtime() - start_time;
    printf("Time taken for the main part: %f\n", time_taken);

    // Generate output file
    sprintf(out_filename, "%d.txt", range);

    FILE * out_file = fopen(out_filename, "w");
    
    if (!out_file) {
        printf("Cannot create output file %s.\n", out_filename);
        exit(1);
    }

    for (i = 0; i < (range - 1); i++) {
        if (primes[i] != 0) {
            fprintf(out_file, "%d, %d, %d\n", rank++, primes[i], (primes[i] - prev_prime));
            prev_prime = primes[i];
        }
    }

	fclose(out_file);
}