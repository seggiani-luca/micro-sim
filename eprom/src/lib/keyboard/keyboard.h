#ifndef KEYBOARD_H
#define KEYBOARD_H

#include "../conf/hardware.h"

/**
 * Namespace for keyboard handling, including make/break code decoding, and 
 * character/string reading/getting.
 */
namespace kyb {
	/**
	 * Reference to keyboard device.
	 */
	inline hwr::dev::keyboard_device& keyboard = hwr::dev::keyboard;
	
	/**
	 * Struct for make code translations.
	 */
	struct code_trans {
		/**
		 * Make code to translate.
		 */
		unsigned char code;
		
		/**
		 * Lowercase translation.
		 */
		char lower;
		
		/**
		 * Uppercase translation.
		 */
		char upper;
	};
	
	/**
	 * Make code translation table.
	 */
	extern code_trans trans_table[];

	/**
	 * Size of make code table.
	 */
	extern int trans_num;

	/**
	 * Struct for control make codes.
	 */
	struct code_contr {
		/**
		 * Control make code.
		 */
		unsigned char code;
		
		/**
		 * Control status.
		 */
		bool status;
	};

	/**
	 * Control code table. Mapping is:
	 * - 0: Shift
	 * - 1: Control
	 * - 2: Alt
	 */
	extern code_contr contr_table[];

	/**
	 * Inline for shift status.
	 *
	 * @return shift status
	 */
	inline bool get_shift() { return contr_table[0].status; }
	
	/**
	 * Inline for control status.
	 *
	 * @return control status
	 */
	inline bool get_control() { return contr_table[1].status; }
	
	/**
	 * Inline for alt status.
	 *
	 * @return alt status
	 */
	inline bool get_alt() { return contr_table[2].status; }
	
	/**
	 * Size of control code table.
	 */
	extern int contr_num;

	/**
	 * Translates a make code to a char. Returns '\0' on invalid codes.
	 *
	 * @param code make code to translate
	 * @return corresponding char
	 */
	char translate_code(unsigned char code);

	/**
	 * Polls a char without echo.
	 *
	 * @return first pressed char if present, '\0' otherwise
	 */
	char poll_char();
	
	/**
	 * Gets a char without echo.
	 *
	 * @return first pressed char
	 */
	char get_char();

	/**
	 * Gets an unsigned integer without echo. Terminates on '\\n'. Allows to 
	 * specify the first digit to insert in the buffer (used by get_int() when 
	 * it peeks at the first character to check for sign).
	 *
	 * @param first first digit to insert
	 * @return unsigned integer from keyboard
	 */
	unsigned get_uint(unsigned int first = 0);

	/**
	 * Gets an integer without echo. Terminates on '\\n'.
	 *
	 * @return integer from keyboard
	 */
	int get_int();

	/**
	 * Gets a string without echo. Terminates on '\\n'.
	 *
	 * @param buf buffer to fill with characters 
	 * @param n max numbers of characters to read 
	 */
	void get_str(char* buf, int n);

	/**
	 * Reads a char with echo. Doesn't echo control characters ('\\b' and 
	 * '\\n').
	 *
	 * @return first pressed char
	 */
	char read_char();
	
	/**
	 * Reads an unsigned integer without echo. Terminates on '\\n'. Allows same 
	 * behavoir same as get_int().
	 *
	 * @param first first digit to insert
	 * @return unsigned integer from keyboard
	 */
	unsigned read_uint(unsigned int first = 0);

	/**
	 * Reads an integer without echo. Terminates on '\\n'.
	 *
	 * @return integer from keyboard
	 */
	int read_int();

	/**
	 * Reads a string with echo. Terminates on '\\n'.
	 *
	 * @param buf buffer to fill with characters 
	 * @param n max numbers of characters to read 
	 */
	void read_str(char* buf, int n);
} // kyb::

#endif
