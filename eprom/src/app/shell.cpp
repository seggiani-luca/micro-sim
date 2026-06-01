#include "../lib/lib.h"
#include "../app/app.h"

#define VER "0.0"

#define SHELL_BUF_SIZE 512
#define SHELL_MAX_ARGS 16
#define SHELL_FIL_BUF_SIZE 2048

/**
 * Implements a simple shell with built-in functions and application execution
 * (via the global application table).
 */
namespace shl {
	/**
	 * Greets the user.
	 */
	void greet() {
		vid::print_str("micro-sim shell v");
		vid::print_strln(VER);
		vid::put_str({vid::cur.row - 1, 60}, "2026 - Luca Seggiani");
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
		/**
		 * Lists contents of current directory.
		 *
		 * @param argc argument count
		 * @param argv argument array (expected "ls")
		 * @return status code
		 */
		int list_dir(int argc, char* argv[]) {
			blk::dir::list(blk::dir::cur);
			return 0;
		}

		/**
		 * Changes current directory to other directory found in current one.
		 *
		 * @param argc argument count
		 * @param argv argument array (expected "cd <name>")
		 * @return status code
		 */
		int change_dir(int argc, char* argv[]) {
			if(argc < 2) {
				vid::print_strln("Nome directory?");
				return 0;
			}

			// find target directory
			blk::fat::dir_ent ent;
			if(!blk::dir::find(argv[1], blk::dir::cur, ent)) {
				vid::print_strln("Directory non trovata");
				return 0;
			}

			// check if directory
			if(!blk::fat::is_dir(ent)) {
				vid::print_strln("L'entrata trovata non e' una directory");
				return 0;
			}


			// change to directory
			blk::dir::cur = ent.cluster_lo;
			return 0;
		}

		/**
		 * Makes a new directory in the current one.
		 *
		 * @param argc argument count
		 * @param argv argument array (expected "md <name>")
		 * @return status code
		 */
		int make_dir(int argc, char* argv[]) {
			if(argc < 2) {
				vid::print_strln("Nome directory?");
				return 0;
			}
			if(!blk::dir::make(argv[1], blk::dir::cur)) {
				vid::print_strln("Operazione fallita");
			}

			return 0;
		}

		/**
		 * Deletes a directory found in the current one.
		 *
		 * @param argc argument count
		 * @param argv argument array (expected "rd <name>")
		 * @return status code
		 */
		int remove_dir(int argc, char* argv[]) {
			if(argc < 2) {
				vid::print_strln("Nome directory?");
				return 0;
			}
			if(!blk::dir::remove(argv[1], blk::dir::cur)) {
				vid::print_strln("Operazione fallita");
			}

			return 0;
		}

		/**
		 * Creates an empty file in the current directory.
		 *
		 * @param argc argument count
		 * @param argv argument array (expected "cf <name>")
		 * @return status code
		 */
		int create_file(int argc, char* argv[]) {
			if(argc < 2) {
				vid::print_strln("Nome file?");
				return 0;
			}
			if(!blk::dir::create_file(argv[1], 0, 0, blk::dir::cur)) {
				vid::print_strln("Operazione fallita");
			}

			return 0;
		}
		
		/**
		 * Reads a file found in the current directory.
		 *
		 * @param argc argument count
		 * @param argv argument array (expected "rf <name>")
		 * @return status code
		 */
		int read_file(int argc, char* argv[]) {
			if(argc < 2) {
				vid::print_strln("Nome file?");
				return 0;
			}
			
			// allocate buffer
			char buf[SHELL_FIL_BUF_SIZE];
			int siz = blk::dir::read_file(argv[1], buf, sizeof(buf), 
					blk::dir::cur);
			if(siz == -1) {
				vid::print_strln("Operazione fallita");
				return 0;
			}
			buf[siz] = '\0';

			// read file
			vid::print_str(buf);

			return 0;
		}

		/**
		 * Deletes a file found in the current directory.
		 *
		 * @param argc argument count
		 * @param argv argument array (expected "df <name>")
		 * @return status code
		 */
		int delete_file(int argc, char* argv[]) {
			if(argc < 2) {
				vid::print_strln("Nome file?");
				return 0;
			}
			if(!blk::dir::delete_file(argv[1], blk::dir::cur)) {
				vid::print_strln("Operazione fallita");
			}

			return 0;
		}

		/**
		 * Cleanly shuts down the system. 
		 *
		 * @param argc argument count
		 * @param argv argument array (expected "shutdown")
		 * @return status code
		 */
		int shutdown(int argc, char* argv[]) {
			vid::print_strln("Arrivederci!");
			tim::sleep(1000);
			utl::halt();
		}
		
		/**
		 * Provides help with the shell. 
		 *
		 * @param argc argument count
		 * @param argv argument array (expected "help")
		 * @return status code
		 */
		int help(int argc, char* argv[]) {
			// list built-in functions
			vid::print_strln("Funzioni built-in disponibili:");
			vid::print_strln("- ls: mostra directory corrente");
			vid::print_strln("- cd: sposta su una directory");
			vid::print_strln("- md: crea una nuova directory");
			vid::print_strln("- rd: rimuovi una directory");
			vid::print_strln("- cf: crea un nuovo file vuoto");
			vid::print_strln("- rf: mostra un file");
			vid::print_strln("- df: rimuovi un file");
			vid::print_strln("- shutdown: spegne il sistema");
			vid::print_strln("- help: mostra questo messaggio");
			vid::print_strln("- clear: ripulisce lo schermo");
			vid::newline();

			// list global application table
			vid::print_strln("Applicazioni disponibili:");
			for(int i = 0; i < APP_TABLE_SIZE; i++) {
				app::entry& entry = app::table[i];
				vid::print_str("- ");
				vid::print_str(entry.name);
				vid::print_str(": ");
				vid::print_strln(entry.desc);
			}
			vid::newline();

			return 0;
		}
		
		/**
		 * Clears the screen 
		 *
		 * @param argc argument count
		 * @param argv argument array (expected "clear")
		 * @return status code
		 */
		int clear(int argc, char* argv[]) {
			vid::clear();
			return 0;
		}
	}


