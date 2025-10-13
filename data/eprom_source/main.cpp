#include "lib/lib.h"

void main() {
	while(true) {
		vid::clear();
		
		vid::print_str("Dammi una stringa: ");
		char buf[40];
		keyb::read_str(buf, 40);

		vid::print_str("Ottenuta stringa: ");
		vid::print_str(buf);
		vid::newline();

		utl::wait();
	}

	return;
}
