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
	 * Video array, points to the 5 KiB of video memory. Video characters are 2 bytes: first byte is 
	 * character codepoint, second byte is style.
	 */
	volatile uint8_t video[5120] __attribute__((section(".video"))); 

	/*
	 * Column of video cursor.
	 */
	int col = 0;
	
	/*
	 * Row of video cursor.
	 */
	int row = 0;

	/*
	 * Get codepoint byte index in video array of given (col, row) pair.
	 *
	 * @param col codepoint byte column
	 * @param row codepoint byte row
	 */
	inline int get_cp_idx(int col, int row) {
		return (col + row * COLS) * 2;
	}

	/*
	 * Get style byte index in video array of given (col, row) pair.
	 *
	 * @param col style byte column
	 * @param row style byte row
	 */
	inline int get_st_idx(int col, int row) {
		return get_cp_idx(col, row) + 1;
	}

	/*
	 * Scrolls video memory up 1 line.
	 */
	void scroll() {
		// copy video memory back
		for(int r = 1; r < ROWS; r++) {
			for(int c = 0; c < COLS * 2; c++) {
				video[c + (r - 1) * COLS * 2] = video[c + r * COLS * 2];
			}
		}

		// clean last line
		for(int c = 0; c < COLS * 2; c++) {
			video[c + (ROWS - 1)  * COLS * 2] = '\0';
		}

		if(row > 0) row--;
	}

	/*
	 * Moves cursor to new line, scrolling if needed.
	 */
	void newline() {
		row++;
		col = 0;

		if(row == ROWS) {
			scroll();
		}
	}

	/*
	 * Moves cursor forward by increasing column, returning to new line if needed. 
	 */
	void inc_cur() {
		col++;

		if(col == COLS) {
			col = 0;
			newline();
		}
	}
	
	/*
	 * Moves cursor backward by decreasing column, decreasing line if needed. Clamps if at the 
	 * beginning of memoyr.
	 */
	void dec_cur() {
		col--;

		if(col == -1) {
			col = COLS - 1;
			
			row--;

			if(row == -1) {
				row = 0;
				col = 0;
			}
		}
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
