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
    print('\ndevice: {}'.format(device))

    net = resnet.ResNet18(args.batch_norm).to(device)

    print('\ntotal trainable parameters: {}'.format(
        sum(param.numel() for param in net.parameters()
            if param.requires_grad)))  # Q3/Q4

    criterion = nn.CrossEntropyLoss()

    optimizers_dict = {
        'sgd':
        optim.SGD(net.parameters(), lr=0.1, momentum=0.9, weight_decay=0.0005),
        'nesterov':
        optim.SGD(net.parameters(),
                  lr=0.1,
                  momentum=0.9,
                  weight_decay=0.0005,
                  nesterov=True),
        'adagrad':
        optim.Adagrad(net.parameters(), lr=0.1, weight_decay=0.0005),
        'adadelta':
        optim.Adadelta(net.parameters(), lr=0.1, weight_decay=0.0005),
        'adam':
        optim.Adam(net.parameters(), lr=0.1, weight_decay=0.0005)
    }
    optimizer = optimizers_dict[args.optim]
    print('\noptimizer: {}'.format(args.optim))

    # train
    print('\ntraining\n')

    tot_epoch_time = 0.0  # C5
    tot_dataloader_time = 0.0  # C3/C4
    tot_train_time = 0.0  # C4
    for epoch in range(args.num_epochs):
        epoch_start = time.perf_counter()  # C2/C5
        dataloader_time = 0.0  # C2/C3/C4
        train_time = 0.0  # C2/C4
        running_loss = 0.0  # C6/C7
        running_correct = 0  # C6/C7
        running_total = 0  # C6/C7
        dataloader_start = time.perf_counter()  # C2/C3/C4
        for i, data in enumerate(trainloader, 0):
            inputs, labels = data[0].to(device), data[1].to(device)
            dataloader_time += time.perf_counter(
            ) - dataloader_start  # C2/3/C4

            optimizer.zero_grad()

            train_start = time.perf_counter()  # C2/C4
            outputs = net(inputs)
            loss = criterion(outputs, labels)
            loss.backward()
            optimizer.step()
            train_time += time.perf_counter() - train_start  # C2/C4

            running_loss += loss.item()  # C6/C7

            _, predicted = torch.max(outputs.data, 1)
            correct = (predicted == labels).sum().item()
            total = labels.size(0)

            running_correct += correct  # C6/C7
            running_total += total  # C6/C7

            if args.mode == 1:
                print(
                    '[epoch {}, mini-batch {}] training loss: {}, top-1 training accuracy: {}%'
                    .format(epoch + 1, i + 1, loss.item(),
                            correct / total * 100))

            dataloader_start = time.perf_counter()  # C2/C3/C4

        epoch_end = time.perf_counter()  # C2/C5

        if args.mode == 2:  # C2
            print(
                '[epoch {}] data loading time: {} s., training (mini-batch calculation) time: {} s., total running time: {} s.'
                .format(epoch + 1, dataloader_time, train_time,
                        epoch_end - epoch_start))

        if args.mode == 6:  # C6
            print(
                '[epoch {}] training time: {}, training loss: {}, top-1 training accuracy: {}%'
                .format(epoch + 1, train_time, running_loss / len(trainloader),
                        running_correct / running_total * 100))

        if args.mode == 7:  # C7
            print('[epoch {}] training loss: {}, top-1 training accuracy: {}%'.
                  format(epoch + 1, running_loss / len(trainloader),
                         running_correct / running_total * 100))

        tot_epoch_time += epoch_end - epoch_start  # C5
        tot_dataloader_time += dataloader_time  # C3/C4
        tot_train_time += train_time  # C4

    print('\nfinished training')

    print('\ntotal number of gradients: {}'.format(
        sum(param.grad.numel() for param in net.parameters()
            if param.requires_grad)))  # Q3/Q4

    if args.mode == 3:  # C3
        print('\ntotal DataLoader time: {} s.'.format(tot_dataloader_time))

    if args.mode == 4:  # C4
        print('\ntotal data loading time: {} s., total computing time: {} s.'.
              format(tot_dataloader_time, tot_train_time))

    if args.mode == 5:  # C5
        print('\naverage running time over 5 epochs: {} s. per epoch'.format(
            tot_epoch_time / 5))


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('path', type=str, help='data path')
    parser.add_argument('--batch_norm',
                        action='store_true',
                        help='use batch norm layers')
    parser.add_argument('--cuda', action='store_true', help='use GPU')
    parser.add_argument(
        '--mode',
        type=int,
        default=1,
        help=
        'type of output specific to coding exercises - 1, 2, 3, 4, 5, 6, 7',
        metavar='')
    parser.add_argument('--num_epochs',
                        type=int,
                        default=5,
                        help='number of training epochs',
                        metavar='')
    parser.add_argument('--num_workers',
                        type=int,
                        default=2,
                        help='number of dataloader workers',
                        metavar='')
    parser.add_argument(
        '--optim',
        type=str,
        default='sgd',
        help='type of optimizer - sgd, nesterov, adagrad, adadelta, adam',
        metavar='')

    args = parser.parse_args()
    main(args)
