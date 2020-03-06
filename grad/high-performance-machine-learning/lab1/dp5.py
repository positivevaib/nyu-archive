# import dependencies
import argparse
import time

import numpy as np

# func. to benchmark
def dp(N,A,B):
    R = np.dot(A, B)
    return R

def main(args):
    # def. constants
    FLOAT = 4
    GIGA = 2**30
    
    # initialize result var.
    res = 0.

    # vecs. as np arrays 
    A = np.ones(args.N,dtype=np.float32)
    B = np.ones(args.N,dtype=np.float32)

    # measure exec. times and store measurements in secs.
    exec_times = []

    for _ in range(args.M):
        start = time.monotonic() 
        res = dp(args.N, A, B)
        end = time.monotonic()

        exec_times.append(end-start)

    # compute average exec. time and and use the value to compute bandwidth and FLOPS in GB/sec. and GFLOPS, respectively
    avg_time = 0.
    for time_ in exec_times[int(args.M/2):]:
        avg_time += time_

    avg_time /= (args.M/2)

    bw = (args.N*(2*FLOAT))/(avg_time*GIGA)

    flops = (args.N*2)/(avg_time*GIGA)

    print('N: {} <T>: {:.6f} sec. B: {:.6f} GB/sec. F: {:.6f} FLOPS result: {:.6f}'.format(args.N, avg_time, bw, flops, res))

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('N', type=int, help='vector space dimension', metavar='DIM')
    parser.add_argument('M', type=int, help='number of repetitions for the measurement', metavar='REPS')

    args = parser.parse_args()
    main(args)
