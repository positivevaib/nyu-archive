#Define Object class to reflect given objects
class Object:
    #Define variables
    name = ''
    index = 0
    weight = 0
    value = 0

    #Define constructor
    def __init__(self, name, index, weight, value):
        self.name = name
        self.index = index
        self.weight = weight
        self.value = value

#Define State class to reflect state spaces
class State:
    #Define variables
    objects = []
    weight = 0
    value = 0

    #Define constructor
    def __init__(self, objects):
        self.objects = objects

    #Define function to add objects
    def add(self, object):
        self.objects.append(object)
        self.weight += object.weight
        self.value += object.value

    #Define function to remove objects
    def remove(self):
        self.weight -= self.objects[-1].weight
        self.value -= self.objects[-1].value
        self.objects = self.objects[:-1]

#Define function to search using Iterative Deeping Algorithm and return the solution if it exists
def IDS(state, depth, object_dict, T, M):
    #Return state if it is a solution
    if depth == 0:
        if state.value >= T and state.weight <= M:
            return state
        else:
            return None
    #Use recursion to search deeper and broader
    else:
        for i in range(state.objects[-1].index + 1, len(object_dict) + 1):
            state.add(object_dict[str(i)])
            state_found = IDS(state, depth - 1, object_dict, T, M)
            if state_found:
                return state_found
            state.remove()
        return None

#Define main function
def main():
    #Read input and store necessary information
    object_dict = {}

    file = open('input2.txt', 'r')
    i = 1
    for line in file:
        line = line.strip().split()
        #Assign target value and max weight to T and M respectively
        if len(line) == 2:
            T = int(float(line[0]))
            M = int(float(line[1]))
        #Add objects to object_dict
        else:
            name = line[0]
            value = int(float(line[1]))
            weight = int(float(line[2]))

            object_dict[str(i)] = Object(name, i, weight, value)
            i += 1

    #Search state space using IDS and print solution
    for i in range(1, len(object_dict) + 1):
        solution = IDS(State([Object('zero', 0, 0, 0)]), i, object_dict, T, M)
        if solution:
            print('Solution:', end = ' ')
            for i in range(1, len(solution.objects)):
                print(solution.objects[i].name, end = ' ')
            print()
            break

    if not solution:
        print('No solution')

main()