import argparse
import time

import numpy as np

def dp(vec_dim, vec_A, vec_B):
    res = 0.0
    for i in range(vec_dim):
        res += vec_A[i]*vec_B[i]

    return res

def main(args):
    vec_A = np.ones(args.vec_dim, dtype=np.float32)
    vec_B = np.ones(args.vec_dim, dtype=np.float32)

    exec_times = []

    for rep in range(args.num_reps):
        start = time.monotonic() 
        res = dp(args.vec_dim, vec_A, vec_B)
        end = time.monotonic()

        if rep >= args.num_reps/2:
            exec_times.append(end-start)

    avg_exec_time = 0
    for time_ in exec_times:
        avg_exec_time += time_

    avg_exec_time /= (args.num_reps/2)

    bandwidth = ((args.vec_dim*(2*4))/avg_exec_time)/(2**30)

    flops = (args.vec_dim*2)/avg_exec_time

    print('N: {} <T>: {:.6f} sec. B: {:.3f} GB/sec. F: {:.3f} FLOPS'.format(args.vec_dim, avg_exec_time, bandwidth, flops))

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('vec_dim', type=int, help='vector space dimension', metavar='DIM')
    parser.add_argument('num_reps', type=int, help='number of repetitions for the measurement', metavar='REPS')

    args = parser.parse_args()
    main(args)
