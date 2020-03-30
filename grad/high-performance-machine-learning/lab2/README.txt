Lab 2 - PyTorch and CIFAR10

The source code is divided into two files - resnet.py and lab2.py, where resnet.py contains code for the ResNet-18 neural net, taken from kuangliu's pytorch-cifar GitHub repo., and lab2.py contains the main code governing setup and training.

In lab2.py, the lines of code relevant to specific experiments are marked by comments specifying the experiment numbers at the end of the lines.

Running the program:

- In order to run the experiments, lab2.py needs to be executed by using the following command.
    python3 lab2.py path [--batch_norm] [--cuda] [--mode] [--num_epochs] [--num_workers] [--optim]

- The only required argument to execute the program is the path to the data directory which either already has the CIFAR10 dataset or where the dataset would be downloaded by the program.

- In order to use the batch-norm layers, the --batch_norm flag needs to be specified. This flag is needed to perform experiment C7.

- To enable GPU use, --cuda flag needs to be specified.

- In order to perform the individual experiments, the --mode argument needs to be specified with the experiment number (1, 2, ..., 7). This ensures that the output is relevant to the experiment of interest.

- The --num_epochs and --num_workers arguments can be used to specify the number of training epochs and data loading worker processes.

- To specify the optimizer, the --optim argument can be used and assigned sgd for SGD, nesterov for SGD with Nesterov momentum, adagrad for Adagrad, adadelta for Adadelta, or adam for the Adam optimization algorithm.
