#include "lib/lib.h"
#include "lib/video/video.h"

// memory buffer
#define BUF_SIZE 512
char buf[BUF_SIZE];
int cursor = 0;

#define BLOCK_ADDR 0

/**
 * Waits for disk.
 */
void wait_for_disk() {
	while(*blk::disk.ctl_prt != 1);
}

/**
 * Gives disk command.
 */
void give_disk_command(int addr, int scn, int cmd) {
	*blk::disk.lba_prt = addr;
	*blk::disk.scn_prt = scn;
	*blk::disk.ctl_prt = cmd;
}

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

/**
 * Counts row on buffer.
 */
int count_rows(int& idx) {
	int ret = 1;
	for(int i = 0; i < BUF_SIZE; i++) {
		if(buf[i] == '\n') {
			ret++;
			idx = i;
		}
	}

	return ret;
}

/**
 * Refreshes video with buffer.
 */
void refresh_vid() {
	// clean dirty rows
	for(int i = 0; i < vid::rows; i++) {
		for(int j = 0; j < vid::cols; j += 4) { 
			*(uint32_t*) (hwr::mem::vram + j + i * vid::cols) = 0; 
		}
	}

	// reset cursor
	cursor = 0;
	vid::set_cursor({0, 0});

	// print buffer 
	for(int i = cursor; i < BUF_SIZE; i++) {
		char c = buf[i];
		if(c) {
			vid::print_char(c);
			cursor++;
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
