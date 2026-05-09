#include "keyboard.h"
#include "../video/video.h"

namespace kyb {
	code_trans trans_table[] = {
		// letters
		{0x1E, 'a', 'A'},
		{0x30, 'b', 'B'},
		{0x2E, 'c', 'C'},
		{0x20, 'd', 'D'},
		{0x12, 'e', 'E'},
		{0x21, 'f', 'F'},
		{0x22, 'g', 'G'},
		{0x23, 'h', 'H'},
		{0x17, 'i', 'I'},
		{0x24, 'j', 'J'},
		{0x25, 'k', 'K'},
		{0x26, 'l', 'L'},
		{0x32, 'm', 'M'},
		{0x31, 'n', 'N'},
		{0x18, 'o', 'O'},
		{0x19, 'p', 'P'},
		{0x10, 'q', 'Q'},
		{0x13, 'r', 'R'},
		{0x1F, 's', 'S'},
		{0x14, 't', 'T'},
		{0x16, 'u', 'U'},
		{0x2F, 'v', 'V'},
		{0x11, 'w', 'W'},
		{0x2D, 'x', 'X'},
		{0x15, 'y', 'Y'},
		{0x2C, 'z', 'Z'},
		// numbers
		{0x02, '1', '!'},
		{0x03, '2', '"'},
		{0x04, '3', '@'},
		{0x05, '4', '$'},
		{0x06, '5', '%'},
		{0x07, '6', '&'},
		{0x08, '7', '/'},
		{0x09, '8', '('},
		{0x0A, '9', ')'},
		{0x0B, '0', '='},
		// whitespace
		{0x1C, '\n', '\n'},
		{0x01, 0x18, 0x18},
		{0x0E, '\b', '\b'},
		{0x0F, '\t', '\t'},
		{0x39, ' ', ' '},
		// symbols
		{0x2B, '\\', '|'},
		{0x28, '\'', '?'},
		{0x4E, '+', '*'},
		{0x33, ',', ';'},
		{0x34, '.', ':'},
		{0x0C, '-', '_'},
	};

	int trans_num = sizeof(trans_table) / sizeof(code_trans);

	code_contr contr_table[] = {
		{0x2A, false},
		{0x1D, false},
		{0x38, false}
	};

	int contr_num = sizeof(contr_table) / sizeof(code_contr);

	char translate_code(unsigned char code) {
		// scan control table
		for(int i = 0; i < contr_num; i++) {
			// get control 
			code_contr* contr = &contr_table[i];
			if(code == contr->code) contr->status = true;
			if(code == (contr->code | 0x80)) contr->status = false;
		}

		// scan translation table
		for(int i = 0; i < trans_num; i++) {
			// get translation 
			code_trans trans = trans_table[i];
			if(code != trans.code) continue; 

			// return corresponding char
			return get_shift() ? trans.upper : trans.lower;
		}

		return '\0';
	}

	char poll_char() {
		// empty keyboard buffer 
		while(*keyboard.sts_prt == 1) {
			// convert code to char 
			unsigned char code = *keyboard.dat_prt;
			char c = translate_code(code);

			// return if valid
			if(c != '\0') return c;
		}

		return '\0';
	}
	
	char get_char() {
		while(true) {
			while(*keyboard.sts_prt != 1); // busy wait
			
			// convert code to char 
			unsigned char code = *keyboard.dat_prt;
			char c = translate_code(code);
				
			// return if valid
			if(c != '\0') return c;
		}
	}

	unsigned get_uint(unsigned int first) {
		unsigned int res = first;

		while(true) {
			char c = get_char();
			
			// if backspace is allowed, do it
			if(c == '\b') {
				res /= 10;

				continue;
			}
			
			// if newline return
			if(c == '\n') break;

			// if not allowed continue
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
	
	unsigned read_uint(unsigned int first) {
		unsigned int res = first;

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
