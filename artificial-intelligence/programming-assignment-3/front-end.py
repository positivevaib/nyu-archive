# Import modules
import sys

# Read input
input_data = sys.stdin.read().split('\n')

total_vertices = int(input_data[0])
edges = input_data[1:]

# Generate output
# Propositions
# Every vertex is traversed at some time
for vertex in range(total_vertices):
	for sequence_number in range(1, total_vertices + 1):
		sys.stdout.write(str((vertex * total_vertices) + sequence_number))
		if sequence_number != total_vertices:
			sys.stdout.write(' ')
	sys.stdout.write('\n')

# No pair of vertices are traversed at the same time
for sequence_number in range(1, total_vertices + 1):
	for vertex in range(total_vertices):
		for other_vertex in range(vertex + 1, total_vertices):
			sys.stdout.write('-' + str((vertex * total_vertices) + sequence_number) + ' ' + '-' + str((other_vertex * total_vertices) + sequence_number) + '\n')

# Two vertices that don't have an edge between them cannot be traversed in 1 unit time
for vertex in range(total_vertices):
	for other_vertex in range(total_vertices):
		if (vertex != other_vertex) and (chr(ord('A') + vertex) + ' ' + chr(ord('A') + other_vertex) not in edges):
			for sequence_number in range(1, total_vertices):
				sys.stdout.write('-' + str((vertex * total_vertices) + sequence_number) + ' ' + '-' + str((other_vertex * total_vertices) + sequence_number + 1) + '\n')

# Keys
sys.stdout.write('0')
vertex_number = 1
for vertex in range(total_vertices):
	for sequence_number in range(1, total_vertices + 1):
		sys.stdout.write('\n' + str(vertex_number) + ' ' + chr(ord('A') + vertex) + ' ' + str(sequence_number))
		vertex_number += 1