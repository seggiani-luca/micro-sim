#ifndef KEYBOARD_H
#define KEYBOARD_H

/*
 * Namespace for handling keyboard, including character/string reading/getting.
 */
namespace kyb {
	/*
	 * Gets a char without echo.
	 */
	char get_char();

	/*
	 * Gets an unsigned integer without echo. Terminates on \n.
	 */
	unsigned get_uint(unsigned int);

	/*
	 * Gets an integer without echo. Terminates on \n.
	 */
	int get_int();

	/*
	 * Gets a string without echo. Terminates on \n.
	 */
	void get_str(char*, int);

	/*
	 * Reads a char with echo. Doesn't echo control characters (\b and \n)
	 */
	char read_char();
	
	/*
	 * Reads an unsigned integer without echo. Terminates on \n.
	 */
	unsigned read_uint(unsigned int);

	/*
	 * Reads an integer without echo. Terminates on \n.
	 */
	int read_int();

	/*
	 * Reads a string with echo. Terminates on \n.
	 */
	void read_str(char*, int);
} // kyb::

#endif
