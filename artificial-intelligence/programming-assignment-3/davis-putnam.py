# Load input data
input_file = open('davis-putnam-input.txt', 'r')
input_data = input_file.read().split('\n')

propositions = []
for proposition in input_data:
    proposition = proposition.split(' ')
    if '0' in proposition:
        break
    propositions.append(proposition)

keys = []
for key_index in range(-1, -len(input_data), -1):
    key = input_data[key_index].split(' ')
    if '0' in key:
        break
    keys.append(key)

# Davis-Putnam algorithm
def obvious_assign(literal, values):
    literal = int(literal)
    if literal > 0:
        values[literal] = 'T'
    else:
        values[literal * (-1)] = 'F'

    return values

def propagate(atom, propositions, values):
    for proposition in propositions:
        if ((atom in proposition) and (values[int(atom)] == 'T')) or (('-' + atom in proposition) and (values[int(atom) * -1] == 'F')):
            propositions.remove(proposition)
        elif (atom in proposition) and (values[int(atom)] == 'F'):
            propositions[propositions.index(proposition).remove(atom)]
        elif ('-' + atom in proposition) and (values[int(atom) * -1] == 'T'):
            propositions[propositions.index(proposition).remove('-' + atom)]
    
    return propositions

def davis_putnam_recursive(total_atoms, propositions, values):
    while True:
        if not propositions:
            for atom in range(len(values)):
                if not values[atom]:
                    values[atom] = 'T'
            return values

def davis_putnam(total_atoms, propositions):
    values = [None] * total_atoms
    return davis_putnam_recursive(total_atoms, propositions, values)