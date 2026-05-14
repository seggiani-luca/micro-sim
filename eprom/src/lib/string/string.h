#ifndef STRING_H
#define STRING_H

/**
 * Namespace for string handling, including cstring functions.
 */
namespace str {
	/**
	 * Returns length of string.
	 *
	 * @param s string to take length of
	 * @return length of string
	 */
	unsigned int len(const char* s);

	/**
	 * Copies source string to destination string.
	 *
	 * @param dst destination string
	 * @param src source string
	 * @return destination string
	 */
	char* cpy(char* dst, const char* src);
	
	/**
	 * Copies at most n characters from source string to destination string.
	 *
	 * @param dst destination string
	 * @param src source string
	 * @param n max number of characters to copy
	 * @return destination string
	 */
	char* ncpy(char* dst, const char* src, unsigned int n);
	
	/**
	 * Concatenates source string to destination string.
	 *
	 * @param dst destination string
	 * @param src source string
	 * @return destination string
	 */
	char* cat(char* dst, const char* src);

	/**
	 * Concatenates at most n characters from source string to destination 
	 * string.
	 *
	 * @param dst destination string
	 * @param src source string
	 * @param n max number of characters to copy
	 * @return destination string
	 */
	char* ncat(char* dst, const char* src, unsigned int n);

	/**
	 * Compares two strings.
	 *
	 * @param str1 first string to compare
	 * @param str2 second string to compare
	 * @return str1 - str2 at first different character, 0 if equal
	 */
	int cmp(const char* str1, const char* str2);
	
	/**
	 * Compares at most n characters of two strings.
	 *
	 * @param str1 first string to compare
	 * @param str2 second string to compare
	 * @param n max number of characters to compare
	 * @return str1 - str2 at first different character, 0 if equal
	 */
	int ncmp(const char* str1, const char* str2, unsigned int n);
} // str::

/**
 * Namespace for memory functions.
 */
namespace mem {
	/**
	 * Copies a buffer of n bytes from source to destination.
	 *
	 * @param dst destination buffer
	 * @param src source buffer
	 * @param n number of bytes to copy
	 * @return destination buffer
	 */
	void* cpy(void* dst, const void* src, unsigned int n);
	
	/**
	 * Copies a buffer of n bytes from source to destination, allowing 
	 * overlapping buffers.
	 *
	 * @param dst destination buffer
	 * @param src source buffer
	 * @param n number of bytes to copy
	 * @return destination buffer
	 */
	void* move(void* dst, const void* src, unsigned int n);

	/**
	 * Sets a buffer of n bytes to the given data byte.
	 *
	 * @param dst destination buffer
	 * @param data data byte to set buffer bytes to
	 * @param n size of buffer
	 * @return destination buffer
	 */
	void* set(void* dst, char data, unsigned int n);
	
	/**
	 * Compares two buffers of n bytes.
	 *
	 * @param buf1 first buffer to compare
	 * @param buf2 second buffer to compare
	 * @param n max number of bytes to compare
	 * @return buf1 - buf2 at first different byte, 0 if equal
	 */
	int cmp(const void* buf1, const void* buf2, unsigned int n);
} // mem::

#endif
