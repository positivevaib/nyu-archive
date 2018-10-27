# Import modules
import copy

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
    propositions_copy = copy.deepcopy(propositions)
    values_copy = values.copy()

    proposition_index = 0
    while proposition_index < len(propositions_copy):
        proposition = propositions_copy[proposition_index]
        if ((atom in proposition) and (values_copy[int(atom) - 1] == 'T')) or (('-' + atom in proposition) and (values_copy[int(atom) - 1] == 'F')):
            propositions_copy.remove(proposition)
        elif (atom in proposition) and (values_copy[int(atom) - 1] == 'F'):
            propositions_copy[proposition_index].remove(atom)
        elif ('-' + atom in proposition) and (values_copy[int(atom) - 1] == 'T'):
            propositions_copy[proposition_index].remove('-' + atom)
        else:
            proposition_index += 1

    return propositions_copy

def davis_putnam_recursive(propositions, values):
    propositions_copy = propositions.copy()
    values_copy = values.copy()

    easy_cases = True
    while easy_cases:
        easy_cases = False

        # Base cases
        if not propositions_copy:
            for atom_index in range(len(values_copy)):
                if not values_copy[atom_index]:
                    values_copy[atom_index] = 'T'
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
        
        literal_index = 0
        while literal_index < len(literals):
            literal = literals[literal_index]
            if (literal * -1) in literals:
                literals.remove(literal)
                literals.remove(literal * -1)
            else:
                literal_index += 1

        if literals:
            if sorted(literals)[0] < 0 and sorted(literals)[-1] > 0:
                pure_literal = min(min(literal for literal in literals if literal > 0), max(literal for literal in literals if literal < 0))
            elif sorted(literals)[0] < 0:
                pure_literal = sorted(literals)[-1]
            else:
                pure_literal = sorted(literals)[0]
            values_copy = obvious_assign(str(pure_literal), values_copy)

            proposition_index = 0
            while proposition_index < len(propositions_copy):
                proposition = propositions_copy[proposition_index]
                if str(pure_literal) in proposition:
                    propositions_copy.remove(proposition)
                else:
                    proposition_index += 1
            
            easy_cases = True
        
        # Singletons
        if not easy_cases:
            for proposition in propositions_copy:
                if len(proposition) == 1:
                    values_copy = obvious_assign(proposition[0], values_copy)
                    propositions_copy = propagate(str(abs(int(proposition[0]))), propositions_copy, values_copy)

                    easy_cases = True
                    break
        
    # Hard cases
    for value_index in range(len(values_copy)):
        if not values_copy[value_index]:
            values_copy[value_index] = 'T'
            propositions_copy_copy = propositions_copy.copy()
            propositions_copy_copy = propagate(str(value_index + 1), propositions_copy_copy, values_copy)
            values_new = davis_putnam_recursive(propositions_copy_copy, values_copy)
            if values_new:
                return values_new
            else:
                values_copy[value_index] = 'F'
                propositions_copy_copy = propagate(str(value_index + 1), propositions_copy, values_copy)
                return davis_putnam_recursive(propositions_copy_copy, values_copy)

def davis_putnam(total_atoms, propositions):
    values = [None] * total_atoms
    return davis_putnam_recursive(propositions, values)

# Call Davis-Putnam
values = davis_putnam(len(keys), propositions)

# Create output file and write data
output_file = open('back-end-input.txt', 'w+')

if values:
    for value_index in range(len(values)):
        output_file.write(str(value_index + 1) + ' ')
        output_file.write(str(values[value_index]) + '\n')

output_file.write('0\n')

for key_index in range(-1, -len(keys) - 1, -1):
    output_file.write(keys[key_index][0] + ' ' + keys[key_index][1] + ' ' + keys[key_index][2])
    if key_index != -len(keys):
        output_file.write('\n')