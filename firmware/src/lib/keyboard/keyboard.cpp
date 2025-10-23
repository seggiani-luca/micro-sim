#include "keyboard.h"
#include "../conf/hardware.h"
#include "../video/video.h"

// reference keyboard device
hwr::dev::keyboard_device& keyboard = hwr::dev::keyboard;

namespace kyb {
	char get_char() {
		while(*keyboard.sts_reg != 1); // busy wait
		return *keyboard.buf_reg;
	}

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

	char read_char() {
		char c = get_char();

		if(c != '\b' && c != '\n') { 
			vid::print_char(c);
		}	

		return c;
	}
	
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
} // kyb::
