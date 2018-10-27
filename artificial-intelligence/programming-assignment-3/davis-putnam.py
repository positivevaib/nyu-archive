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
    values_copy = values.copy()

    if literal > 0:
        values_copy[literal - 1] = 'T'
    else:
        values_copy[(literal * (-1)) - 1] = 'F'

    return values_copy

def propagate(atom, propositions, values):
    propositions_copy = propositions.copy()
    values_copy = values.copy()

    for proposition in propositions_copy:
        if ((atom in proposition) and (values_copy[int(atom) - 1] == 'T')) or (('-' + atom in proposition) and (values_copy[(int(atom) * -1) - 1] == 'F')):
            propositions_copy.remove(proposition)
        elif (atom in proposition) and (values_copy[int(atom) - 1] == 'F'):
            propositions_copy[propositions_copy.index(proposition).remove(atom)]
        elif ('-' + atom in proposition) and (values_copy[(int(atom) * -1) - 1] == 'T'):
            propositions_copy[propositions_copy.index(proposition).remove('-' + atom)]
    
    return propositions_copy

def davis_putnam_recursive(propositions, values):
    propositions_copy = propositions.copy()
    values_copy = values.copy()

    while True:
        # Base cases
        if not propositions_copy:
            for atom in range(len(values_copy)):
                if not values_copy[atom]:
                    values_copy[atom] = 'T'
            return values_copy
        
        for proposition in propositions_copy:
            if not proposition:
                return None
        
        # Easy cases
        # Pure literal elimination
        literals = []
        for proposition in propositions_copy:
            for literal in proposition:
                literal = int(literal)
                if literal not in literals:
                    literals.append(literal)
        
        for literal in literals:
            if (literal * -1) in literals:
                literals.remove(literal)
                literals.remove(literal * -1)

        if literals:
            pure_literal = min(literal for literal in literals if abs(literal) > 0)
            values_copy = obvious_assign(str(pure_literal), values_copy)
            for proposition in propositions_copy:
                if str(pure_literal) in proposition:
                    propositions_copy.remove(proposition)

        # Singletons
        for proposition in propositions_copy:
            if len(proposition) == 1:
                values_copy = obvious_assign(proposition[0], values_copy)
                propositions_copy = propagate(str(abs(int(proposition[0]))), propositions_copy, values_copy)
                break
        
    # Hard cases
    for value_index in range(len(values_copy)):
        if not values_copy[value_index]:
            values_copy[value_index] = 'T'
            propositions_copy_copy = propositions_copy.copy()
            propositions_copy_copy = propagate(value_index + 1, propositions_copy_copy, values_copy)
            values_new = davis_putnam_recursive(propositions_copy_copy, values_copy)
            if values_new:
                return values_new
            else:
                values_copy[value_index] = 'F'
                propositions_copy_copy = propagate(value_index + 1, propositions_copy, values_copy)
                return davis_putnam_recursive(propositions_copy_copy, values_copy)

def davis_putnam(total_atoms, propositions):
    values = [None] * total_atoms
    return davis_putnam_recursive(propositions, values)

# Call Davis-Putnam
values = davis_putnam(len(keys), propositions)

# Create output file and write data
output_file = open('back-end-input.txt', 'w+')

for value_index in range(len(values)):
    output_file.write(str(value_index + 1) + ' ')
    output_file.write(str(values[value_index]) + '\n')

output_file.write('0\n')

for key_index in range(-1, -len(keys) - 1, -1):
    output_file.write(keys[key_index][0] + ' ' + keys[key_index][1] + ' ' + keys[key_index][2])
    if key_index != -len(keys):
        output_file.write('\n')