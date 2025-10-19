#include "lib/lib.h"

void main() {
	while(true) {
		char c = keyb::get_char();
		vid::print_char(c);
	}

	utl::wait();
	return;
}
