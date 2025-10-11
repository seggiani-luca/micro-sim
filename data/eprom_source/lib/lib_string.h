#ifndef LIB_STRING_H
#define LIB_STRING_H

#include <stdint.h>

namespace str {
	/*
	 * Returns length of string.
	 */
	unsigned int len(const char* s) {
		unsigned int i = 0;
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
	char* ncpy(char* dest, const char* src, unsigned int n) {
		

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
	char* ncat(char* dest, const char* src, unsigned int n) {
		int i = len(dest);
		ncpy(dest + i, src, n);
		return dest;
	}

	/*
	 * Copies a buffer of n bytes from src to dest.
	 */
	void* mcpy(void* dest, const void* src, unsigned int n) {
		// head
		uint8_t* b_dptr = (uint8_t*) dest;
		const uint8_t* b_sptr = (const uint8_t*) src;
		while(n > 0 && ((uint32_t) b_dptr & 0x3)) {
			*b_dptr++ = *b_sptr++;
			n--;
		}

		if(n >= 4 && ((uint32_t) b_sptr & 0x3) == 0) { // aligned
			// body
			uint32_t* w_dptr = (uint32_t*) b_dptr;
			const uint32_t* w_sptr = (const uint32_t*) b_sptr;
			while(n >= 4) {
				*w_dptr++ = *w_sptr++;
				n -= 4;
			}

			// setup tail
			b_dptr = (uint8_t*) w_dptr;
			b_sptr = (uint8_t*) w_sptr;
		}

		// tail
		while(n--) *b_dptr++ = *b_sptr++;
	
		return dest;
	}
	
	/*
	 * Copies a buffer of n bytes from src to dest, possibly overlapping.
	 */
	void* mmove(void* dest, const void* src, unsigned int n) {
		if((uint32_t) dest < (uint32_t) src) {
			return mcpy(dest, src, n); // fotward copy
		} // backwards copy
		
		// tail 
		uint8_t* b_dptr = (uint8_t*) dest + n - 1;
		const uint8_t* b_sptr = (const uint8_t*) src + n - 1;
		while(n > 0 && ((uint32_t) b_dptr & 0x3)) {
			*b_dptr-- = *b_sptr--;
			n--;
		}

		if(n >= 4 && ((uint32_t) b_sptr & 0x3) == 0) { // aligned
			// body
			uint32_t* w_dptr = (uint32_t*) b_dptr;
			const uint32_t* w_sptr = (const uint32_t*) b_sptr;
			while(n >= 4) {
				*w_dptr-- = *w_sptr--;
				n -= 4;
			}

			// setup head
			b_dptr = (uint8_t*) w_dptr;
			b_sptr = (uint8_t*) w_sptr;
		}

		// head
		while(n--) *b_dptr-- = *b_sptr--;

		return dest;
	}

	/*
	 * Sets a buffer of n bytes to the given data byte.
	 */
	void* mset(void* dest, char data, unsigned int n) {
		// head
		uint8_t b_dat = data;
		uint8_t* b_dptr = (uint8_t*) dest;
		while(n > 0 && ((uint32_t) b_dptr & 0x3)) {
			*b_dptr++ = b_dat;
			n--;
		}
	
		// body
		uint32_t w_dat = data | data << 8 | data << 16 | data << 24;
		uint32_t* w_dptr = (uint32_t*) b_dptr;
		while(n >= 4) {
			*w_dptr++ = w_dat;
			n -= 4;
		}

		// tail
		b_dptr = (uint8_t*) w_dptr;
		while(n--) *b_dptr++ = b_dat;
	
		return dest;
	}
}

#endif
