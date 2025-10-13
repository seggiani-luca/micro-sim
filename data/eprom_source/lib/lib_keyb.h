#ifndef LIB_KEYB_H
#define LIB_KEYB_H

#include <cstdint>

namespace vid {
	void print_char(char);
	void backspace();
	void newline();
}

/*
 * Namespace for handling keyboard, including character/string reading/getting.
 */
namespace keyb {
	/*
	 * Keyboard status register.
	 */
	inline volatile uint32_t*	keyb_status = (volatile uint32_t*) 0x00040000;

	/*
	 * Keyboard data register.
	 */
	inline volatile uint32_t*	keyb_data = (volatile uint32_t*) 0x00040001;

	/*
	 * Gets a char without echo.
	 */
	char get_char() {
		while(*keyb_status != 1); // busy wait
		return *keyb_data;
	}

	/*
	 * Gets an unsigned integer without echo. Terminates on \n.
	 */
	unsigned get_uint(unsigned int base = 0) {
		unsigned int res = base;

		while(true) {
			char c = get_char();
			
			// if backspace is allowed, do it
			if(c == '\b') {
				res /= 10;

				continue;
			}
			
			// if newline return
			if(c == '\n') break;

			// if not allowed continnue
			if(c < '0' || c > '9') continue;

			// write decimal and step
			res = res * 10 + c - '0';
		}

		return res;
	}

	/*
	 * Gets an integer without echo. Terminates on \n.
	 */
	int get_int() {
		bool neg = false;
		char c = get_char();

		unsigned int u;
		
		if(c == '-') {
			neg = true;
		}

		if(c < '0' || c > '9') {
			u = get_uint();
		} else {
			u = get_uint(c - '0'); // carry the first digit
		}

		return neg ? - (int) u : (int) u;
	}

	/*
	 * Gets a string without echo. Terminates on \n.
	 */
	void get_str(char* buf, int n) {
		int i = 0;

		while(true) {
			char c = get_char();

			// if backspace is allowed, do it
			if(c == '\b') {
				if(i > 0) i--;

				// scrap if at the beginning of buffer
				continue;
			}
			
			// if newline return
			if(c == '\n') break;

			// write character in string
			buf[i] = c;

			// step forward
			i++;
			if(i >= n - 1) break; 

		}

		buf[i] = '\0';
	}

	/*
	 * Reads a char with echo. Doesn't echo control characters (\b and \n)
	 */
	char read_char() {
		char c = get_char();

		if(c != '\b' && c != '\n') { 
			vid::print_char(c);
		}	

		return c;
	}
	
	/*
	 * Reads an unsigned integer without echo. Terminates on \n.
	 */
	unsigned read_uint(unsigned int base = 0) {
		unsigned int res = base;

		while(true) {
			char c = read_char();
			
			// if backspace is allowed, do it
			if(c == '\b') {
				res /= 10;
				vid::backspace();

				continue;
			}
			
			// if newline return
			if(c == '\n') {
				vid::newline();
				break;
			}

			// if not allowed continnue
			if(c < '0' || c > '9') continue;

			// write decimal and step
			res = res * 10 + c - '0';
		}

		return res;
	}

	/*
	 * Reads an integer without echo. Terminates on \n.
	 */
	int read_int() {
		bool neg = false;
		char c = read_char();

		unsigned int u;
		
		if(c == '-') {
			neg = true;
		}

		if(c < '0' || c > '9') {
			u = read_uint();
		} else {
			u = read_uint(c - '0'); // carry the first digit
		}

		return neg ? - (int) u : (int) u;
	}

	/*
	 * Reads a string with echo. Terminates on \n.
	 */
	void read_str(char* buf, int n) {
		int i = 0;

		while(true) {
			char c = read_char();

			// if backspace is allowed, do it
			if(c == '\b') {
				if(i > 0) {
					i--;
					vid::backspace();
				}

				// scrap if at the beginning of buffer
				continue;
			}
			
			// if newline return
			if(c == '\n') {
				vid::newline();
				break;
			}

			// write character in string
			buf[i] = c;

			// step forward
			i++;
			if(i >= n - 1) {
				vid::newline();
				break;
			}

		}
				
		buf[i] = '\0';
	}

}

#endif
