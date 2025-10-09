#include "lib/lib.h"

char* mess = "Ciao RISC-V!";

extern "C" void main() {
	for(int i = 0; i < 50; i++) {
		vid::print_char(i + '0');
		vid::newline();
	}
	
	utl::spin();

	return;
}
