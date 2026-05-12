#include "lib/lib.h"

void main() {
	uint16_t beg0 = blk::fat_chain(2000);
	vid::print_str("Allocata chain beg0: ");
	vid::print_int(beg0); vid::newline();

	uint16_t beg1 = blk::fat_chain(1000);
	vid::print_str("Allocata chain beg1: ");
	vid::print_int(beg1); vid::newline();
	
	blk::fat_unchain(beg0);
	vid::print_strln("Deallocata chain beg0");
	
	uint16_t beg2 = blk::fat_chain(4000);
	vid::print_str("Allocata chain beg2: ");
	vid::print_int(beg2); vid::newline();


	utl::wait();
}
