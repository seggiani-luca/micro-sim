#ifndef LIB_VIDEO_H
#define LIB_VIDEO_H

#include <stdint.h>

namespace utl {
	void panic(const char*);
	extern "C" void debugger();
	void wait();
}
namespace str {
	unsigned int len(const char* s);
	void* mmove(void*, const void*, unsigned int);
	void* mset(void*, char, unsigned int);
}

/*
 * Namespace for handling console graphics, including simple character/string/int printing/putting, 
 * display cursor handling, simple geometric graphics and table drawing.
 */
namespace vid {
	/*
	 * Number of columns in text mode.
	 */
	const int COLS = 80;

	/*
	 * Number of rows in text mode.
	 */
	const int ROWS = 30;

	/*
	 * Size of tabulation.
	 */
	const int TAB_SIZE = 4;

	/*
	 * Special character set.
	 */
	const char SYM_SHADE = 0x80;
	const char SYM_SHADE_MEDIUM = 0x81;
	const char SYM_SHADE_DARK = 0x82;

	const char SYM_HEART = 0xf0;
	const char SYM_DIAMOND = 0xf1;
	const char SYM_CLUB = 0xf2;
	const char SYM_SPADE = 0xf3;
	const char SYM_NOTE = 0xf4;
	const char SYM_NOTES = 0xf5;

	/*
	 * Table character set
	 */
	const char SYM_TABLE_SOLID = 0x90;
	const char SYM_TABLE_DOTTED = 0xa0;
	const char SYM_TABLE_DOUBLE = 0xb0;

	/*
	 * Table character offsets to use different table styles.
	 */
	const char TABLE_VERT_OFF = 0;
	const char TABLE_HORIZ_OFF = 1;
	const char TABLE_CORNER_TOP_RIGHT_OFF = 2;
	const char TABLE_CORNER_TOP_LEFT_OFF = 3;
	const char TABLE_CORNER_BOTTOM_LEFT_OFF = 4;
	const char TABLE_CORNER_BOTTOM_RIGHT_OFF = 5;
	const char TABLE_CROSS_RIGHT_OFF = 6;
	const char TABLE_CROSS_LEFT_OFF = 7;
	const char TABLE_CROSS_TOP_OFF = 8;
	const char TABLE_CROSS_BOTTOM_OFF = 9;
	const char TABLE_CROSS_OFF = 10;

	/*
	 * Video array, points to the 5 KiB of video memory. Video characters are 1 bytes representing 
	 * character codepoint.
	 */
	inline volatile uint8_t video[3072] __attribute__((section(".video")));

	/*
	 * Port to specify cursor column.
	 */
	inline volatile uint32_t* cursor_col = (volatile uint32_t*) 0x00030001;
	
	/*
	 * Port to specify cursor row.
	 */
	inline volatile uint32_t* cursor_row = (volatile uint32_t*) 0x00030000;

	/*
	 * Defines coordinates on screen and allows conversion to video array index and validation. 
	 */
	struct Coords {
		int col;
		int row;

		/*
		 * Constructs a coordinate pair from column and row indices.
		 */
		Coords(int col, int row) {
			this->col = col;
			this->row = row;
		}

