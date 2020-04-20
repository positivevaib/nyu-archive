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
    print("using {} GPU(s)".format(torch.cuda.device_count()))

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
                                              batch_size=(torch.cuda.device_count() *args.batch_size),
                                              shuffle=True,
                                              num_workers=2)

    # setup neural net, training loss function and optimization algorithm
    device = torch.device(
        'cuda:0' if torch.cuda.is_available() else 'cpu')

    net = resnet.ResNet18(False)

    if torch.cuda.device_count() > 1:
        net = nn.DataParallel(net)

    net.to(device)

    criterion = nn.CrossEntropyLoss()

    optimizer = optim.SGD(net.parameters(), lr=0.1, momentum=0.9, weight_decay=0.0005)

    # train
    print('training')

    for epoch in range(args.num_epochs):
        train_time = 0  # Q1
        running_time = 0  # Q2
        running_start = time.perf_counter()  # Q2
        for i, data in enumerate(trainloader, 0):
            train_start = time.perf_counter()  # Q1/Q2
            inputs, labels = data[0].to(device), data[1].to(device)

            optimizer.zero_grad()

            outputs = net(inputs)
            loss = criterion(outputs, labels)
            loss.backward()
            optimizer.step()

            train_time += time.perf_counter() - train_start  # Q1
            running_time += time.perf_counter() - running_start  # Q2

            print('[{}, {}]     '.format(epoch + 1, i + 1), end = '\r')

            running_start = time.perf_counter()  # Q2

        if epoch != 0:
            if args.mode == 1:
                print("train time per epoch: {} s.".format(train_time))
            elif args.mode == 2:
                print("train time per epoch: {} s.".format(running_time))

    print('finished training')

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('path', type=str, help='data path')
    parser.add_argument('--batch_size', type=int, default=32, help='training batch size per gpu', metavar='')
    parser.add_argument('--mode', type=int, default=1, help='type of output specific to coding exercises - 1, 2, 3, 4', metavar='')
    parser.add_argument('--num_epochs',
                        type=int,
                        default=2,
                        help='number of training epochs',
                        metavar='')

    args = parser.parse_args()
    main(args)
