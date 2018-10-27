# Load input data
input_file = open('back-end-input.txt', 'r')
input_data = input_file.read().split('\n')

# Values
values = []
for value in input_data:
    value = value.split(' ')
    if '0' in value:
        break
    values.append(value)

# Keys
keys = {}
for key_index in range(-1, -len(input_data), -1):
    key = input_data[key_index].split(' ')
    if '0' in key:
        break
    keys[key[0]] = key[1:]

# Generate output
if not values:
    print('No solution')
else:
    path = []
    for value in values:
        if 'T' in value:
            path.append(value[0])
    
    for sequence_number in range(len(keys)):
        for vertex in path:
            if str(sequence_number) in keys[vertex]:
                print(keys[vertex][0] + keys[vertex][1], end = ' ')
    print()