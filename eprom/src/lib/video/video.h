#ifndef LIB_VIDEO_H
#define LIB_VIDEO_H

#include "../conf/hardware.h"

/*
 * Namespace for handling console graphics, including simple character/string/int printing/putting, 
 * display cursor handling, simple geometric graphics and table drawing.
 */
namespace vid {
	/*
	 * Reference to video device.
	 */
	inline hwr::dev::video_device& video = hwr::dev::video;

	/*
	 * Reference to video memory.
	 */
	inline volatile uint8_t (&vram)[] = hwr::mem::vram;

	/*
	 * Reference to screen width.
	 */
	inline const int COLS = hwr::dev::video.cols;
	
	/*
	 * Reference to screen height.
	 */
	inline const int ROWS = hwr::dev::video.rows;
	
	/*
	 * Size of tabulation.
	 */
	inline const int TAB_SIZE = 4;

	/*
	 * Defines coordinates on screen and allows conversion to video array index and validation. 
	 */
	struct coords {
		int row;
		int col;

		/*
		 * Default constructor.
		 */
		coords() {};

		/*
		 * Constructs a coordinate pair from row and column indices.
		 */
		coords(int, int);

		/*
		 * Constructs a coordinate pair from a video array index.
		 */
		coords(int);

	 /*
	  * Get codepoint byte index in video array of coordinate pair.
	  */
		int get_idx();

		/*
		 * Check if coordinate pair is on screen.
		 */
		bool validate();

		/*
		 * Sums coordinates.
		 */
		coords operator+(const coords&);

		/*
		 * Compares coordinates
		 */
		bool operator==(const coords&);
	};

	/*
	 * Cursor coordinates.
	 */
	extern coords cur;

	/*
	 * Updates cursor, also writing position to cursor ports.
	 */
	void set_cursor(coords);

	/*
	 * Clears video buffer.
	 */
	void clear();

	/*
	 * Scrolls video memory up 1 line.
	 */
	void scroll();

	/*
	 * Moves cursor to new line, scrolling if needed.
	 */
	void newline();

	/*
	 * Moves cursor forward by increasing column, returning to new line if needed. 
	 */
	void inc_cur();
	
	/*
	 * Moves cursor backward by decreasing column, decreasing line if needed. Clamps if at the 
	 * beginning of memory.
	 */
	void dec_cur();

	/*
	 * Deletes previous character on buffer and moves to it.
	 */
	void backspace();

	/*
	 * Inserts enough space to reach the next TAB_SIZE column multiple.
	 */
	void tabulate(char);

	/*
	 * Prints a character on the screen.
	 */
	void print_char(char);

	/*
	 * Prints an unsigned integer on the screen.
	 */
	void print_uint(unsigned int);

	/*
	 * Prints an integer on the screen.
	 */
	void print_int(int);

	/*
	 * Prints a string on the screen.
	 */
	void print_str(const char*);

	/*
	 * Prints a string on the screen and jumps to new line.
	 */
	void print_strln(const char*);

	/*
	 * Puts a character on screen at the given coordinates.
	 */
	void put_char(coords, char);
	
	/*
	 * Puts an unsigned integer on the screen at the given coordinates.
	 */
	void put_uint(coords, unsigned int);

	/*
	 * Puts an integer on the screen at the given coordinates.
	 */
	void put_int(coords, int);

	/*
	 * Puts a string on screen at the given coordinates.
	 */
	void put_str(coords, const char*);
} // vid::

#endif