	/**
	 * Tries executing the requested program, looking into the built-in shell
	 * functions and the global applications header.
	 *
	 * @param argc argument count
	 * @param argv argument array
	 * @return int status code. special values are:
	 *   - -1: invalid arguments
	 *   - -2: no such program
	 */
	int exec(int argc, char** argv) {
		// get command name
		if(argc < 0) return -1;
		char* cmd = argv[0];

		// look into built-in shell functions
		if(!str::cmp(cmd, "ls"))       return blt::list_dir(argc, argv);
		if(!str::cmp(cmd, "cd"))       return blt::change_dir(argc, argv);
		if(!str::cmp(cmd, "md"))       return blt::make_dir(argc, argv);
		if(!str::cmp(cmd, "rd"))       return blt::remove_dir(argc, argv);
		if(!str::cmp(cmd, "cf"))       return blt::create_file(argc, argv);
		if(!str::cmp(cmd, "rf"))       return blt::read_file(argc, argv);
		if(!str::cmp(cmd, "df"))       return blt::delete_file(argc, argv);
		if(!str::cmp(cmd, "shutdown")) return blt::shutdown(argc, argv);
		if(!str::cmp(cmd, "help"))     return blt::help(argc, argv);
		if(!str::cmp(cmd, "clear"))    return blt::clear(argc, argv);
	
		// look into global application table
		for(int i = 0; i < APP_TABLE_SIZE; i++) {
			app::entry& entry = app::table[i];
			if(!str::cmp(cmd, entry.name)) return entry.ent(argc, argv);
		}

		return -2;
	}
} // shl::

using namespace shl;
namespace app {
	ENTRY(shell) {
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
	
			// execute command
			ret = exec(argc, argv);

			// show return code
			switch(ret) {
				case -1: break;
				case -2: vid::print_str("Comando non trovato: ");
				         vid::print_str(argv[0]);
				         vid::newline();
				         break;
				case 0: break;
				default: vid::print_str("Programma uscito con codice ");
				         vid::print_int(ret);
				         vid::newline();
			}
		}
	}
} // app::
