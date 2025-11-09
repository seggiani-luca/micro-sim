#ifndef KEYBOARD_H
#define KEYBOARD_H

#include "../conf/hardware.h"

/*
 * Namespace for handling keyboard, including character/string reading/getting.
 */
namespace kyb {
	/*
	 * Reference to keyboard device.
	 */
	inline hwr::dev::keyboard_device& keyboard = hwr::dev::keyboard;
	
	/*
	 * Polls a char without echo.
	 */
	char poll_char();
	
	/*
	 * Gets a char without echo.
	 */
	char get_char();

	/*
	 * Gets an unsigned integer without echo. Terminates on \n. First allows to specify the first 
	 * character to insert in the buffer (used by get_int() when it peeks at the first character to 
	 * check for sign).
	 */
	unsigned get_uint(unsigned int first = 0);

	/*
	 * Gets an integer without echo. Terminates on \n.
	 */
	int get_int();

	/*
	 * Gets a string without echo. Terminates on \n.
	 */
	void get_str(char* buf, int n);

	/*
	 * Reads a char with echo. Doesn't echo control characters (\b and \n)
	 */
	char read_char();
	
	/*
	 * Reads an unsigned integer without echo. Terminates on \n. First is same as get_int() 
	 */
	unsigned read_uint(unsigned int first = 0);

	/*
	 * Reads an integer without echo. Terminates on \n.
	 */
	int read_int();

	/*
	 * Reads a string with echo. Terminates on \n.
	 */
	void read_str(char* buf, int n);
} // kyb::

#endif
