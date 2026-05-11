#include "lib/lib.h"

void main() {
	blk::fat::vbr bs;
	blk::read_sector(0, &bs);

	vid::print_strln(bs.oem_name);

	utl::wait();
}