		/*
		 * Constructs a coordinate pair from a video array index.
		 */
		Coords(int idx) {
			this->col = idx % COLS;
			this->row = idx / COLS;
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
	Coords cur = Coords(0, 0); 

	/*
	 * Updates cursor, also writing position to cursor ports.
	 */
	void set_cursor(Coords new_Coords) {
		cur.col = new_Coords.col;
		cur.row = new_Coords.row;

		*cursor_col = cur.col;
		*cursor_row = cur.row;
	}

	/*
	 * Clears video buffer.
	 */
	void clear() {
		str::mset((void*) video, '\0', ROWS * COLS);
		set_cursor(Coords(0, 0));
	}

	/*
	 * Scrolls video memory up 1 line.
	 */
	void scroll() {
		// copy video memory back one row
		str::mmove((void*) video, (void*) (video + COLS), COLS * (ROWS - 1));
		
		// clean last line
		str::mset((void*) (video + (ROWS - 1) * COLS), ' ', COLS);
				
		if(cur.row > 0) set_cursor(Coords(cur.col, cur.row - 1));
	}

	/*
	 * Moves cursor to new line, scrolling if needed.
	 */
	void newline() {
		set_cursor(Coords(0, cur.row + 1));

		if(cur.row == ROWS) {
			scroll();
		}
	}

	/*
	 * Moves cursor forward by increasing column, returning to new line if needed. 
	 */
	void inc_cur() {
		set_cursor(Coords(cur.col + 1, cur.row));

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

		set_cursor(Coords(new_col, new_row));
	}

	/*
	 * Deletes previous character on buffer and moves to it.
	 */
	void backspace() {
		dec_cur();

		video[cur.get_idx()] = '\0';
	}

	/*
	 * Inserts enough space to reach the next TAB_SIZE column multiple.
	 */
	void tabulate(char c = ' ') {
		do {
			video[cur.get_idx()] = c;
			
			inc_cur();
		} while(cur.col % TAB_SIZE);
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
				video[cur.get_idx()] = c;

				inc_cur();
		}
	}

	/*
	 * Prints an unsigned integer on the screen.
	 */
	void print_uint(unsigned int n) {
		char temp[10];
		int i = 0;

		do {
			temp[i++] = n % 10 + '0';
			n /= 10;
		} while (n > 0);
	
		while(i > 0) {
			print_char(temp[--i]);
		}
	}

