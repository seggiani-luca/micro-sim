#include "lib/lib.h"

void main() {
	vid::print_str("sizeof(vbr): ");
	vid::print_int(sizeof(blk::fat::vbr)); vid::newline();
	vid::print_str("sizeof(dir): ");
	vid::print_int(sizeof(blk::fat::dir)); vid::newline();
	utl::wait();
}
