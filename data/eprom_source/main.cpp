#include "lib/lib.h"

void main() {
	vid::graph::ocirc(vid::Coords(10, 10), 8, vid::SYM_HEART);
	
	vid::set_cursor(vid::Coords(0, ROWS - 1));
	utl::wait();
	return;
}
