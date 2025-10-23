#ifndef STRING_H
#define STRING_H

/*
 * Namespace for string handling, including cstring functions + memory functions.
 */
namespace str {
	/*
	 * Returns length of string.
	 */
	unsigned int len(const char*);

	/*
	 * Copies source string to destination string.
	 */
	char* cpy(char*, const char*);
	
	/*
	 * Copies at most n characters from source stringto destination string.
	 */
	char* ncpy(char*, const char*, unsigned int);
	
	/*
	 * Concatenates source string to destination string.
	 */
	char* cat(char*, const char*);

	/*
	 * Concatenates at most n characters from source string to destination string.
	 */
	char* ncat(char*, const char*, unsigned int);

	/*
	 * Compares two strings.
	 */
	int cmp(const char*, const char*);
	
	/*
	 * Compares at most n characters of two strings.
	 */
	int ncmp(const char*, const char*, unsigned int);

	/*
	 * Copies a buffer of n bytes from source to destination.
	 */
	void* mcpy(void*, const void*, unsigned int);
	
	/*
	 * Copies a buffer of n bytes from source to destination, allowing overlapping buffers.
	 */
	void* mmove(void*, const void*, unsigned int);

	/*
	 * Sets a buffer of n bytes to the given data byte.
	 */
	void* mset(void*, char, unsigned int);
	
	/*
	 * Compares two buffers of n bytes.
	 */
	int mcmp(const void*, const void*, unsigned int);
} // str::

#endif
