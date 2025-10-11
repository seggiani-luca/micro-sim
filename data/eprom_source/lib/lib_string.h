#ifndef LIB_STRING_H
#define LIB_STRING_H

#include <cstdint>
#include <stdint.h>

namespace str {
	/*
	 * Returns length of string.
	 */
	int len(const char* s) {
		int i = 0;
		while(*s++) i++;	
		return i;
	}

	/*
	 * Copies string src to string dest.
	 */
	char* cpy(char* dest, const char* src) {
		char* ret = dest;
		while(*dest++ = *src++);
		return ret;
	}
	
	/*
	 * Copies at most n characters from string src to string dest.
	 */
	char* ncpy(char* dest, const char* src, int n) {
		char* ret = dest;
		while(n-- && (*dest++ = *src++));
		return ret;
	}
	
	/*
	 * Concatenates string src to string dest.
	 */
	char* cat(char* dest, const char* src) {
		int i = len(dest);
		cpy(dest + i, src);
		return dest;
	}

	/*
	 * Concatenates at most n characters from string c to string dest.
	 */
	char* ncat(char* dest, const char* src, int n) {
		int i = len(dest);
		ncpy(dest + i, src, n);
		return dest;
	}

	/*
	 * Copies a buffer of n bytes from src to dest.
	 */
	void* mcpy(void* dest, const void* src, int n);
	
	/*
	 * Copies a buffer of n bytes from src to dest, possibly overlapping.
	 */
	void* mmove(void* dest, const void* src, int n);

	/*
	 * Sets a buffer of n bytes to the given data byte.
	 */
	void* mset(void* dest, char data, int n) {
		// head
		uint8_t b_dat = data;
		uint8_t* b_ptr = (uint8_t*) dest;
		while(((uint32_t) b_ptr & 0x3) && n--) *b_ptr++ = b_dat;
	
		// body
		uint32_t w_dat = data | data << 8 | data << 16 | data << 24;
		uint32_t* w_ptr = (uint32_t*) b_ptr;
		while(n >= 4) {
			*w_ptr++ = w_dat;
			n -= 4;
		}

		// tail
		b_ptr = (uint8_t*) w_ptr;
		while(n--) *b_ptr++ = b_dat;
	
		return dest;
	}
}

#endif
