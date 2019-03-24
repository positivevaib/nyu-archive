#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <mpi.h>
#include <stdbool.h>

// Global vars.
int nb_x;
float target_err, * x, * a, * b;

float * backup_x;
float current_err;

// Func. declaration
void input();

// Main func.
int main(int argc, char * argv[]) {
	// Initialize MPI communicator
	int comm_sz;
	int rank;

	MPI_Init(&argc, &argv);
	MPI_Comm_size(MPI_COMM_WORLD, &comm_sz);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);

	if(argc != 2) {
		printf("Usage: mpirun -n x exec_name filename\n");
		exit(1);
	}

	// Declare vars.
	int local_nb_x;
	float * local_a, * local_b, * local_updated_x;

	if (rank == 0) {
		// Declare vars.
		int i, j;

		int nb_iters;
		bool terminate;

		float sum;

		char out_filename[100] = "";
  
		// Read and store input
		input(argv[1]);

		// Broadcast overall prob. sz. and calculate local prob. sz.
		MPI_Bcast(&nb_x, 1, MPI_INT, 0, MPI_COMM_WORLD);
		local_nb_x = nb_x / comm_sz;

		// Allocate mem.
		local_a = (float *)malloc(local_nb_x * nb_x * sizeof(float));
		local_b = (float *)malloc(local_nb_x * sizeof(float));
		local_updated_x = (float *)malloc(local_nb_x * sizeof(float));

		backup_x = (float *)malloc(nb_x * sizeof(float));

		// Distribute input data
		MPI_Bcast(&target_err, 1, MPI_FLOAT, 0, MPI_COMM_WORLD);
		MPI_Bcast(x, nb_x, MPI_FLOAT, 0, MPI_COMM_WORLD);
		MPI_Scatter(a, (local_nb_x * nb_x), MPI_FLOAT, local_a, (local_nb_x * nb_x), MPI_FLOAT, 0, MPI_COMM_WORLD);
		MPI_Scatter(b, local_nb_x, MPI_FLOAT, local_b, local_nb_x, MPI_FLOAT, 0, MPI_COMM_WORLD);
		
		// Solve lin. eqns.
		nb_iters = 0;
		terminate = false;

		while (!terminate) {
			nb_iters++;

			// Backup x vals.
			for (i = 0; i < nb_x; i++)
				backup_x[i] = x[i];

			// Calculate x vals.
			for (i = 0; i < local_nb_x; i++) {
    			sum = 0;
    			for (j = 0; j < nb_x; j++) {
     				if (j != i)
      					sum += local_a[(i * nb_x) + j] * x[j];
    			}
    				local_updated_x[i] = (b[i] - sum) / a[(i * nb_x) + i];
   			}

			// Gather updated x vals.
   			MPI_Allgather(local_updated_x, local_nb_x, MPI_FLOAT, x, local_nb_x, MPI_FLOAT, MPI_COMM_WORLD); 
   
			// Check and broadcast prog. termination condition
   			terminate = true;
   			for (i = 0; i < nb_x; i++) {
    			current_err = fabs((x[i] - backup_x[i]) / x[i]);    
    			if (current_err > target_err) {
     				terminate = false;
     				break;
    			}
   			}

   			MPI_Bcast(&terminate, 1, MPI_BYTE, 0, MPI_COMM_WORLD);
  		}
   
		// Write results to output file
		sprintf(out_filename, "%d.sol", nb_x);

		FILE * out_file = fopen(out_filename, "w");
		if(!out_file) {
			printf("Cannot create the file '%s'.\n", out_filename);
			exit(1);
		}
		
		for( i = 0; i < nb_x; i++)
			fprintf(out_file, "%f\n", x[i]);
	
		// Close output file
		fclose(out_file);

		// Print output
		printf("Total number of iterations: %d\n", nb_iters);	
	}
	else {
		// Declare vars.
  		int i, j;

  		bool terminate;
		float sum;

  		// Receive overall prob. sz. and calculate local prob. sz. and offset
  		MPI_Bcast(&nb_x, 1, MPI_INT, 0, MPI_COMM_WORLD);

  		local_nb_x = nb_x / comm_sz;
		int offset = rank * local_nb_x;

		// Allocate mem.
  		local_a = (float *)malloc((local_nb_x * nb_x) * sizeof(float));
  		local_b = (float *)malloc(local_nb_x * sizeof(float));
  		local_updated_x = (float *)malloc(local_nb_x * sizeof(float));

		x = (float *)malloc(nb_x * sizeof(float));

		// Receive input data
		MPI_Bcast(&target_err, 1, MPI_FLOAT, 0, MPI_COMM_WORLD);
		MPI_Bcast(x, nb_x, MPI_FLOAT, 0, MPI_COMM_WORLD);
		MPI_Scatter(a, (local_nb_x * nb_x), MPI_FLOAT, local_a, (local_nb_x * nb_x), MPI_FLOAT, 0, MPI_COMM_WORLD);
		MPI_Scatter(b, local_nb_x, MPI_FLOAT, local_b, local_nb_x, MPI_FLOAT, 0, MPI_COMM_WORLD);
		
		// Solve lin. eqns.
		terminate = false;

  		while (!terminate) {
			// Calculate x vals.
   			for (i = offset; i < (offset + local_nb_x); i++) {
    			sum = 0;
    			for (j = 0; j < nb_x; j++) {
     				if (j != i)
      					sum += local_a[(i % offset) + j] * x[j];
    			}

    			local_updated_x[(i % offset)] = (local_b[(i % offset)] - sum) / local_a[(i % offset) + i];
   			}

			// Gather updated x vals.
   			MPI_Allgather(local_updated_x, local_nb_x, MPI_FLOAT, x, local_nb_x, MPI_FLOAT, MPI_COMM_WORLD); 
   
			// Receive prog. termination condition
   			MPI_Bcast(&terminate, 1, MPI_BYTE, 0, MPI_COMM_WORLD);
  		}
	}

	// Close MPI communicator
 	MPI_Finalize();

	// Exit prog.
 	exit(0);
}

// Func. to read input
void input(char filename[]) {
	// Declare vars.
	int i, j;
 
	// Open file
	FILE * in_file = fopen(filename, "r");
	if (!in_file) {
    	printf("Cannot open file '%s'.\n", filename);
    	exit(1);
	}

	// Read and store input
	// x
	fscanf(in_file, "%d", &nb_x);
	fscanf(in_file, "%f", &target_err);

	x = (float *)malloc(nb_x * sizeof(float));
	if(!x) {
		printf("Cannot allocate array 'x'.\n");
		exit(1);
	}

	for (i = 0; i < nb_x; i++)
		fscanf(in_file, "%f", &x[i]);

	// a and b
	a = (float *)malloc((nb_x * nb_x) * sizeof(float));
	if(!a) {
		printf("Cannot allocate array 'a'.\n");
		exit(1);
	}

 	b = (float *)malloc(nb_x * sizeof(float));
	if(!b) {
		printf("Cannot allocate array 'b'.\n");
		exit(1);
	}

	for(i = 0; i < nb_x; i++) {
		for(j = 0; j < nb_x; j++)
			fscanf(in_file, "%f", &a[(i * nb_x) + j]);
   
   		fscanf(in_file, "%f", &b[i]);
 	}

	// Close file
	fclose(in_file); 
}