#include "lib/block/dir/dir.h"
#include "lib/lib.h"
#include "lib/video/video.h"
#include <cstddef>

#define SHELL_BUF_SIZE 512
#define SHELL_MAX_ARGS 16

/**
 * Enum for command types.
 */
enum cmd_type {
	LIST_DIR,
	CHANGE_DIR,
	MAKE_DIR,
	REMOVE_DIR,
	UNKNOWN
};

/**
 * Converts a command string to its command type.
 */
cmd_type get_type(const char* cmd) {
	if(!str::cmp(cmd, "ls")) return LIST_DIR;
	if(!str::cmp(cmd, "cd")) return CHANGE_DIR;
	if(!str::cmp(cmd, "md")) return MAKE_DIR;
	if(!str::cmp(cmd, "rd")) return REMOVE_DIR;

	return UNKNOWN;
}

/**
 * Gets arguments from the current str::tok buffer.
 */
void get_arguments(int* argc, char** argv) {
	*argc = 0;
	
	// go through arguments
	for(int i = 0; i < SHELL_MAX_ARGS; i++) {
		char* tok = str::tok(NULL);
		if(tok == NULL) return;	
		argv[(*argc)++] = tok; 
	}
}

int main() {
	char cmd[SHELL_BUF_SIZE];

	while(true) {
		// get command
		vid::print_str("$ ");
		kyb::read_str(cmd, SHELL_BUF_SIZE);

		// get command type
		char* tok = str::tok(cmd);
		cmd_type typ = get_type(tok);

		// get arguments
		int argc;
		char* argv[SHELL_MAX_ARGS];
		get_arguments(&argc, argv);		
	
		// execute command
		switch(typ) {
			case LIST_DIR: {
				blk::dir::list(); 
				break;
			}
			case CHANGE_DIR: { 
				if(argc < 1) {
					vid::print_strln("Nome directory?");
					break;
				}
				if(!blk::dir::change(argv[0]))
					vid::print_strln("Operazione fallita");
				break;
			}
			case MAKE_DIR: { 
				if(argc < 1) {
					vid::print_strln("Nome directory?");
					break;
				}
				if(!blk::dir::make(argv[0]))
					vid::print_strln("Operazione fallita");
				break;
			}
			case REMOVE_DIR: { 
				if(argc < 1) {
					vid::print_strln("Nome directory?");
					break;
				}
				if(!blk::dir::remove(argv[0]))
					vid::print_strln("Operazione fallita");
				break;
			}
			default: {
				vid::print_strln("Comando sconosciuto");
				break;
			}
		}
	}
}
