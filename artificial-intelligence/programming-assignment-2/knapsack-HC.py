import random
import copy
import pdb
#pdb.set_trace()
class Object:
    name = ''
    index = 0
    weight = 0
    value = 0

    def __init__(self, name, index, weight, value):
        self.name = name
        self.index = index
        self.weight = weight
        self.value = value

class State:
    objects = []

    weight = 0
    value = 0

    def __init__(self, objects):
        self.objects = objects

    def add_object(self, object):
        self.objects.append(object)
        self.weight += object.weight
        self.value += object.value

    def add_object_index(self, object, index):
        self.objects.insert(index + 1, object)
        self.weight += object.weight
        self.value += object.value

    def remove_last_object(self):
        self.weight -= self.objects[-1].weight
        self.value -= self.objects[-1].value
        self.objects = self.objects[:-1]

    def remove_object(self, index):
        self.weight -= self.objects[index].weight
        self.value -= self.objects[index].value
        self.objects.pop(index)

def random_restart(object_dict):
    state = State([])
    for object in object_dict.values():
        choice = random.choice([object, None])
        if choice:
            state.add_object(choice)

    return state

def error(state, T, M):
    return max(state.weight - M, 0) + max(T - state.value, 0)

def HCS(total_restarts, object_dict, T, M):
    best_states = []
    for i in range(total_restarts):
        state = random_restart(object_dict)
        state_error = error(state, T, M)

        neighbor_state = None
        neighbor_error = state_error
       
        index = 0
        for object in state.objects:
            state.remove_object(index)
            if error(state, T, M) < neighbor_error:
                neighbor_state = copy.copy(state)
                neighbor_error = error(state, T, M)
            state.add_object_index(object, index)
            index += 1

        for new_object in object_dict.values():
            change = True
            for object in state.objects:
                if new_object.name == object.name:
                    change = False
                    break
            if change:
                state.add_object(new_object)
                if error(state, T, M) < neighbor_error:
                    neighbor_state = copy.copy(state)
                    neighbor_error = error(state, T, M)
                state.remove_last_object()

                for index in range(len(state.objects)):
                    object = state.objects[index]
                    state.remove_object(index)
                    state.add_object(new_object)
                    if error(state, T, M) < neighbor_error:
                        neighbor_state = copy.copy(state)
                        neighbor_error = error(state, T, M)
                    state.remove_last_object()
                    state.add_object_index(object, index)

        if neighbor_state:
            best_states.append(neighbor_state)
    return best_states

def main():
    object_dict = {}
    file = open('input1.txt', 'r')
    i = 1
    for line in file:
        line = line.strip().split()
        if len(line) == 2:
            T = int(float(line[0]))
            M = int(float(line[1]))
        else:
            name = line[0]
            value = int(float(line[1]))
            weight = int(float(line[2]))

            object_dict[str(i)] = Object(name, i, weight, value)
        i += 1
    
    total_restarts = 10

    best_states = HCS(total_restarts, object_dict, T, M)

    for state in best_states:
        print('[', end = '  ')
        for object in state.objects:
            print(object.name, end = '  ')
        print(']')

main()