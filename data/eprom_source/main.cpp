#include "lib/lib.h"

extern "C" void main() {
	while(true) {
		char c = keyb::get_char();
		vid::print_char(c);
	}

	return;
}
