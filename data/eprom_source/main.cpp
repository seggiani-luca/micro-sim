#include "lib/lib.h"

void main() {
	for(int i = 0; i < 10; i++) {
		vid::print_str("stringa di prova ");
	}

	utl::wait();

	str::mmove((void*) (vid::video + 50), (void*) (vid::video + 25), 75);

	utl::wait();
	return;
}
