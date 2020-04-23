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
    print('using {} GPU(s)'.format(torch.cuda.device_count()))

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
    trainloader = torch.utils.data.DataLoader(
        trainset,
        batch_size=(torch.cuda.device_count() * args.batch_size),
        shuffle=True,
        num_workers=2)

    # setup neural net, training loss function and optimization algorithm
    device = torch.device('cuda:0' if torch.cuda.is_available() else 'cpu')

    net = resnet.ResNet18(True)
    bn_activations = (112 * 112 * 64) + (4 * (64 * 56 * 56)) + (
        5 * (128 * 28 * 28)) + (5 * (256 * 14 * 14)) + (5 * (512 * 7 * 7))

    if torch.cuda.device_count() > 1:
        net = nn.DataParallel(net)

    net.to(device)

    criterion = nn.CrossEntropyLoss()

    optimizer = optim.SGD(net.parameters(),
                          lr=0.1,
                          momentum=0.9,
                          weight_decay=0.0005)

    # train
    print('training')

    for epoch in range(args.num_epochs):
        run_time = 0  # Q1/Q2/Q3
        running_loss = 0  # Q4
        correct = 0  # Q4
        total = 0  # Q4

        if args.mode == 2:
            run_start = time.perf_counter()

        for i, data in enumerate(trainloader, 0):
            if args.mode == 1 or int(args.mode) == 3:
                run_start = time.perf_counter()
            inputs, labels = data[0].to(device), data[1].to(device)

            optimizer.zero_grad()

            outputs = net(inputs)
            loss = criterion(outputs, labels)
            loss.backward()
            optimizer.step()

            if args.mode == 1 or args.mode == 2 or int(args.mode) == 3:
                run_time += time.perf_counter() - run_start

            if args.mode == 4:
                running_loss += loss.item()
                _, predicted = torch.max(outputs.data, 1)
                correct += (predicted == labels).sum().item()
                total += labels.size(0)

            print('[{}, {}]             '.format(epoch + 1, i + 1), end='\r')

            if args.mode == 2:
                run_start = time.perf_counter()

        if epoch != 0:
            if args.mode == 1:
                print('train time per epoch: {} s.'.format(run_time))
            elif args.mode == 2:
                print('train time per epoch: {} s.'.format(run_time))
            elif args.mode == 3.1:
                print('computation time per epoch {} s.'.format(
                    args.baseline / torch.cuda.device_count()))
                print('communication time per epoch {} s.'.format(
                    run_time - (args.baseline / torch.cuda.device_count())))
            elif args.mode == 3.2:
                print('bandwidth utilization: {} GB/S.'.format(
                    (2 * len(trainset) *
                     (sum(param.numel() for param in net.parameters()
                          if param.requires_grad) + bn_activations + 1) *
                     (torch.cuda.device_count() - 1) * 4 / 1e9) /
                    (run_time - (args.baseline / torch.cuda.device_count()))))
            elif args.mode == 4 and epoch == 4:
                print('5th epoch - avg.train loss: {}, train acc.: {}%'.format(
                    running_loss / len(trainloader), correct / total * 100))


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('path', type=str, help='data path')
    parser.add_argument('--batch_size',
                        type=int,
                        default=32,
                        help='training batch size per gpu',
                        metavar='')
    parser.add_argument(
        '--baseline',
        type=float,
        default=None,
        help='training time baseline as measured with 1 GPU setup',
        metavar='')
    parser.add_argument('--mode',
                        type=float,
                        choices={1, 2, 3.1, 3.2, 4},
                        default=1,
                        help='type of output specific to coding exercises',
                        metavar='')
    parser.add_argument('--num_epochs',
                        type=int,
                        default=2,
                        help='number of training epochs',
                        metavar='')

    args = parser.parse_args()

    if int(args.mode) == 3 and not args.baseline:
        print('--mode' + args.mode + 'requires --baseline')
        quit()
    elif args.mode == 4 and not args.num_epochs >= 5:
        print('--mode 4 requires at least 5 epochs')
        quit()

    main(args)
