#ifndef LIB_VIDEO_H
#define LIB_VIDEO_H

#include <stdint.h>

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
	 * Column of video cursor.
	 */
	int col = 0;
	
	/*
	 * Row of video cursor.
	 */
	int row = 0;

	/*
	 * Updates cursor, also writing position to cursor ports.
	 */
	void set_cursor(int new_col, int new_row) {
		col = new_col;
		row = new_row;

		*cursor_col = col;
		*cursor_row = row;
	}

	/*
	 * Get codepoint byte index in video array of given (col, row) pair.
	 *
	 * @param col codepoint byte column
	 * @param row codepoint byte row
	 */
	inline int get_cp_idx(int col, int row) {
		return col + row * COLS;
	}

	/*
	 * Scrolls video memory up 1 line.
	 */
	void scroll() {
		// copy video memory back one row
		for(int r = 1; r < ROWS; r++) {
			for(int c = 0; c < COLS; c++) {
				video[c + (r - 1) * COLS] = video[c + r * COLS];
			}
		}

		// clean last line
		for(int c = 0; c < COLS; c++) {
			video[c + (ROWS - 1)  * COLS] = '\0';
		}

		if(row > 0) set_cursor(col, row - 1);
	}

	/*
	 * Moves cursor to new line, scrolling if needed.
	 */
	void newline() {
		set_cursor(0, row + 1);

		if(row == ROWS) {
			scroll();
		}
	}

	/*
	 * Moves cursor forward by increasing column, returning to new line if needed. 
	 */
	void inc_cur() {
		set_cursor(col + 1, row);

		if(col == COLS) {
			newline();
		}
	}
	
	/*
	 * Moves cursor backward by decreasing column, decreasing line if needed. Clamps if at the 
	 * beginning of memoyr.
	 */
	void dec_cur() {
		int new_col = col - 1;
		int new_row = row;

		if(new_col == -1) {
			new_col = COLS - 1;	
			new_row--;

			if(new_row == -1) {
				new_col = 0;
				new_row = 0;
			}
		}

		set_cursor(new_col, new_row);
	}

	/*
	 * Deletes previous character on buffer and moves to it.
	 */
	void backspace() {
		dec_cur();

		int idx = get_cp_idx(col, row);
		video[idx] = '\0';
	}

	/*
	 * Inserts TAB_SIZE spaces.
	 */
	void tabulate() {
		for(int i = 0; i < TAB_SIZE; i++) {
			int idx = get_cp_idx(col, row);
			video[idx] = ' ';
			
			inc_cur();
		}
	}

	/*
	 * Prints a character on the screen.
	 *
	 * @param c character to print
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
				int idx = get_cp_idx(col, row);
				video[idx] = c;

				inc_cur();
		}
	}

	/*
	 * Prints a string on the screen.
	 *
	 * @param s string to print
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
	 *
	 * @param s string to print
	 */
	void print_strln(char* s) {
		print_str(s);
		newline();
	}

}

#endif
