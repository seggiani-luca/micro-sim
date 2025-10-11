#ifndef LIB_VIDEO_H
#define LIB_VIDEO_H

#include <stdint.h>

namespace utl {
	void panic(char*);
	extern "C" void debugger();
}
namespace str {
	unsigned int len(const char* s);
	void* mmove(void*, const void*, unsigned int);
	void* mset(void*, char, unsigned int);
}

namespace vid {
	/*
	 * Number of columns in text mode.
	 */
	#define COLS 80

	/*
	 * Number of rows in text mode.
	 */
	#define ROWS 30

	/*
	 * Size of tabulation.
	 */
	#define TAB_SIZE 4

	/*
	 * Special character set.
	 */
	const char SYM_SHADE_LIGHT = 0x80;
	const char SYM_SHADE_MEDIUM = 0x81;
	const char SYM_SHADE_DARK = 0x82;
	const char SYM_TABLE_VERT = 0x90;
	const char SYM_TABLE_HORIZ = 0x91;
	const char SYM_TABLE_CORNER_TOP_RIGHT = 0x92;
	const char SYM_TABLE_CORNER_TOP_LEFT = 0x93;
	const char SYM_TABLE_CORNER_BOTTOM_LEFT = 0x94;
	const char SYM_TABLE_CORNER_BOTTOM_RIGHT = 0x95;
	const char SYM_TABLE_CROSS_RIGHT = 0x96;
	const char SYM_TABLE_CROSS_LEFT = 0x97;
	const char SYM_TABLE_CROSS_BOTTOM = 0x98;
	const char SYM_TABLE_CROSS_TOP = 0x99;
	const char SYM_TABLE_CROSS = 0x9a;
	const char SYM_HEART = 0xa0;
	const char SYM_DIAMOND = 0xa1;
	const char SYM_CLUB = 0xa2;
	const char SYM_SPADE = 0xa3;
	const char SYM_NOTE = 0xa4;
	const char SYM_NOTES = 0xa5;

	/*
	 * Video array, points to the 5 KiB of video memory. Video characters are 1 bytes representing 
	 * character codepoint.
	 */
	volatile uint8_t video[3072] __attribute__((section(".video")));

	/*
	 * Port to specify cursor row.
	 */
	volatile uint32_t* cursor_row = (volatile uint32_t*) 0x00030000;

	/*
	 * Port to specify cursor column.
	 */
	volatile uint32_t* cursor_col = (volatile uint32_t*) 0x00030001;

	/*
	 * Defines coordinates on screen and allows conversion to video array index and validation. 
	 */
	struct coords {
		int col;
		int row;

		/*
		 * Constructs a coordinate pair from column and row indices.
		 */
		coords(int col, int row) {
			this->col = col;
			this->row = row;
		}

		/*
		 * Constructs a coordinate pair from a video array index.
		 */
		coords(int idx) {
			this->col = idx % COLS;
			this->row = idx / ROWS;
		}

	 /*
	  * Get codepoint byte index in video array of coordinate pair.
	  */
		inline int get_idx() {
			return col + row * COLS;
		}

		/*
		 * Check if coordinate pair is on screen.
		 */
		bool validate() {
			return !(col < 0 || col >= COLS || row < 0 || row >= ROWS);
		}
	};

	/*
	 * Cursor coordinates.
	 */
	coords cur = coords(0, 0); 

	/*
	 * Updates cursor, also writing position to cursor ports.
	 */
	void set_cursor(coords new_coords) {
		cur.col = new_coords.col;
		cur.row = new_coords.row;

		*cursor_col = cur.col;
		*cursor_row = cur.row;
	}

	void print_str(const char*);

	/*
	 * Scrolls video memory up 1 line.
	 */
	void scroll() {
		// copy video memory back one row
		str::mmove((void*) video, (void*) (video + COLS), COLS * (ROWS - 1));
		
		// clean last line
		str::mset((void*) (video + (ROWS - 1) * COLS), ' ', COLS);
				
		if(cur.row > 0) set_cursor(coords(cur.col, cur.row - 1));
	}

	/*
	 * Moves cursor to new line, scrolling if needed.
	 */
	void newline() {
		set_cursor(coords(0, cur.row + 1));

		if(cur.row == ROWS) {
			scroll();
		}
	}

	/*
	 * Moves cursor forward by increasing column, returning to new line if needed. 
	 */
	void inc_cur() {
		set_cursor(coords(cur.col + 1, cur.row));

		if(cur.col == COLS) {
			newline();
		}
	}
	
	/*
	 * Moves cursor backward by decreasing column, decreasing line if needed. Clamps if at the 
	 * beginning of memoyr.
	 */
	void dec_cur() {
		int new_col = cur.col - 1;
		int new_row = cur.row;

		if(new_col == -1) {
			new_col = COLS - 1;	
			new_row--;

			if(new_row == -1) {
				new_col = 0;
				new_row = 0;
			}
		}

		set_cursor(coords(new_col, new_row));
	}

	/*
	 * Deletes previous character on buffer and moves to it.
	 */
	void backspace() {
		dec_cur();

		int idx = cur.get_idx();
		video[idx] = '\0';
	}

	/*
	 * Inserts TAB_SIZE spaces.
	 */
	void tabulate() {
		for(int i = 0; i < TAB_SIZE; i++) {
			int idx = cur.get_idx();
			video[idx] = ' ';
			
			inc_cur();
		}
	}

	/*
	 * Prints a character on the screen.
	 */
	void print_char(char c) {
		switch(c) {
			case '\n':
				newline();
				break;

			case '\b':
				backspace();
				break;

			case '\t':
				tabulate();
				break;

			default:
				int idx = cur.get_idx();
				video[idx] = c;

				inc_cur();
		}
	}

	/*
	 * Prints a string on the screen.
	 */
	void print_str(const char* s) {
		// loop char by char
		while(*s != '\0') {
			print_char(*s);
			s++;
		}
	}	

	/*
	 * Prints a string on the screen and jumps to new line.
	 */
	void print_strln(const char* s) {
		print_str(s);
		newline();
	}

	/*
	 * Puts a character on screen at the given coordinates.
	 */
	void put_char(coords pos, char c) {
		if(!pos.validate()) {
			utl::panic("Coordinate invalide per put_char()");
		}

		video[pos.get_idx()] = c;
	}

	/*
	 * Puts a string on screen at the given coordinates.
	 */
	void put_str(coords pos, const char* s) {
		int len = str::len(s);
		int pos_idx = pos.get_idx();
		coords last_pos = coords(pos_idx + len);

		if(!pos.validate() || !last_pos.validate()) {
			utl::panic("Coordinate invalide per put_string() (forse la stringa e' troppo lunga?)");
		}

		for(int i = 0; i < len; i++) {
			video[pos_idx + i] = *s++;
		}
	}

}

#endif
