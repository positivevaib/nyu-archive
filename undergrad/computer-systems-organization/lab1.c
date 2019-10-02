
/*
 * IMPORTANT: WRITE YOUR NAME AND NetID BELOW.
 * 
 * Last Name: Gadodia
 * First Name: Vaibhav
 * Netid: vag273
 * 
 * You will do your project in this file only.
 * Do not change function delarations. However, feel free to add new functions if you want.
 * 
 */


#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>


/* Function declarations: do not change that, but you may add your own functions */
void f1(int, int, int);
void f2(char *);
void f3(char *, int);
void f4(int *, int);
void f5(int, int);

/* Add here function declarations of your own, if any. */


/*********************************************************************************/

/* 
 * Do  NOT change anything in main function 
 */
int main(int argc, char * argv[])
{
  int option = 0;
  int i, j, k;
  int * list;
  
  if(argc < 2 )
  {
     fprintf(stderr,"Usage: lab1 num [input]\n");
     fprintf(stderr,"num: 1, 2, 3, 4, or 5\n");
     exit(1);
  }
  
  option = atoi(argv[1]);
  
  switch(option)
  {
    case 1: if(argc != 5)
	    {
	      fprintf(stderr,"Usage: lab1 1 A B C\n");
	      fprintf(stderr,"A, B, and C: Positive integers where A <= B and C < B \n");
	      exit(1);
	    }
	    i = atoi(argv[2]);
	    j = atoi(argv[3]);
	    k = atoi(argv[4]);
	   
	    f1(i, j, k);
	    
	    break;
	    
	    
    case 2: if(argc != 3)
	    {
	      fprintf(stderr,"Usage: lab1 2 filename\n");
	      fprintf(stderr,"filename: the name of the file containing the characters\n");
	      exit(1);
	    }
	    
	    f2(argv[2]);
	    
	    break;

	    
    case 3: if(argc != 4)
	    {
	      fprintf(stderr,"Usage: lab1 3 filename gen\n");
	      fprintf(stderr,"filename: the name of the file containing the characters\n");
	      fprintf(stderr,"gen: number of generations nonzero positive integer \n");
	      exit(1);
	    }
	    
	    i = atoi(argv[3]);
	    f3(argv[2], i);
	    
	    break; 
	    
	    
    case 4: if(argc != 3)
	    {
	      fprintf(stderr,"Usage: lab1 4 num\n");
	      fprintf(stderr,"num: nonzero positive integer = number of elements in the array\n");
	      exit(1);
	    }
	    
	    i = atoi(argv[2]);
	    if(!(list = (int *)malloc(i*sizeof(int))))
	    {
	      fprintf(stderr,"Cannot allocate an integer array of %d elements\n", i);
	      exit(0);
	    }
	    for(j = 0; j < i; j++)
	    {
	      printf("enter element %d: ", j);
	      scanf("%d",&list[j]);
	    }
	    
	    f4(list, i);
	    
	    break;

	    
    case 5: if(argc != 4)
	    {
	      fprintf(stderr,"Usage: lab1 5 A B\n");
	      fprintf(stderr,"A B: positive nonzero integers where A <= B\n");
	      exit(1);
	    } 
	    
	    i = atoi(argv[2]);
	    j = atoi(argv[3]);
	    
	    f5(i, j);
	    
	    break;

	          
	    
    default: fprintf(stderr, "You entered an invalid option!\n");
	     exit(1);
  }
  
  return 0;
}


/********************************************************************************************/
/******* Start filling the blanks from here and add any extra functions you want, if any *****/

/*
 * function 1:
 * intput: start, end, and increment
 * output: print on the screen: start start+incr start+2incr ... end
 * Example: start = 1, end = 5, incr = 3
 * f1 must print on the screen (pay attention to spaces): 1 4 5
 * At the end, print a new line before returning
 * */
void f1(int start, int end, int incr)
{
	// To print the correct range, we begin by printing start and keep incrementing it by the amount incr until it becomes greater than or equal to the end value. When that happens, we exit the loop and print the end value.
	while (start < end) {
		printf("%d ", start);
		start += incr;
	}
	
	printf("%d\n", end );  
}


/*********************************************************************************/

/* 
 * function 2:
 * intput: filename for a file that contains characters in upper/lower case as well as spaces
 * output: filename.reverse that contains the same characters as the original but the case is 
 *         reversed and the spaces are intact
 * Note1: filename can be any name, for example: info.txt or characters or data.info, ...
 * Note2: The given file will not contain anything but alphabets and spaces.
 */
void f2(char * filename)
{
	// We begin by creating a pointer to the file that we want to read the data from.
	FILE * original;
	original = fopen(filename, "r");

	// Next, we create a new file to write the reversed text to.
	FILE * new;
	char newname[100];
	strcpy(newname, filename);
	strcat(newname, ".reverse");
	new = fopen(newname, "w");
	
	// We read the original file one char at a time and change the case if it is an alphabet, otherwise we simply keep it as it is. After the conversion (or not in the non-alphabetic cases), we write that char to the reverse file.
	char ch;
	while (fscanf(original, "%c", &ch) == 1) {
		if (islower(ch)) {
			fprintf(new, "%c", ch-32);
		}
		else if (isupper(ch)) {
			fprintf(new, "%c", ch+32);
		}		
		else {
			fprintf(new, "%c", ch);
		}
	}

	// We finally close the files.
	fclose(original);
	fclose(new);
}


