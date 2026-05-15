#include "lib/block/dir/dir.h"
#include "lib/lib.h"

#define VER "0.0"

#define SHELL_BUF_SIZE 512
#define SHELL_MAX_ARGS 16
#define SHELL_FIL_BUF_SIZE 2048

/**
 * Namespace for shell functions.
 */
namespace shl {
	/**
	 * Greets the user.
	 */
	void greet() {
		vid::print_str("Shell micro-sim ");
		vid::print_str(VER);
		vid::print_strln(" - caricati 64 KB di RAM");
	}

	/**
	 * Enum for command types.
	 */
	enum cmd_type {
		LIST_DIR,
		CHANGE_DIR,
		MAKE_DIR,
		REMOVE_DIR,
		CREATE_FIL,
		READ_FIL,
		DELETE_FIL,
		SHUTDOWN,
		UNKNOWN
	};

	/**
	 * Converts a command string to its command type.
	 */
	cmd_type get_type(const char* cmd) {
		if(!str::cmp(cmd, "ls"))       return LIST_DIR;
		if(!str::cmp(cmd, "cd"))       return CHANGE_DIR;
		if(!str::cmp(cmd, "md"))       return MAKE_DIR;
		if(!str::cmp(cmd, "rd"))       return REMOVE_DIR;
		if(!str::cmp(cmd, "cf"))       return CREATE_FIL;
		if(!str::cmp(cmd, "rf"))       return READ_FIL;
		if(!str::cmp(cmd, "df"))       return DELETE_FIL;
		if(!str::cmp(cmd, "shutdown")) return SHUTDOWN;

		return UNKNOWN;
	}

	/**
	 * Gets arguments from a command.
	 *
	 * @param cmd command buffer
	 * @param argc argument count
	 * @param argv argument values
	 */
	void get_arguments(char* cmd, int* argc, char** argv) {
		// init argument count
		*argc = 0;
		
		// get first argument
		char* tok = str::tok(cmd);
		if(tok == NULL) return;
		argv[(*argc)++] = tok; 

		// go through following arguments
		for(int i = 0; i < SHELL_MAX_ARGS; i++) {
			char* tok = str::tok(NULL);
			if(tok == NULL) return;	
			argv[(*argc)++] = tok; 
		}
	}

	/**
	 * Namespace for built-in shell functions.
	 */
	namespace blt {
		int list_dir(int argc, char* argv[]) {
			blk::dir::list(blk::dir::cur_dir);
			return 0;
		}

		int change_dir(int argc, char* argv[]) {
			if(argc < 2) {
				vid::print_strln("Nome directory?");
				return 0;
			}

			// find target directory
			blk::fat::dir_ent ent;
			if(!blk::dir::find(argv[1], blk::dir::cur_dir, ent)) {
				vid::print_strln("Directory non trovata");
				return 0;
			}

			// check if directory
			if(!blk::fat::is_dir(ent)) {
				vid::print_strln("L'entrata trovata non e' una directory");
				return 0;
			}


			// change to directory
			blk::dir::cur_dir = ent.cluster_lo;
			return 0;
		}

		int make_dir(int argc, char* argv[]) {
			if(argc < 2) {
				vid::print_strln("Nome directory?");
				return 0;
			}
			if(!blk::dir::make(argv[1], blk::dir::cur_dir)) {
				vid::print_strln("Operazione fallita");
			}

			return 0;
		}

		int remove_dir(int argc, char* argv[]) {
			if(argc < 2) {
				vid::print_strln("Nome directory?");
				return 0;
			}
			if(!blk::dir::remove(argv[1], blk::dir::cur_dir)) {
				vid::print_strln("Operazione fallita");
			}

			return 0;
		}

		int create_file(int argc, char* argv[]) {
			if(argc < 2) {
				vid::print_strln("Nome file?");
				return 0;
			}
			if(!blk::dir::create_file(argv[1], 0, 0, blk::dir::cur_dir)) {
				vid::print_strln("Operazione fallita");
			}

			return 0;
		}
		
		int read_file(int argc, char* argv[]) {
			if(argc < 2) {
				vid::print_strln("Nome file?");
				return 0;
			}
			
			// allocate buffer
			char buf[SHELL_FIL_BUF_SIZE];

			if(!blk::dir::read_file(argv[1], buf, sizeof(buf), 
						blk::dir::cur_dir)) {
				vid::print_strln("Operazione fallita");
				return 0;
			}

			// read file
			vid::print_strln(buf);

			return 0;
		}

		int delete_file(int argc, char* argv[]) {
			if(argc < 2) {
				vid::print_strln("Nome file?");
				return 0;
			}
			if(!blk::dir::delete_file(argv[1], blk::dir::cur_dir)) {
				vid::print_strln("Operazione fallita");
			}

			return 0;
		}

		int shutdown(int argc, char* argv[]) {
			utl::halt();
		}
	}
}

using namespace shl;

int main() {
	greet();

	// init command buffer
	char cmd[SHELL_BUF_SIZE];
	int ret = 0;

	while(true) {
		// get command
		vid::print_str("$ ");
		kyb::read_str(cmd, SHELL_BUF_SIZE);

		// get arguments
		int argc;
		char* argv[SHELL_MAX_ARGS];
		get_arguments(cmd, &argc, argv);
		if(argc == 0) continue;

		// get command type
		cmd_type typ = get_type(argv[0]);
	
		// execute command
		switch(typ) {
			case LIST_DIR:   ret = blt::list_dir(argc, argv);    break;
			case CHANGE_DIR: ret = blt::change_dir(argc, argv);  break;
			case MAKE_DIR:   ret = blt::make_dir(argc, argv);    break;
			case REMOVE_DIR: ret = blt::remove_dir(argc, argv);  break;
			case CREATE_FIL: ret = blt::create_file(argc, argv); break;
			case READ_FIL:   ret = blt::read_file(argc, argv);   break;
			case DELETE_FIL: ret = blt::delete_file(argc, argv);   break;
			case SHUTDOWN:   ret = blt::shutdown(argc, argv);    break;
			default: {
				vid::print_strln("Comando sconosciuto");
				ret = 0;
				break;
			}
		}

		// show return code
		if(ret != 0) {
			vid::print_strln("Programma uscito con codice ");
			vid::print_int(ret);
			vid::newline();
		}
	}
}
