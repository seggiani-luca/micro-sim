#include "lib/lib.h"

void main() {
	blk::make_dir("Dir0");
	blk::make_dir("Dir1");
	blk::make_dir("Dir2");
	blk::list_dir();

	utl::wait();
}
