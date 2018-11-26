# import dependencies
import sys
import copy

def naive_multiplication(f, g):
    # initialize product
    product = [0]*(2*len(f) - 1)

    # multiply
    for i in range(len(g)):
        for j in range(len(f)):
            product[i + j] = product[i + j] + g[i]*f[j]

    return product

def karatsuba_multiplication(f, g):
    # use recursion for large degrees and naive_multiplication for smaller degrees
    degree = len(f) - 1
    if degree > 64:
        # split polynomials
        f_lo = f[:int(degree/2) + 1]
        f_hi = f[int(degree/2) + 1:]

        g_lo = g[:int(degree/2) + 1]
        g_hi = g[int(degree/2) + 1:]
    
        # compute sums
        F = copy.deepcopy(f_hi)
        G = copy.deepcopy(g_hi)
        
        for i in range(len(f_lo)):
            F[i] += f_lo[i]
            G[i] += g_lo[i]

        # multiply
        U = karatsuba_multiplication(f_hi, g_hi)
        V = karatsuba_multiplication(f_lo, g_lo)
        W = karatsuba_multiplication(F, G)

        product = [0]*(2*degree + 1)
        for i in range(len(V)):
            product[i] = V[i]
        
        for i in range(len(U)):
            product[i + int(degree/2) + 1] += W[i] - U[i] - V[i]

        for i in range(len(U)):
            product[i + degree + 1] += U[i]

        return product
    else:
        # multiply
        return naive_multiplication(f, g)


def main():
    # read input
    input_data = sys.stdin.read().split('\n')

    degree = int(input_data[0])

    f = []
    for coefficient in input_data[1].split(' '):
        f.append(int(coefficient))

    g = []
    for coefficient in input_data[2].split(' '):
        g.append(int(coefficient))

    # multiply
    product = karatsuba_multiplication(f, g)

    # print output
    sys.stdout.write(' '.join(str(coefficient) for coefficient in product))

main()