#include "string.h"
#include <cstdint>

namespace str {
	unsigned int len(const char* s) {
		unsigned int i = 0;
		while(*s++) i++;	
		return i;
	}

	char* cpy(char* dest, const char* src) {
		char* ret = dest;
		while(*dest++ = *src++);
		return ret;
	}
	
	char* ncpy(char* dest, const char* src, unsigned int n) {
		char* ret = dest;
		while(n > 0 && (*dest++ = *src++)) {
			n--;
		}

		// pad
		while(n > 0) {
			*dest++ = '\0';
			n--;
		}

		return ret;
	}
	
	char* cat(char* dest, const char* src) {
		int i = len(dest);
		cpy(dest + i, src);
		return dest;
	}

	char* ncat(char* dest, const char* src, unsigned int n) {
		int i = len(dest);
		ncpy(dest + i, src, n);
		return dest;
	}

	int cmp(const char* str1, const char* str2) {
		while(*str1 && *str2) {
			if(*str1 != *str2) break;
			str1++;
			str2++;
		}

		return (unsigned char) *str1 - (unsigned char) *str2;
	}
	
	int ncmp(const char* str1, const char* str2, unsigned int n) {
		while(*str1 && *str2 && n) {
			if(*str1 != *str2) break;
			str1++;
			str2++;
			n--;
		}
		
		if(n == 0) return 0;

		return (unsigned char) *str1 - (unsigned char) *str2;
	}

	void* mcpy(void* dest, const void* src, unsigned int n) {
		uint8_t* b_dptr = (uint8_t*) dest;
		const uint8_t* b_sptr = (const uint8_t*) src;

		for(int i = 0; i < n; i++) {
			*b_dptr++ = *b_sptr++;
		}
	
		return dest;
	}
	
	void* mmove(void* dest, const void* src, unsigned int n) {
		if((uint32_t) dest < (uint32_t) src) {
			return mcpy(dest, src, n); // fotward copy
		} 

		// backwards copy
		uint8_t* b_dptr = (uint8_t*) dest + n - 1;
		const uint8_t* b_sptr = (const uint8_t*) src + n - 1;
		for(int i = 0; i < n; i++) {
			*b_dptr-- = *b_sptr--;
		}

		return dest;
	}

	void* mset(void* dest, char data, unsigned int n) {
		uint8_t b_dat = data;
		uint8_t* b_dptr = (uint8_t*) dest;
		
		for(int i = 0; i < n; i++) {
			*b_dptr++ = b_dat;
		}

		return dest;
	}

	int mcmp(const void* buf1, const void* buf2, unsigned int n) {
		unsigned char* b_1ptr = (unsigned char*) buf1;		
		unsigned char* b_2ptr = (unsigned char*) buf2;		

		for(unsigned int i = 0; i < n; i++) {
			if(b_1ptr[i] != b_2ptr[i]) {
				return b_1ptr[i] - b_2ptr[i];
			}
		}

		return 0;
	}

} // str::
