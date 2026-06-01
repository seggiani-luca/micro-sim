#include "../lib/lib.h"

/**
 * Text editor implementation.
 */
namespace edt {
	/**
	 * Size constants-
	 */
	#define FILE_BUF_SIZE 4096

	/**
	 * Represents a piece of a line (fixed size element of a rope).
	 */
	#define PIECE_SIZE 32
	struct piece {
		char buf[PIECE_SIZE];
		piece* next;
	};
	
	/**
	 * Path of current file.
	 */
	char* file;

	/**
	 * Array of pieces, handled with a list allocator.
	 */
	#define MAX_PIECES 1024
	piece* pieces;

	/**
	 * First piece of free piece list.
	 */
	piece* pieces_free;

	/**
	 * Allocates a new piece.
	 *
	 * @return an allocated piece
	 */
	piece* alloc_piece() {
		piece* piece = pieces_free;
		if(piece == NULL) utl::panic("Memoria esaurita");
		pieces_free = pieces_free->next;
		piece->next = NULL;
		mem::set(piece->buf, 0, PIECE_SIZE);
		return piece;
	}

	/**
	 * Frees an allocated piece.
	 *
	 * @param piece the piece to free
	 */
	void free_piece(piece* piece) {
		piece->next = pieces_free;
		pieces_free = piece;
	}

	/**
	 * Array of lines, handled as a static array.
	 */
	#define MAX_LINES 1024
	piece** lines;

	/**
	 * Number of lines in the file.
	 */
	int n_lines;

	/**
	 * Beginning of line window.
	 */
	int window_line;
	
	/**
	 * Size of line window.
	 */
	int window_size = vid::rows - 1;

	/**
	 * Enum for editor status.
	 */
	enum editor_status {
		COMMAND,
		INSERT
	};

	/**
	 * Current editor status.
	 */
	editor_status stat;

	/**
	 * Returns current status as a string.
	 */
	const char* status_str() {
		switch(stat) {
			case COMMAND: return "comando";
			case INSERT: return "inserisci";
			default: return NULL;
		}
	}

	/**
	 * Current line.
	 */
	int cur_line;
	
	/**
	 * Current character in line.
	 */
	int cur_char;
	
	/**
	 * Decrements current line.
	 */
	void dec_cur_line() {
		if(cur_line > 0) cur_line--;
		char* line = lines[cur_line]->buf;
		while(line[cur_char] == '\0' && cur_char != 0) cur_char--;
	}

	/**
	 * Advances current line.
	 */
	void adv_cur_line() {
		if(cur_line < n_lines - 1) cur_line++;
		char* line = lines[cur_line]->buf;
		while(line[cur_char] == '\0' && cur_char != 0) cur_char--;
	}

	/**
	 * Decrements current character.
	 */
	void dec_cur_char() {
		if(cur_char > 0) cur_char--;
	}

	/**
	 * Advances current character.
	 */
	void adv_cur_char() {
		if(cur_char >= PIECE_SIZE - 1) return;
		char* line = lines[cur_line]->buf;
		if(line[cur_char + 1] != '\0') cur_char++;
	}

	/**
	 * Opens the current file.
	 *
	 * @return was the operation succesful?
	 */
	bool open_file() {
		// read file from disk 
		char fbuf[FILE_BUF_SIZE];
		int fsiz = blk::dir::read_file(file, fbuf, FILE_BUF_SIZE, 
				blk::dir::cur);
		if(fsiz == -1) {
			vid::print_strln("Errore lettura file");
			return 0;
		}
	
		// initialize first piece 
		piece* p = alloc_piece();
		int n_chars = 0;

		// initialize first line
		lines[n_lines] = p; 

		// go through file allocating pieces and lines
		for(int i = 0; i < fsiz; i++) {
			char c = fbuf[i];
			
			// next line
			if(c == '\n') {
				if(n_lines == MAX_LINES - 1) return 0;
				p = alloc_piece();
				n_chars = 0;
				lines[++n_lines] = p;
				continue;
			}

			// append to piece
			p->buf[n_chars++] = c;

			// piece full
			if(n_chars == PIECE_SIZE) {
				piece* next = alloc_piece();
				n_chars = 0;
				p->next = next;
			}
		}
		n_lines++;

		return 1;
	}

	/**
	 * Closes current file.
	 *
	 * @return was the operation succesful?
	 */
	bool close_file() {
		// initialize file buffer
		char fbuf[FILE_BUF_SIZE];
		str::cpy(fbuf, "Scemo chi legge\nDoppio scemo chi rilegge\n");
		int fsiz = str::len(fbuf);

		// write file to disk
		if(!blk::dir::update_file(file, fbuf, fsiz, blk::dir::cur)) {
			vid::print_strln("Errore scrittura file");
		}

		return 1;
	}

	/**
	 * Displays editor screen.
	 */
	void print_screen() {
		// clear screen
		vid::clear();

		// print file lines
		int act_lines = window_line + window_size;
		if(act_lines > n_lines) act_lines = n_lines;
		for(int i = window_line; i < act_lines; i++) {
			piece* p = lines[i];

			// print line buffers
			do {
				for(int j = 0; j < PIECE_SIZE; j++) {
					char c = p->buf[j];
					if(c == '\0') break;
					vid::print_char(c);
				}

				// new line
				vid::newline();
			} while((p = p->next));
		}

		// move cursor
		vid::set_cursor({cur_line - window_line, cur_char});

		// print status
		vid::put_str({vid::rows - 1, 0}, status_str());
		vid::put_str({vid::rows - 1, 10}, "ln: ");
		vid::put_uint({vid::rows - 1, 14}, cur_line);
		vid::put_str({vid::rows - 1, 20}, "ch: ");
		vid::put_uint({vid::rows - 1, 24}, cur_char);
	}

	/**
	 * Main editor loop.
	 *
	 * @return should the editor exit?
	 */
	bool loop() {
		// print screen
		print_screen();
		
		// get input
		char c = kyb::get_char();

		// update status
		switch(stat) {
			case COMMAND: {
				switch(c) {
					case ESC: vid::clear(); return 1;
					case 'i': stat = INSERT; break;
					case 'h': dec_cur_char(); break;
					case 'l': adv_cur_char(); break;
					case 'j': dec_cur_line(); break;
					case 'k': adv_cur_line(); break;
				}
				break;
			}
			case INSERT: {
				if(c == ESC) {
					stat = COMMAND;
					break;
				}
				break;
			}
		}

		return 0;
	}
	
} // edt::

using namespace edt;
namespace app {
	ENTRY(editor) {
		// grab memory
		piece _pieces[MAX_PIECES];
		pieces = _pieces;
		piece* _lines[MAX_LINES];
		lines = _lines;

		// initialize pieces list allocator 
		for(int i = 0; i < MAX_PIECES - 1; i++) {
			pieces[i].next = &pieces[i + 1];
		}
		pieces[MAX_PIECES - 1].next = NULL;
		pieces_free = &pieces[0];		

		// initialize lines
		n_lines = 0;
	
		// initialize editor
		stat = COMMAND;
		cur_line = 0;
		cur_char = 0;

		// get requested file path
		if(argc < 2) {
			vid::print_strln("Nome file?");
			return 1;
		}
		file = argv[1];

		// try opening file 
		if(!edt::open_file()) return 2;

		// enter editor loop
		while(!edt::loop());

		// close file (syncing changes)
		if(!edt::close_file()) return 3;

		return 0;
	}
} // app::
