#include "../lib/lib.h"

/**
 * Text editor implementation.
 */
namespace edt {
	/**
	 * Size constants.
	 */
	#define FILE_BUF_SIZE 4096
	#define LINE_BUF_SIZE 256

	/**
	 * Status strings.
	 */
	#define COMMAND "comando"
	#define INSERT  "inserisci"

	/**
	 * Represents a piece of a line (fixed size element of a rope).
	 */
	#define PIECE_SIZE 16
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
		// get new piece
		piece* piece = pieces_free;
		if(piece == NULL) utl::panic("Memoria esaurita");

		// clean up piece
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
	 * Beginning of line window.
	 */
	int window_first;
	
	/**
	 * Size of line window.
	 */
	int window_size = vid::rows - 1;

	/**
	 * Current line.
	 */
	int cur_line;
		
	/**
	 * Decrements current line.
	 */
	void dec_cur_line() {
		if(cur_line > 0) cur_line--;
		if(cur_line - window_first < 0) window_first -= window_size;
	}

	/**
	 * Advances current line.
	 */
	void adv_cur_line() {
		if(cur_line < MAX_LINES - 1) cur_line++;
		if(cur_line - window_first >= window_size) window_first += window_size;
	}

	/**
	 * Converts a '\0' or '\n' terminated buffer into the pieces that make up a 
	 * line.
	 *
	 * @param buf buffer to convert
	 * @return first piece of the line
	 */
	piece* piece_line(char* buf) {
		// don't return anything for empty lines
		if(*buf == '\n' || *buf == '\0') return NULL;

		int j = 0; // index in piece

		// allocate first piece
		piece* p = alloc_piece();
		piece* first = p;

		// go through all characters in buffer
		while(true) {
			char c = *(buf++);
			
			// next line
			if(c == '\n' || c == '\0') break; 

			// append to piece
			p->buf[j++] = c;

			// piece full
			if(j == PIECE_SIZE) {
				piece* next = alloc_piece();
				j = 0;
				p->next = next;
				p = next;
			}
		}

		return first;
	}

	/**
	 * Clears a line (freeing all pieces).
	 *
	 * @param line first piece of the line to clear
	 */
	void clear_line(piece*& first) {
		piece* cur = first;

		// go through all pieces, freeing
		while (cur) {
			piece* next = cur->next;
			free_piece(cur);
			cur = next;
		}

		// also free first reference
		first = NULL;
	}

	/**
	 * Opens the current file.
	 *
	 * @return was the operation succesful?
	 */
	bool open_file() {
		// read file from disk 
		char fbuf[FILE_BUF_SIZE];
		int fsiz = blk::dir::read_file(file, fbuf, FILE_BUF_SIZE, blk::dir::cur);
		if(fsiz == -1) {
			vid::print_strln("Errore lettura file");
			return 0;
		}

		int i = 0; // index in buffer
		int j = 0; // current line
	
		// write file into line array
		while (i < fsiz && j < MAX_LINES) {
			lines[j++] = piece_line(&fbuf[i]);
			
			// advance to next line
			while (i < fsiz && fbuf[i] != '\n') i++;
			i++; // skip '\\n'
		}

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
	 * Prints a single line.
	 * 
	 * @param first first piece of line to print
	 */
	void print_line(piece* first) {
			// print line buffers
			do {
				for(int j = 0; j < PIECE_SIZE; j++) {
					char c = first->buf[j];
					if(c == '\0') break;
					vid::print_char(c);
				}
			} while((first = first->next));
	}
	
	/**
	 * Displays editor screen.
	 */
	void print_screen(const char* status) {
		// clear screen
		vid::clear();

		// print file lines
		int window_last = window_first + window_size;
		if(window_last > MAX_LINES) window_last = MAX_LINES;
		for(int i = window_first; i < window_last; i++) {
			// get line and print if valid
			piece* p = lines[i];
			if(p) print_line(p);
			
			// move to next line
			vid::newline();
		}

		// move cursor
		vid::set_cursor({cur_line - window_first, 0});

		// print status
		vid::put_str({vid::rows - 1, 0}, "linea: ");
		vid::put_uint({vid::rows - 1, 7}, cur_line);
		vid::put_str({vid::rows - 1, vid::cols - 10}, status);
	}
	
	/**
	 * Inserts on the current line.
	 */
	void insert() {
		// clear line
		clear_line(lines[cur_line]);

		// print screen
		print_screen(INSERT);

		// get next line
		char new_line[LINE_BUF_SIZE];
		kyb::read_str(new_line, LINE_BUF_SIZE);
		lines[cur_line] = piece_line(new_line);
	}

	/**
	 * Main editor loop.
	 *
	 * @return should the editor exit?
	 */
	bool loop() {
		// print screen
		print_screen(COMMAND);
		
		// get input
		char c = kyb::get_char();

		// edit
		switch(c) {
			case ESC: vid::clear(); return 1;
			case 'i': insert(); break;
			case 'w': dec_cur_line(); break;
			case 's': adv_cur_line(); break;
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
		for(int i = 0; i < MAX_LINES; i++) lines[i] = NULL;

		// initialize editor
		cur_line = 0;

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
