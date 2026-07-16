#include "../lib/lib.h"

#define VER "0.0"

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
	#define COMMAND "COMANDO"
	#define REPLACE "RIMPIAZZA"
	#define INSERT  "INSERISCI"
	#define HELP    "AIUTO"

	/**
	 * Represents a piece of a line (fixed size element of a rope).
	 */
	#define PIECE_SIZE 32
	struct piece {
		/**
		 * Buffer of piece.
		 */
		char buf[PIECE_SIZE];

		/**
		 * Pointer to next piece in rope.
		 */
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
	 * Counter of used pieces.
	 */
	int used_pieces;

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

		// keep track
		used_pieces++;

		return piece;
	}

	/**
	 * Frees an allocated piece.
	 *
	 * @param piece the piece to free
	 */
	void free_piece(piece* piece) {
		// insert into free list
		piece->next = pieces_free;
		pieces_free = piece;
		
		// keep track
		used_pieces--;
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
	 * Should screen be redrawn? 
	 */
	bool dirty;	
	
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
	void dec_line() {
		if(cur_line > 0) cur_line--;
		if(cur_line - window_first < 0) {
			window_first -= window_size;
			dirty = true;
		}
	}

	/**
	 * Advances current line.
	 */
	void adv_line() {
		if(cur_line < MAX_LINES - 1) cur_line++;
		if(cur_line - window_first >= window_size) {
			window_first += window_size;
			dirty = true;
		}
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

		// also make first reference NULL
		first = NULL;
	}

	/**
	 * Opens the current file.
	 *
	 * @return was the operation succesful?
	 */
	bool open_file() {
		// show intent
		vid::print_str("Leggo file ");
		vid::print_strln(file);
		tim::sleep(500);

		// initialize file buffer 
		char fbuf[FILE_BUF_SIZE];

		// read file from disk
		int fsiz = blk::dir::read_file(file, fbuf, FILE_BUF_SIZE, 
				blk::dir::cur);
		if(fsiz == -1) {
			vid::print_strln("Errore lettura file");
			return 0;
		}

		int i = 0; // index in buffer
		int j = 0; // current line
	
		// write file into line array
		while (i < fsiz && j < MAX_LINES) {
			// get line
			lines[j++] = piece_line(&fbuf[i]);
			
			// advance to next line
			while (i < fsiz && fbuf[i] != '\n') i++;
			i++; // skip '\\n'
		}

		return 1;
	}

	/**
	 * Writes a line to a buffer.
	 *
	 * @param line first piece of the line to write
	 * @param buf buffer to fill
	 * @param size of buffer to fill
	 * @return characters written
	 */
	int write_line(piece* first, char* buf, int siz) {
		int i = 0; // current index

		// print line buffers
		do {
			for(int j = 0; j < PIECE_SIZE; j++) {
				char c = first->buf[j];
				if(c == '\0') break;
				buf[i++] = c;
				
				// don't overflow
				if(i >= siz) break;
			}
		} while((first = first->next));

		return i;
	}

	/**
	 * Closes current file.
	 *
	 * @return was the operation succesful?
	 */
	bool close_file() {
		// show intent
		vid::print_str("Scrivo file ");
		vid::print_strln(file);
		tim::sleep(500);
		
		// initialize file buffer
		char fbuf[FILE_BUF_SIZE];

		int fsiz = 0; // index in file
		int prev = 0; // previous lines

		// write line array into file
		for(int i = 0; i < MAX_LINES; i++) {
			piece* p = lines[i];

			// if empty, increase empty lines
			if(!p) {
				prev++;
				continue;
			}

			// if not empty, print empty lines
			while(prev > 0) {
				fbuf[fsiz++] = '\n';
				if(fsiz >= FILE_BUF_SIZE) return 0;
				prev--;
			}

			// and write actual line
			fsiz += write_line(lines[i], fbuf + fsiz, FILE_BUF_SIZE - fsiz);
			fbuf[fsiz++] = '\n';
			if(fsiz >= FILE_BUF_SIZE) return 0;
		}

		// write file to disk
		blk::dir::update_file(file, fbuf, fsiz, blk::dir::cur);

		return 1;
	}

	/**
	 * Prints a single line.
	 * 
	 * @param first first piece of line to print
	 */
	void print_line(piece* first) {
		int cur = 0; // current index

		// print line buffers
		do {
			for(int j = 0; j < PIECE_SIZE; j++) {
				char c = first->buf[j];
				if(c == '\0') break;
				vid::print_char(c);

				// don't fill more than one line
				cur++;
				if(cur >= vid::cols - 1) break;
			}
		} while((first = first->next));
	}

	/**
	 * Prints the status screen and other information.
	 */
	void print_status(const char* status) {
		// last row
		int lrow = vid::rows - 1;

		// clear rast row
		mem::set((void*)(vid::vram + vid::cols * lrow), 0, vid::cols);

		// current line
		vid::put_str({lrow, 0}, "Linea: ");
		vid::put_uint({lrow, 7}, cur_line);

		// free pieces
		vid::put_str({lrow, 20}, "Blocchi usati: ");
		vid::put_uint({lrow, 35}, used_pieces);
		vid::put_str({lrow, 45}, "/");
		vid::put_uint({lrow, 46}, MAX_PIECES);

		// current status
		vid::put_str({lrow, vid::cols - 10}, status);
	}

	/**
	 * Displays editor screen.
	 */
	void print_screen(const char* status) {
		// redraw only when needed
		if(dirty) {
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
		}
		dirty = false;

		// move cursor
		vid::set_cursor({cur_line - window_first, 0});

		// print status
		print_status(status);
	}
	
	/**
	 * Inserts on the current line.
	 */
	void replace() {
		// clear line
		clear_line(lines[cur_line]);

		// print screen
		dirty = true;
		print_screen(REPLACE);

		// get next line
		char new_line[LINE_BUF_SIZE];
		kyb::read_str(new_line, LINE_BUF_SIZE);
		lines[cur_line] = piece_line(new_line);
	}

	/**
	 * Shifts all lines forwards and inserts on the current line. 
	 */
	void insert() {
		// move lines from current forward 
		for(int i = MAX_LINES - 1; i >= cur_line + 1; i--) {
			// free last line if needed
			if(i == MAX_LINES - 1 && lines[i] != NULL) 
				clear_line(lines[i]); 
			lines[i] = lines[i - 1];
		}

		// empty line (don't clear, referenced by next)
		lines[cur_line] = NULL;

		// print screen
		dirty = true;
		print_screen(INSERT);

		// get next line
		char new_line[LINE_BUF_SIZE];
		kyb::read_str(new_line, LINE_BUF_SIZE);
		lines[cur_line] = piece_line(new_line);	
	}

	/**
	 * Removes the current line.
	 */
	void del() {
		// clear line
		clear_line(lines[cur_line]);

		// move lines from current backward
		for(int i = cur_line; i < MAX_LINES - 1; i++) {
			lines[i] = lines[i + 1];
		}
		lines[MAX_LINES - 1] = NULL;
		
		dirty = true;
	}

	/**
	 * Shows help.
	 */
	void help() {
		vid::clear();

		// list modes 
		vid::print_strln("Modalita' editor:");
		vid::print_str("- ");
		vid::print_str(COMMAND);
		vid::print_strln(": permette di inserire comandi (ESC esce)");
		vid::print_str("- ");
		vid::print_str(REPLACE);
		vid::print_strln(": rimpiazza la linea attuale (INVIO esce)");
		vid::print_str("- ");
		vid::print_str(INSERT);
		vid::print_strln(": inserisce prima della linea attuale (INVIO esce)");
		vid::print_str(HELP);
		vid::print_strln(": questa schermata");
		vid::newline();

		// list commands
		vid::print_str("Comandi disponibili in modalita'");
		vid::print_str(COMMAND);
		vid::print_strln(":");
		vid::print_strln("- w: va alla linea precedente");
		vid::print_strln("- s: va alla prossima linea");
		vid::print_str("- r: entra in modalita' ");
		vid::print_strln(REPLACE);
		vid::print_str("- i: entra in modalita' ");
		vid::print_strln(INSERT);
		vid::print_strln("- d: rimuove la linea attuale");
		vid::print_strln("- h: mostra questa schermata");
		vid::newline();
		
		// print status
		print_status(HELP);

		utl::wait();
		
		dirty = true;
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
			case 'r': replace(); break;
			case 'i': insert(); break;
			case 'd': del(); break;
			case 'h': help(); break;
			case 'w': dec_line(); break;
			case 's': adv_line(); break;
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
		window_first = 0;
		dirty = true; // force first draw
		cur_line = 0;
		used_pieces = 0;

		// get requested file path
		if(argc < 2) {
			vid::print_strln("Nome file?");
			return 1;
		}
		file = argv[1];

		// try opening file 
		if(!open_file()) return 2;

		// enter editor loop
		while(!loop());

		// close file (syncing changes)
		if(!close_file()) return 3;

		return 0;
	}
} // app::
