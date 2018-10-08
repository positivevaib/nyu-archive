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

    def remove_last_object(self):
        self.weight -= self.objects[-1].weight
        self.value -= self.objects[-1].value
        self.objects = self.objects[:-1]

def IDS(depth, state):
    object_dict = {'1': Object('A', 1, 8, 10), '2': Object('B', 2, 4, 8), '3': Object('C', 3, 3, 7), '4': Object('D', 4, 3, 6), '5': Object('E', 5, 1, 4)}

    if depth == 0:
        if state.value >= 20 and state.weight <= 10:
            return state
        else:
            return None
    else:
        for i in range(state.objects[-1].index + 1, 6):
            state.add_object(object_dict[str(i)])
            found = IDS(depth - 1, state)
            if found:
                return found
            state.remove_last_object()
        return None

def main():
    object_dict = {'1': Object('A', 1, 8, 10), '2': Object('B', 2, 4, 8), '3': Object('C', 3, 3, 7), '4': Object('D', 4, 3, 6), '5': Object('E', 5, 1, 4)}

    for i in range(1, len(object_dict) + 1):
        solution = IDS(i, State([Object('zero', 0, 0, 0)]))
        if solution:
            print('[', end = "")
            for i in range(1, len(solution.objects)):
                if i == len(solution.objects) - 1:
                    print(solution.objects[i].name, end = "")
                else:
                    print(solution.objects[i].name, end = ', ')
            print(']')
            break

main()