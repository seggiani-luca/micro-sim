#ifndef STRING_H
#define STRING_H

/*
 * Namespace for string handling, including cstring functions + memory functions.
 */
namespace str {
	/*
	 * Returns length of string.
	 */
	unsigned int len(const char* s);

	/*
	 * Copies source string to destination string.
	 */
	char* cpy(char* dest, const char* src);
	
	/*
	 * Copies at most n characters from source stringto destination string.
	 */
	char* ncpy(char* dest, const char* src, unsigned int n);
	
	/*
	 * Concatenates source string to destination string.
	 */
	char* cat(char* dest, const char* src);

	/*
	 * Concatenates at most n characters from source string to destination string.
	 */
	char* ncat(char* dest, const char* src, unsigned int n);

	/*
	 * Compares two strings.
	 */
	int cmp(const char* str1, const char* str2);
	
	/*
	 * Compares at most n characters of two strings.
	 */
	int ncmp(const char* str1, const char* str2, unsigned int n);

	/*
	 * Copies a buffer of n bytes from source to destination.
	 */
	void* mcpy(void* dest, const void* src, unsigned int n);
	
	/*
	 * Copies a buffer of n bytes from source to destination, allowing overlapping buffers.
	 */
	void* mmove(void* dest, const void* src, unsigned int n);

	/*
	 * Sets a buffer of n bytes to the given data byte.
	 */
	void* mset(void* dest, char data, unsigned int n);
	
	/*
	 * Compares two buffers of n bytes.
	 */
	int mcmp(const void* buf1, const void* buf2, unsigned int n);
} // str::

#endif
