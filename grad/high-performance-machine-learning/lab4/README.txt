Lab 4 - Distributed Deep Learning 

The source code is divided into two files - resnet.py and lab4.py, where resnet.py contains code for the ResNet-18 neural net, taken from kuangliu's pytorch-cifar GitHub repo., and lab4.py contains the main code governing setup and training.

In lab4.py, the lines of code relevant to specific experiments are marked either by comments specifying the experiment numbers at the end of the lines or by if-elif statements specifying the experiment numbers as modes.

Running the program:

- In order to make the execution of all required experiments easier, the bash script, exps.sh has been provided. It executes all experiments in order and it can be executed with the following command.
    bash exps.sh

- In order to run individual experiments on a single GPU, lab4.py needs to be executed by using the following command.
    CUDA_VISIBLE_DEVICES=0 python3 lab4.py path [--batch_size] [--baseline] [--mode] [--num_epochs]

- By adjusting the same command as follows, it can be used to execute lab4.py on 2 and 4 GPUs, respectively.
    CUDA_VISIBLE_DEVICES=0,1
    CUDA_VISIBLE_DEVICES=0,1,2,3

- The only required argument to execute the program is the path to the data directory which either already has the CIFAR10 dataset or where the dataset would be downloaded by the program.

- In order to specify the per GPU batch size, the --batch_size flag needs to be specified.

- In order to perform the individual experiments, the --mode argument needs to be specified with the experiment number (1, 2, 3.1, 3.2, 4). This ensures that the output is relevant to the experiment of interest.

- The --num_epochs argument can be used to specify the number of training epochs.

- The --baseline argument is required with modes 3.1 and 3.2 and it is used to specify the single GPU runtime for equivalent batch size setup.

