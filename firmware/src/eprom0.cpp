#include "lib/lib.h"

void main() {
	while(true) {
		char c = kyb::get_char();
		vid::print_char(c);
	}

	utl::wait();
	return;
}
