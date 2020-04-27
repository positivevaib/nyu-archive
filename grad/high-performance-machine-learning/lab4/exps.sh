#!/bin/bash

printf "Q1\n"

for sz in 32 128 512
do
    printf "\nbatch size $sz per GPU\n"
    CUDA_VISIBLE_DEVICES=0 python3 lab4.py cifar10 --batch_size $sz --mode 1 --num_epochs 2
done

printf "\nQ2\n"

for gpu in 2 4
do
    for sz in 32 128 512
    do
        printf "\nbatch size $sz per GPU - $gpu GPUs\n"
        if [ $gpu == 2 ]
        then
            CUDA_VISIBLE_DEVICES=0,1 python3 lab4.py cifar10 --batch_size $sz --mode 2 --num_epochs 2
        elif [ $gpu == 4 ]
        then
            CUDA_VISIBLE_DEVICES=0,1,2,3 python3 lab4.py cifar10 --batch_size $sz --mode 2 --num_epochs 2
        fi
    done
done

printf "\nQ3\n"

printf "\nenter baselines: "
read base_32 base_128 base_512

for m in 3.1 3.2
do
    printf "\nQ$m\n"

    for gpu in 2 4
    do
        for sz in 32 128 512
        do
            if [ $sz == 32 ]
            then
                base=$base_32
            elif [ $sz == 128 ]
            then 
                base=$base_128
            else
                base=$base_512
            fi

            printf "\nbatch size $sz per GPU - $gpu GPUs\n"
            if [ $gpu == 2 ]
            then
                CUDA_VISIBLE_DEVICES=0,1 python3 lab4.py cifar10 --batch_size $sz --baseline $base --mode $m --num_epochs 2
            elif [ $gpu == 4 ]
            then
                CUDA_VISIBLE_DEVICES=0,1,2,3 python3 lab4.py cifar10 --batch_size $sz --baseline $base --mode $m --num_epochs 2
            fi
        done
    done
done  

printf "\nQ4\n"

CUDA_VISIBLE_DEVICES=0,1,2,3 python3 lab4.py cifar10 --batch_size 512 --mode 4 --num_epochs 5