	/*
	 * Prints an integer on the screen.
	 */
	void print_int(int n) {
		if(n < 0) {
			n = -n;
			print_char('-');
		}

		print_uint((unsigned int) n);
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
	void put_char(Coords pos, char c) {
		if(!pos.validate()) {
			utl::panic("Coordinate non valide per put_char()");
		}

		video[pos.get_idx()] = c;
	}
	
	/*
	 * Puts an unsigned integer on the screen at the given coordinates.
	 */
	void put_uint(Coords pos, unsigned int n) {
		int pos_idx = pos.get_idx();
		Coords last_pos = Coords(pos_idx + 10);

		if(!pos.validate() || !last_pos.validate()) {
			utl::panic("Coordinate non valide per put_uint() (il numero puo' occupare 10 caratteri)");
		}

		char temp[10];
		int i = 0;

		do {
			temp[i++] = n % 10 + '0';
			n /= 10;
		} while (n > 0);
	
		int j = 0;
		while(i > 0) {
			video[pos_idx + j++] = temp[--i];
		}
	}

	/*
	 * Puts an integer on the screen at the given coordinates.
	 */
	void put_int(Coords pos, int n) {
		if(!pos.validate()) {
			utl::panic("Coordinate non valide per put_int()");
		}

		int pos_idx = pos.get_idx();

		if(n < 0) {
			n = -n;
			video[pos_idx] = '-';
		}

		put_uint(Coords(pos_idx + 1), (unsigned int) n);
	}

	/*
	 * Puts a string on screen at the given coordinates.
	 */
	void put_str(Coords pos, const char* s) {
		int len = str::len(s);
		int pos_idx = pos.get_idx();
		Coords last_pos = Coords(pos_idx + len);

		if(!pos.validate() || !last_pos.validate()) {
			utl::panic("Coordinate non valide per put_string() (forse la stringa e' troppo lunga?)");
		}

		for(int i = 0; i < len; i++) {
			video[pos_idx + i] = *s++;
		}
	}

	/*
	 * Namespace that includes functions for simple geometric shape drawing.
	 */
	namespace graph {

		/*
		 * Draws a filled box from top left (tl) to bottom right (br) coordinates.
		 */
		void rect(Coords tl, Coords br, char fill = SYM_SHADE_DARK) {
			if(!tl.validate() || !br.validate()) {
				utl::panic("Coordinate non valide per draw_rect()");
			}

			for(int c = tl.col; c <= br.col; c++) {
				for(int r = tl.row; r <= br.row; r++) {
					put_char(Coords(c, r), fill);
				}
			}
		}
		
		/*
		 * Draws an outlined box from top left (tl) to bottom right (br) coordinates.
		 */
		void orect(Coords tl, Coords br, char line = SYM_SHADE_DARK) {
			if(!tl.validate() || !br.validate()) {
				utl::panic("Coordinate non valide per draw_orect()");
			}

			// top
			for(int c = tl.col; c <= br.col; c++) {
					put_char(Coords(c, tl.row), line);
			}

			// bottom
			for(int c = tl.col; c <= br.col; c++) {
					put_char(Coords(c, br.row), line);
			}

			// left
			for(int r = tl.row + 1; r <= br.row - 1; r++) {
					put_char(Coords(tl.col, r), line);
			}
			
			// right
			for(int r = tl.row + 1; r <= br.row - 1; r++) {
					put_char(Coords(br.col, r), line);
			}
		}

		/*
		 * Draws a circle from the center (c) of radius (r), using a modified version of the midpoint 
		 * algorithm. Because of quantization error, adjacent odd-even pairs look similar if not equal.
		 */
		void circ(Coords c, int r, char line = SYM_SHADE_DARK) {
			if(r == 0) return;

			// we are trying to approximate:
			// x^2 + 4 * (y^2) = r^2
			// this is because screen characters are 8x16, hence we "squash" by a factor of 2 on the 
			// vertical axis to retain square proportions.

			// higher octant, choose x as the dominant direction
			int x = 0;
			int y = -(r + 1) / 2; // prefer approximating away from zero
			int p = -2 * r + 1; 

			while(x < - 4 * y) {
				for(int i = -x; i <= x; i++) {
					put_char(Coords(c.col + i, c.row + y), line);
					put_char(Coords(c.col + i, c.row - y), line);
				}
				
				if(p > 0) {
					y++;
					p += 2 * x + 8 * y + 1;
				} else {
					p += 2 * x + 1;
				}

				x++;
			}

			// lower octant, choose y as the dominant direction
			while(y <= 0) {
				for(int i = -x; i <= x; i++) {
					put_char(Coords(c.col + i, c.row + y), line);
					put_char(Coords(c.col + i, c.row - y), line);
				}
				
				if(p > 0) {
					x++;
					p += 8 * y - 2 * x + 4;
				} else {
					p += 8 * y + 4;
				}

				y++;
			}
		}

		/*
		 * Draws an outlined circle from the center (c) of radius (r), using the same algorithm as 
		 * draw_circ(). 
		 */
		void ocirc(Coords c, int r, char line = SYM_SHADE_DARK) {
			if(r == 0) return;

			// higher octant
			int x = 0;
			int y = -(r + 1) / 2; // prefer approximating away from zero

			int p = -2 * r + 1;

			while(x < - 4 * y) {
				put_char(Coords(c.col + x, c.row + y), line);
				put_char(Coords(c.col + x, c.row - y), line);
				put_char(Coords(c.col - x, c.row + y), line);
				put_char(Coords(c.col - x, c.row - y), line);
				
				if(p > 0) {
					y++;
					p += 2 * x + 8 * y + 1;
				} else {
					p += 2 * x + 1;
				}

				x++;
			}

			// lower octant
			while(y <= 0) {
				put_char(Coords(c.col + x, c.row + y), line);
				put_char(Coords(c.col + x, c.row - y), line);
				put_char(Coords(c.col - x, c.row + y), line);
				put_char(Coords(c.col - x, c.row - y), line);
				
				if(p > 0) {
					x++;
					p += 8 * y - 2 * x + 4;
				} else {
					p += 8 * y + 4;
				}

				y++;
			}
		}

	} // vid::graph::
	
	/*
	 * Namespace for table drawing.
	 */
	namespace tab {

		/*
		 * Draws a table from top left (tl) to bottom right (br) coordinates.
		 */
		void draw(Coords tl, Coords br, char base_char = SYM_TABLE_SOLID) {
			if(!tl.validate() || !br.validate()) {
				utl::panic("Coordinate non valide per draw_table()");
			}

			// corners
			put_char(tl, base_char + TABLE_CORNER_BOTTOM_RIGHT_OFF);
			put_char(br, base_char + TABLE_CORNER_TOP_LEFT_OFF);
			put_char(Coords(tl.col, br.row), base_char + TABLE_CORNER_TOP_RIGHT_OFF);
			put_char(Coords(br.col, tl.row), base_char + TABLE_CORNER_BOTTOM_LEFT_OFF);

			// top
			for(int c = tl.col + 1; c <= br.col - 1; c++) {
					put_char(Coords(c, tl.row), base_char + TABLE_HORIZ_OFF);
			}

			// bottom
			for(int c = tl.col + 1; c <= br.col - 1; c++) {
					put_char(Coords(c, br.row), base_char + TABLE_HORIZ_OFF);
			}

			// left
			for(int r = tl.row + 1; r <= br.row - 1; r++) {
					put_char(Coords(tl.col, r), base_char + TABLE_VERT_OFF);
			}
			
			// right
			for(int r = tl.row + 1; r <= br.row - 1; r++) {
					put_char(Coords(br.col, r), base_char + TABLE_VERT_OFF);
			}
		}

		/*
		 * Draws an horizontal table line from left coordinate (l) of length len, properly intersecting 
		 * edges.
		 */
		void horiz_line(Coords l, int len, char base_char = SYM_TABLE_SOLID) {
			if(!l.validate() || l.col + len >= COLS) {
				utl::panic("Coordinate non valide per horiz_line()");
			}

			int l_idx = l.get_idx();

			// start
			if(video[l_idx] == base_char + TABLE_VERT_OFF) {
				video[l_idx] = base_char + TABLE_CROSS_RIGHT_OFF;
			} else {
				video[l_idx] = base_char + TABLE_HORIZ_OFF;
			}

			// line body
			int i = 1;
			for(; i < len; i++) {
				if(video[l_idx + i] == base_char + TABLE_VERT_OFF) {
					video[l_idx + i] = base_char + TABLE_CROSS_OFF;
				} else {
					video[l_idx + i] = base_char + TABLE_HORIZ_OFF;
				}
			}

			// end
			if(video[l_idx + i] == base_char + TABLE_VERT_OFF) {
				video[l_idx + i] = base_char + TABLE_CROSS_LEFT_OFF;
			} else {
				video[l_idx + i] = base_char + TABLE_HORIZ_OFF;
			}
		}
		
		/*
		 * Draws a vertical table line from top coordinate (t) of length len, properly intersecting 
		 * edges.
		 */
		void vert_line(Coords t, int len, char base_char = SYM_TABLE_SOLID) {
			if(!t.validate() || t.row + len >= ROWS) {
				utl::panic("Coordinate non valide per horiz_line()");
			}

			int t_idx = t.get_idx();

			// start
			if(video[t_idx] == base_char + TABLE_HORIZ_OFF) {
				video[t_idx] = base_char + TABLE_CROSS_BOTTOM_OFF;
			} else {
				video[t_idx] = base_char + TABLE_VERT_OFF;
			}

			// line body
			int i = 1;
			for(; i < len; i++) {
				if(video[t_idx + i * COLS] == base_char + TABLE_HORIZ_OFF) {
					video[t_idx + i * COLS] = base_char + TABLE_CROSS_OFF;
				} else {
					video[t_idx + i * COLS] = base_char + TABLE_VERT_OFF;
				}
			}

			// end
			if(video[t_idx + i * COLS] == base_char + TABLE_HORIZ_OFF) {
				video[t_idx + i * COLS] = base_char + TABLE_CROSS_TOP_OFF;
			} else {
				video[t_idx + i * COLS] = base_char + TABLE_VERT_OFF;
			}
		}

	} // vid::tab::

} // vid::

#endif
