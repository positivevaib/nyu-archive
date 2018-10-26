# Load input data
input_file = open('front-end-input.txt', 'r')
input_data = input_file.read()

total_vertices = int(input_data[0])
edges = input_data[2:].split('\n')

# Create output file and write data
output_file = open('davis-putnam-input.txt', 'w+')

# Propositions
# Every vertex is traversed at some time
for vertex in range(total_vertices):
	for sequence_number in range(1, total_vertices + 1):
		output_file.write(str((vertex * total_vertices) + sequence_number) + ' ')
	output_file.write('\n')

# No pair of vertices are traversed at the same time
output_file.write('\n')
for sequence_number in range(1, total_vertices + 1):
	for vertex in range(total_vertices):
		for other_vertex in range(vertex + 1, total_vertices):
			output_file.write('-' + str((vertex * total_vertices) + sequence_number) + ' ' + '-' + str((other_vertex * total_vertices) + sequence_number) + '\n')

# Two vertices that don't have an edge between them cannot be traversed in 1 unit time
output_file.write('\n')
for vertex in range(total_vertices):
	for other_vertex in range(total_vertices):
		if (vertex != other_vertex) and (chr(ord('A') + vertex) + ' ' + chr(ord('A') + other_vertex) not in edges):
			for sequence_number in range(1, total_vertices):
				output_file.write('-' + str((vertex * total_vertices) + sequence_number) + ' ' + '-' + str((other_vertex * total_vertices) + sequence_number + 1) + '\n')

# Keys
output_file.write('\n0')
vertex_number = 1
for vertex in range(total_vertices):
	for sequence_number in range(1, total_vertices + 1):
		output_file.write('\n' + str(vertex_number) + ' ' + chr(ord('A') + vertex) + ' ' + str(sequence_number))
		vertex_number += 1