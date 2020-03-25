# import dependencies
import argparse
import time

import torch
import torch.nn as nn
import torch.optim as optim
import torchvision
import torchvision.transforms as transforms

import resnet


def main(args):
    # load training data
    transformations = transforms.Compose([
        transforms.RandomCrop(32, padding=4),
        transforms.RandomHorizontalFlip(0.5),
        transforms.ToTensor(),
        transforms.Normalize((0.4914, 0.4822, 0.4465),
                             (0.2023, 0.1994, 0.2010),
                             inplace=True)
    ])

    trainset = torchvision.datasets.CIFAR10(args.path,
                                            train=True,
                                            transform=transformations,
                                            download=True)
    trainloader = torch.utils.data.DataLoader(trainset,
                                              batch_size=128,
                                              shuffle=True,
                                              num_workers=args.num_workers)

    # setup neural net, training loss function and optimization algorithm 
    device = torch.device(
        'cuda:0' if args.cuda and torch.cuda.is_available() else 'cpu')
    print('device: {}'.format(device))

    net = resnet.ResNet18().to(device)

    criterion = nn.CrossEntropyLoss()

    optimizers_dict = {
        'SGD':
        optim.SGD(net.parameters(), lr=0.1, momentum=0.9, weight_decay=0.0005),
        'Nesterov':
        optim.SGD(net.parameters(),
                  lr=0.1,
                  momentum=0.9,
                  weight_decay=0.0005,
                  nesterov=True),
        'Adagrad':
        optim.Adagrad(net.parameters(), lr=0.1, weight_decay=0.0005),
        'Adadelta':
        optim.Adadelta(net.parameters(), lr=0.1, weight_decay=0.0005),
        'Adam':
        optim.Adam(net.parameters(), lr=0.1, weight_decay=0.0005)
    }
    optimizer = optimizers_dict[args.optim]

    # train and output minibatch training loss, minibatch top-1 training accuracy and time measurements
    print('\ntraining')

    total_data_load_time = 0.0
    for epoch in range(5):
        epoch_time_start = time.perf_counter()
        data_load_time = 0.0
        train_time = 0.0
        for i, data in enumerate(trainloader, 0):
            data_time_start = time.perf_counter()
            inputs, labels = data
            data_time_end = time.perf_counter()
            data_load_time += data_time_end - data_time_start

            optimizer.zero_grad()

            train_time_start = time.perf_counter()
            outputs = net(inputs)
            loss = criterion(outputs, labels)
            loss.backward()
            optimizer.step()
            train_time_end = time.perf_counter()
            train_time += train_time_end - train_time_start

            _, predicted = torch.max(outputs.data, 1)
            correct = (predicted == labels).sum().item()
            total = labels.size(0)

            print('[{}, {}] loss: {}, acc: {}%'.format(epoch + 1, i + 1,
                                                       loss.item(),
                                                       correct / total * 100))

        epoch_time_end = time.perf_counter()
        print(
            '[{}] data load time: {} s., train time: {} s., epoch time: {} s.'.
            format(epoch, data_load_time, train_time,
                   epoch_time_end - epoch_time_start))

        total_data_load_time += data_load_time

    print('\nfinished training')


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('path', type=str, help='data path')
    parser.add_argument('--cuda', action='store_true', help='use GPU')
    parser.add_argument('--num_workers',
                        type=int,
                        default=2,
                        help='number of dataloader workers')
    parser.add_argument('--optim',
                        type=str,
                        default='SGD',
                        help='type of optimizer')

    args = parser.parse_args()
    main(args)
