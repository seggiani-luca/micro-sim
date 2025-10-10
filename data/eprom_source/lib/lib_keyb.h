#ifndef LIB_KEYB_H
#define LIB_KEYB_H

#include "lib_video.h"
#include <cstdint>

namespace keyb {
	/*
	 * Keyboard status register.
	 */
	volatile uint32_t*	keyb_status = (volatile uint32_t*) 0x00040000;

	/*
	 * Keyboard data register.
	 */
	volatile uint32_t*	keyb_data = (volatile uint32_t*) 0x00040001;

	/*
	 * Gets a char without echo.
	 */
	char get_char() {
		while(*keyb_status != 1); // busy wait
		return *keyb_data;
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
	 * Gets a string without echo. Terminates on \n.
	 */
	void get_str(char* buf, int n) {
		int i = 0;

		while(true) {
			char c = get_char();

			// if backspace is allowed, do it
			if(c == '\b') {
				if(i > 0) {
					i--;
					buf[i] = 0;
				}

				// scrap if at the beginning of buffer
				continue;
			}
			
			// if newline return
			if(c == '\n') {
				buf[i] = '\0';
				return;
			}

			// write character in string
			buf[i] = c;

			// step forward
			i++;
			if(i >= n - 1) {
				buf[i] = '\0';
				return;
			}

		}
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
					buf[i] = 0;
					vid::backspace();
				}

				// scrap if at the beginning of buffer
				continue;
			}
			
			// if newline return
			if(c == '\n') {
				buf[i] = '\0';
				vid::newline();
				return;
			}

			// write character in string
			buf[i] = c;

			// step forward
			i++;
			if(i >= n - 1) {
				buf[i] = '\0';
				vid::newline();
				return;
			}

		}
	}

}

#endif