/*********************************************************************************/

/*
 * function 3:
 * Two inputs: - filename that contains a 3x3 matrix of cels. Each cell is either 1 or  0. 
 *             - a positive number of generations
 * output: print on the screen the 3x3 matrix, one row per line,
 *           following the following rules
 * rule 1: a cell that has 2 or 3 neighboring 1's becomes 1 in the next generation
 * rule 2: any other case, the cell becomes in the next generation 
 * Example: If the file has 0110101000 
 * It means the first row is: 011 
 * the second row is 010  and the 3rd row is 000 
 */
void f3(char *filename, int gen)
{
	// We begin by creating a pointer to the file with our matrix data and read it one char at a time, adding the valid chars ('0' - '9') to an integer array.
	FILE * data = fopen(filename, "r");
	
	int mat[9];

	int i = 0;
	char ch;
	while (fscanf(data, "%c", &ch) == 1) {
		if (ch != ' ') {
			mat[i] = ch - '0';
			i++;
		}
	}

	// We declare a new int array and for every generation, we iterate through the previous generation's matrix (stored in mat[]) and find that number's row and column in the virtual matrix. Then, we iterate through the matrix again, this time skipping any entry that is not 1 or that is the same as our focus number (who's neighbors we're checking). For every entry we then find if that entry is a neighbor or not, which can be done by first finding it's row and column and then finding if the distance is at most 1 between the focus and neighbor's row and column. If it turns out to be a neighbor, we note that down and later on (after the inner iteration is done) we change the focus value in the new_mat[] to 1 if it had 2 or 3 neighbors with value 1, otherwise we 0 it out. Finally, to prepare for the next generation, we copy the new_mat[] values to mat[]
	int new_mat[9];
	while (gen > 0) {
		for (i = 0; i < 9; i++) {
			int ones = 0;

			int focus_row = i/3;		 
			int focus_col = i % 3;
			
			for (int j = 0; j < 9; j++) {
				if (i != j && mat[j] == 1) {
					int neighbor_row = j/3;
					int neighbor_col = j % 3;
					
					if (abs(focus_row - neighbor_row) <= 1 && abs(focus_col - neighbor_col) <= 1)
						ones++;
				}
			}

			if (ones == 2 || ones == 3)
				new_mat[i] = 1;
			else
				new_mat[i] = 0;
		}

		for (i = 0; i < 9; i++) {
			mat[i] = new_mat[i];
		}
		
		gen--;
	}

	// To print the mat[] entries in the right format (as a matrix), we find if an entry's index is divisible by 3 as that means that the entry is the first in a new row of the matrix. This allows us to add a new line to the print statement. Otherwise, the entry is printed without a new line. Also, 0th entry is printed out before the loop and any evaluations because that way there is no needless new line in the beginning.
	printf("%d ", mat[0]);
	for (i = 1; i < 9; i++) {
		if ((i % 3) == 0)
			printf("\n%d ", mat[i]);
		else
			printf("%d ", mat[i]);
	}
	printf("\n");
}

/*********************************************************************************/

/*
 * function 4
 * intput: an array of n integers
 * output: print on the screen the array sorted in reverse order followed by a new line
 */
void f4(int * num, int n)
{
	// To print the reverse order, we first sort the array in descending order and then print the array iteratively.
	for (int i = 0; i < n; i++) {
		int max_index = i;
		for (int j = (i + 1); j < n; j++) {
			if (num[j] > num[max_index])
				max_index = j; 
		}
		if (max_index != i) {
			num[i] ^= num[max_index];
			num[max_index] ^= num[i];
			num[i] ^= num[max_index];
		}
	}

	for (int i = 0; i < n; i++) {
		printf("%d ", num[i]);
	}
	printf("\n");
}

/*********************************************************************************/

/* 
 * function 5:
 * input: two positive nonzero integers a and b where a <= b
 * output: print on the screen the non-prime numbers betweeb a and b, 
 *         including a and b themselves if they are non-prime. 
*          0, 1, and 2 are assumed not to be prime, in case you encounter them.
 *         Leave a space between each two numbers.
 *         At the end, print a new line.
 */
void f5(int a, int b)
{
	// To print all non primes in the range, we iterate through the range and assign nonprime to 0 (false). Then, we iterate through all numbers between 2 and one less than the focus number itself and find if any of those divides it. If the number is divisible by even one of these, then we change nonprime to 1 and break out of the inner loop, printing the number if it's nonprime, 0, 1, or 2. 0, 1 and 2 are printed as special cases because of the way that primes are defined.
	for (int num = a; num <= b; num++) {
		int nonprime = 0;
		for (int divisor = 2; divisor < num; divisor++) {
			if (num % divisor == 0) {
				nonprime = 1;
				break;
			}
		}
		if (nonprime || (num >= 0 && num <= 2))
			printf("%d ", num);	
	}
	printf("\n");
}

/*********************************************************************************/

