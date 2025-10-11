#include "lib/lib.h"

int sum(int a, int b) {
	return a + b;
}

void main() {
	utl::debugger();

	int a = 2;
	int b = 3;

	int c = sum(a, b);

	vid::print_char(c + '0');	
	vid::newline();

	utl::wait();
	return;
}
