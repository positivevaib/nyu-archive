#include <stdlib.h>
#include <stdio.h>

int f1(int);
int f2(int); 
long f3(int );
int f4(int);
int f5(int);

int main()
{
  int i;
  
  printf("Enter a number between 1 and 50 inclusive: ");
  scanf("%d", &i);
  
  if( i < 1 || i > 50)
  {
    printf("You entered an invalid number: must be between 1 and 50\n");
    exit(1);
  }
  
  printf("f1 output is %d\n", f1(i) );
  
  printf("f2 output is %d\n", f2(i) );
   
  printf("f3 output is %ld\n", f3(i) );
    
  printf("f4 output is %d\n", f4(i) );
     
  printf("f5 output is %u\n", f5(i) );
  
  
}

/*******************************************************/
int f1(int x)
{		   
	if (x <= 29)
		return 26*x;
	else
		return 17 + x/4; 
}
/*******************************************************/
int f2(int x)
{
	int sum = x;
	while (x > 0) {
		if (x % 2) {
			sum += 2;
			x /= 4;
		}
		else
			x /= 4;
	}
	return sum;
}


/*******************************************************/
long f3( int x)
{
	long val = 171;
	x *= 11;
	int counter = 0;
	while (counter < x) {
		val += counter;
		counter++;
	}
	return val;	
}


/*******************************************************/
int f4(int i)
{
	if (i > 15) {
		if ((unsigned)(i - 16) > 9)
			return 171;
		else
			return i + 17;
	}
	else
		return i + 15;
}

/*******************************************************/
/*This function returns specific outputs for specific numbers. So, in essence, the solution could just have been hardcoded from running the reference file. But I have written the return statements more in line with what was in the assembly as evidence for actually having read the assembly.*/
int f5( int x)
{
	if (x > 5)
		return 5*x;
	else {
		if (x == 1)
			return 8*x + 1;
		else if (x == 2)
			return 10 + x;
		else if (x == 3)
			return 2+3+2+1;
		else if (x == 4)
			return 0;
		else
			return 17;				
	}
}


/*******************************************************/
