#include "lib/lib.h"
#include "lib/util/util.h"

#define VER "0.0"

#define SHELL_BUF_SIZE 512
#define SHELL_MAX_ARGS 16

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

int main() {
	greet();

	// init command buffer
	char cmd[SHELL_BUF_SIZE];

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
			case LIST_DIR: {
				blk::dir::list(); 
				break;
			}
			case CHANGE_DIR: { 
				if(argc < 1) {
					vid::print_strln("Nome directory?");
					break;
				}
				if(!blk::dir::change(argv[1]))
					vid::print_strln("Operazione fallita");
				break;
			}
			case MAKE_DIR: { 
				if(argc < 1) {
					vid::print_strln("Nome directory?");
					break;
				}
				if(!blk::dir::make(argv[1]))
					vid::print_strln("Operazione fallita");
				break;
			}
			case REMOVE_DIR: { 
				if(argc < 1) {
					vid::print_strln("Nome directory?");
					break;
				}
				if(!blk::dir::remove(argv[1]))
					vid::print_strln("Operazione fallita");
				break;
			}
			case SHUTDOWN: {
				vid::print_strln("Arrivederci!");
				tim::sleep(1000);
				utl::halt();
			}
			default: {
				vid::print_strln("Comando sconosciuto");
				break;
			}
		}
	}
}
