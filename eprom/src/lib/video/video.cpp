#include "video.h"
#include "../util/util.h"
#include "../string/string.h"

namespace vid {
	coords::coords(int col, int row) {
		this->col = col;
		this->row = row;
	}

	coords::coords(int idx) {
		this->col = idx % video.cols;
		this->row = idx / video.cols;
	}

	int coords::get_idx() {
		return col + row * video.cols;
	}

	bool coords::validate() {
		return !(col < 0 || col >= video.cols || row < 0 || row >= video.rows);
	}

	coords coords::operator+(const coords& other) {
		return coords(this->col + other.col, this->row + other.row);
	}
	
	bool coords::operator==(const coords& other) {
		return this->col == other.col && this->row == other.row;
	}

	coords cur = coords(0, 0); 

	void set_cursor(coords new_Coords) {
		cur.col = new_Coords.col;
		cur.row = new_Coords.row;

		*video.cur_col_reg = cur.col;
		*video.cur_row_reg = cur.row;
	}


	void clear() {
		str::mset((void*) vram, '\0', video.rows * video.cols);
		set_cursor(coords(0, 0));
	}

	void scroll() {
		// copy video memory back one row
		str::mmove((void*) vram, (void*) (vram + video.cols), video.cols * (video.rows - 1));
		
		// clean last line
		str::mset((void*) (vram + (video.rows - 1) * video.cols), ' ', video.cols);
				
		if(cur.row > 0) set_cursor(coords(cur.col, cur.row - 1));
	}

	void newline() {
		set_cursor(coords(0, cur.row + 1));

		if(cur.row == video.rows) {
			scroll();
		}
	}

	void inc_cur() {
		set_cursor(coords(cur.col + 1, cur.row));

		if(cur.col == video.cols) {
			newline();
		}
	}
	
	void dec_cur() {
		int new_col = cur.col - 1;
		int new_row = cur.row;

		if(new_col == -1) {
			new_col = video.cols - 1;	
			new_row--;

			if(new_row == -1) {
				new_col = 0;
				new_row = 0;
			}
		}

		set_cursor(coords(new_col, new_row));
	}

	void backspace() {
		dec_cur();

		vram[cur.get_idx()] = '\0';
	}

	void tabulate(char c = ' ') {
		do {
			vram[cur.get_idx()] = c;
			
			inc_cur();
		} while(cur.col % TAB_SIZE);
	}

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
				vram[cur.get_idx()] = c;

				inc_cur();
		}
	}

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

	void print_int(int n) {
		if(n < 0) {
			n = -n;
			print_char('-');
		}

		print_uint((unsigned int) n);
	}

	void print_str(const char* s) {
		// loop char by char
		while(*s != '\0') {
			print_char(*s);
			s++;
		}
	}	

	void print_strln(const char* s) {
		print_str(s);
		newline();
	}

	void put_char(coords pos, char c) {
		if(!pos.validate()) {
			utl::panic("Coordinate non valide per put_char()");
		}

		vram[pos.get_idx()] = c;
	}
	
	void put_uint(coords pos, unsigned int n) {
		int pos_idx = pos.get_idx();
		coords last_pos = coords(pos_idx + 10);

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
			vram[pos_idx + j++] = temp[--i];
		}
	}

	void put_int(coords pos, int n) {
		if(!pos.validate()) {
			utl::panic("Coordinate non valide per put_int()");
		}

		int pos_idx = pos.get_idx();

		if(n < 0) {
			n = -n;
			vram[pos_idx] = '-';
		}

		put_uint(coords(pos_idx + 1), (unsigned int) n);
	}

	void put_str(coords pos, const char* s) {
		int len = str::len(s);
		int pos_idx = pos.get_idx();
		coords last_pos = coords(pos_idx + len);

		if(!pos.validate() || !last_pos.validate()) {
			utl::panic("Coordinate non valide per put_string() (forse la stringa e' troppo lunga?)");
		}

		for(int i = 0; i < len; i++) {
			vram[pos_idx + i] = *s++;
		}
	}
} // vid::
