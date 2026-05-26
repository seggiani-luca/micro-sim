#include "lib/lib.h"
#include "lib/video/video.h"

// memory buffer
#define BUF_SIZE 512
char buf[BUF_SIZE];
int cursor = 0;

#define BLOCK_ADDR 0

/**
 * Saves buffer to block.
 */
void save() {
	// give command and wait
	give_disk_command(BLOCK_ADDR, 1, blk::write_cmd);
	wait_for_disk();

	// write to disk
	for(int i = 0; i < BUF_SIZE; i += 2) {
		short dat = (0xff & buf[i]) | (buf[i + 1] << 8);
		*blk::disk.buf_prt = dat;
	}
}

/**
 * Loads buffer from block.
 */
void load() {
	// give command and wait
	give_disk_command(BLOCK_ADDR, 1, blk::read_cmd);
	wait_for_disk();

	// read from disk
	for(int i = 0; i < BUF_SIZE; i += 2) {
		short dat = *blk::disk.buf_prt;
		buf[i] = dat;
		buf[i + 1] = dat >> 8;
	}

	// reset cursor
	cursor = str::len(buf);
}

/**
 * Enum of modifiers.
 */
enum modif {
	SAVE,
	LOAD,
	REFRESH,
	QUIT,
	NONE
};

/**
 * Converts char to modifier.
 *
 * @param c key pressed alongside modifier key
 * @return corresponding modifier
 */
modif get_modifier(char c) {
	switch(c) {
		case 's': return SAVE;
		case 'l': return LOAD;
		case 'r': return REFRESH;
		case 'q': return QUIT;
	}

	return NONE;
}

/**
 * Increment buffer cursor.
 */
void inc_cursor() {
	cursor++;
	if(cursor >= BUF_SIZE) {
		cursor = BUF_SIZE - 1;
	}
}

/**
 * Decrement buffer cursor.
 */
void dec_cursor() {
	cursor--;
	if(cursor < 0) {
		cursor = 0;
	}
}

#define MAX_ROWS 512

/**
 * Gets pointers to at most MAX_ROWS rows in the buffer.
 */
void get_rows(int* rows, int& n_rows) {
	n_rows = 1;
	for(int i = 0; i < BUF_SIZE; i++) {
		if(buf[i] == '\n') {
			rows[n_rows] = i + 1;
			n_rows++;
		}
	}
}

/**
 * Cleans row j.
 */
void clean_row(int i) {
	for(int j = 0; j < vid::cols; j += 4) { 
		*(uint32_t*) (hwr::mem::vram + j + i * vid::cols) = 0; 
	}
}

/**
 * Refreshes video with buffer.
 */
void refresh_vid() {
	// reset video cursor
	vid::set_cursor({0, 0});

	// get row pointers
	static int rows[MAX_ROWS];
	static int n_rows;
	get_rows(rows, n_rows);

	// get rows to draw
	int beg = 0;
	while(n_rows - beg > vid::rows) {
		beg += vid::rows;
	}

	// actually draw rows
	for(int i = beg; i < n_rows; i++) {
		if(i != beg) vid::newline();

		// print line number
		vid::print_int(i);
		vid::print_char(' ');
		
		// print line
		int j = rows[i];
		while(char c = buf[j++]) {
			if(c == '\n') break;
			vid::print_char(c);
		}
	}
}

void main() {
	while(1) {
		// get char and modifier key
		char c = kyb::get_char();
		bool ctl = kyb::get_control();

		if(ctl) {
			// get modifier if modifier key was pressed
			modif mod = get_modifier(c);
			switch(mod) {
				case SAVE: save(); break;
				case LOAD: 
					load();
					refresh_vid();
					break;
				case REFRESH:
					refresh_vid();
					break;
				case QUIT:
					utl::halt();
					break;
				default: continue;
			}
		} else {
			// decrement cursor on backspace 
			if(c == '\b') {
				dec_cursor();
				buf[cursor] = '\0';
			} 
			// increment cursor filling buffer
			else {
				buf[cursor] = c;
				inc_cursor();
			}

			// draw screen
			refresh_vid();	
		}
	}

	utl::wait();
}
