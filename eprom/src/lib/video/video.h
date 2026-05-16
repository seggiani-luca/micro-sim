#ifndef VIDEO_H
#define VIDEO_H

#include "../conf/hardware.h"

/**
 * Namespace for handling console graphics, including simple 
 * character/string/int printing/putting and display cursor handling.
 */
namespace vid {
	/**
	 * Reference to video device.
	 */
	inline hwr::dev::video_device& video = hwr::dev::video;

	/**
	 * Reference to video memory.
	 */
	inline volatile uint8_t (&vram)[] = hwr::mem::vram;

	/**
	 * Reference to screen width.
	 */
	inline const int cols = hwr::dev::video.cols;
	
	/**
	 * Reference to screen height.
	 */
	inline const int rows = hwr::dev::video.rows;
	
	/**
	 * Size of tabulation.
	 */
	inline int tab_size = 4;

	/**
	 * Defines coordinates on screen, allows conversion to video array index 
	 * and validation. 
	 */
	struct coords {
		/**
		 * Row (or y position) of coordinate pair.
		 */
		int row;
		
		/**
		 * Column (or x position) of coordinate pair.
		 */
		int col;

		/**
		 * Default constructor.
		 */
		coords() {};

		/**
		 * Constructs a coordinate pair from row and column indices.
		 *
		 * @param row row of coordinate pair
		 * @param col column of coordinate pair
		 */
		coords(int row, int col);

		/**
		 * Constructs a coordinate pair from a video array index.
		 *
		 * @param idx index of coordinate in video array 
		 */
		coords(int idx);

		/**
		 * Get video index array of coordinate pair.
		 *
		 * @return video array index of this coordinate
		 */
		int get_idx();

		/**
		 * Check if coordinate pair is on screen.
		 *
		 * @return is the coordinate valid?
		 */
		bool validate();

		/**
		 * Sums coordinates.
		 *
		 * @param other other addendum
		 * @return result of sum
		 */
		coords operator+(const coords& other);

		/**
		 * Compares coordinates
		 *
		 * @param other coordinate 
		 * @return are the coordinates equal?
		 */
		bool operator==(const coords& other);
	};

	/**
	 * Cursor coordinates.
	 */
	extern coords cur;

	/**
	 * Updates cursor, also writing position to cursor ports.
	 *
	 * @param new_coords new position of cursor
	 */
	void set_cursor(coords new_coords);

	/**
	 * Clears video buffer.
	 */
	void clear();

	/**
	 * Scrolls video memory up 1 line.
	 */
	void scroll();

	/**
	 * Moves cursor to new line, scrolling if needed.
	 */
	void newline();

	/**
	 * Moves cursor forward by increasing column, returning to new line if 
	 * needed. 
	 */
	void inc_cur();
	
	/**
	 * Moves cursor backward by decreasing column, decreasing line if needed. 
	 * Clamps if at the beginning of memory.
	 */
	void dec_cur();

	/**
	 * Deletes previous character on buffer and moves to it.
	 */
	void backspace();

	/**
	 * Inserts enough tabulation characters to reach the next tab_size column 
	 * multiple.
	 *
	 * @param c tabulation character 
	 */
	void tabulate(char c);
	
	/**
	 * Removes enough characters to reach the previous tab_size column 
	 * multiple.
	 */
	void detabulate();

	/**
	 * Prints a character on the screen.
	 *
	 * @param c character to print
	 */
	void print_char(char c);

	/**
	 * Prints an unsigned integer on the screen.
	 *
	 * @param n unsigned integer to print
	 */
	void print_uint(unsigned int n);

	/**
	 * Prints an integer on the screen.
	 *
	 * @param n integer to print
	 */
	void print_int(int n);

	/**
	 * Prints a string on the screen.
	 *
	 * @param s string to print
	 */
	void print_str(const char* s);

	/**
	 * Prints a string on the screen and jumps to new line.
	 *
	 * @param s string to print
	 */
	void print_strln(const char* s);

	/**
	 * Puts a character on screen at the given coordinates.
	 *
	 * @param pos coordinate to put character at
	 * @param c character to put
	 */
	void put_char(coords pos, char c);
	
	/**
	 * Puts an unsigned integer on the screen at the given coordinates.
	 *
	 * @param pos coordinate to put unsigned integer at
	 * @param n unsigned integer to put 
	 */
	void put_uint(coords pos, unsigned int n);

	/**
	 * Puts an integer on the screen at the given coordinates.
	 *
	 * @param pos coordinate to put integer at
	 * @param n integer to put 
	 */
	void put_int(coords pos, int n);

	/**
	 * Puts a string on screen at the given coordinates.
	 *
	 * @param pos coordinate to put string at
	 * @param s string to put 
	 */
	void put_str(coords pos , const char* s);
} // vid::

#endif
