#Import packages
import random
import copy

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
    objects = {}
    weight = 0
    value = 0

    #Define constructor
    def __init__(self, objects):
        self.objects = objects

    #Define function to add objects
    def add(self, object):
        self.objects[str(object.index)] = object
        self.weight += object.weight
        self.value += object.value

    #Define function to remove objects
    def remove(self, object):
        del self.objects[str(object.index)]
        self.weight -= object.weight
        self.value -= object.value

#Define function to return random initial states
def random_restart(object_dict):
    state = State({})
    for object in object_dict.values():
        choice = random.choice([object, None])
        if choice:
            state.add(choice)

    return state

#Define function to compute and return state error
def error(state, T, M):
    return max(state.weight - M, 0) + max(T - state.value, 0)

#Define function to search using Hill Climbing Algorithm with random restarts and return all best states found
def HCS(restarts, object_dict, T, M):
    best_states = []

    #Search for all random restarts
    for i in range(restarts):
        #Initialize state and error
        state = random_restart(object_dict)
        state_error = error(state, T, M)

        #Search until no optimal neighbor states to move to
        while True:
            #Initialize optimal neighbor state
            neighbor_state = None
            neighbor_error = state_error

            #Delete objects
            for object in state.objects.values():
                state.remove(object)
                #Update neighbor_state if error improves
                if error(state, T, M) < neighbor_error:
                    neighbor_state = copy.deepcopy(state)
                    neighbor_error = error(state, T, M)
                #Revert to original state for next deletion
                state.add(object)

            #Add and swap objects
            for new_object in object_dict.values():
                #Initialize sentinel value
                change = True

                #Continue to next iteration if new_objects already in state
                for object in state.objects.values():
                    if new_object.name == object.name:
                        #Update sentinel value
                        change = False
                        break
                
                #If new_object not in state, add and swap objects
                if change:
                    #Add new_object
                    state.add(new_object)
                    #Update neighbor state if error improves
                    if error(state, T, M) < neighbor_error:
                        neighbor_state = copy.deepcopy(state)
                        neighbor_error = error(state, T, M)
                    #Revert to original state for swaps and next insertion
                    state.remove(new_object)

                    #Swap objects with new_object
                    for object in state.objects.values():
                        state.remove(object)
                        state.add(new_object)
                        #Update neighbor state if error improves
                        if error(state, T, M) < neighbor_error:
                            neighbor_state = copy.deepcopy(state)
                            neighbor_error = error(state, T, M)
                        #Revert to original state for further swaps
                        state.remove(new_object)
                        state.add(object)

            #Update state and state_error for next iteration if an optimal neighbor state exists
            if neighbor_state:
                state = copy.deepcopy(neighbor_state)
                state_error = neighbor_error
            else:
                break
        
        #Add best state found to best_states
        best_states.append(state)

    #Return best states found for all restarts
    return best_states

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
    
    total_restarts = 10
    #Search state space using HCS
    best_states = HCS(total_restarts, object_dict, T, M)

    #Print best states
    for state in best_states:
        print('Solution:', end = ' ')
        for object in state.objects.values():
            print(object.name, end = ' ')
        print()

main()