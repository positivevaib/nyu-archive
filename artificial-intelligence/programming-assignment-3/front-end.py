input_file = open('front-end-input.txt', 'r')
input_data = input_file.read()

total_vertices = int(input_data[0])

output_file = open('davis-putnam-input.txt', 'w+')

# Propositions
# Every vertex is traversed at some time.
for vertex in range(1, total_vertices + 1):
	for sequence_number in range(1, total_vertices + 1):
		output_file.write(str(((vertex - 1) * total_vertices) + sequence_number) + ' ')
	output_file.write('\n')

# No pair of vertices are traversed at the same time.
for sequence_number in range(1, total_vertices + 1):
	for vertex in range(1, total_vertices + 1):
		for other_vertex in range(vertex, total_vertices + 1):
			output_file.write()

#key

vertex_number = 1
for vertex in range(total_vertices):
	for sequence_number in range(1, total_vertices + 1):
		output_file.write('\n' + str(vertex_number) + ' ' + chr(ord('A') + vertex) + ' ' + str(sequence_number))
		vertex_number += 1