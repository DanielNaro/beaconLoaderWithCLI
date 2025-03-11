import json
import sys

def get_max_string_length(data):
	"""
	Recursively traverse the JSON data to find the maximum length of any string.
	"""
	max_length = 0
	
	if isinstance(data, dict):
		# If the data is a dictionary, iterate over its values.
		for value in data.values():
			max_length = max(max_length, get_max_string_length(value))
	elif isinstance(data, list):
		# If the data is a list, iterate over its items.
		for item in data:
			max_length = max(max_length, get_max_string_length(item))
	elif isinstance(data, str):
		# If the data is a string, update max_length with its length.
		max_length = len(data)

	return max_length

def main():
	if len(sys.argv) < 2:
		print("Usage: python program.py <path_to_json_file>")
		sys.exit(1)

	filename = sys.argv[1]

	try:
		with open(filename, 'r') as file:
			data = json.load(file)
	except Exception as e:
		print(f"Error reading JSON file: {e}")
		sys.exit(1)

	max_length = get_max_string_length(data)
	print("Maximum string length:", max_length)

if __name__ == "__main__":
	main()
