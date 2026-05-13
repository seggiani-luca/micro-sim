#include "lib/block/block.h"
#include "lib/lib.h"

void main() {
	vid::print_strln("Listato della directory di root:");
	blk::make_dir("Dir0");
	blk::make_dir("Dir1");
	blk::make_dir("Dir2");
	blk::list_dir();

	utl::wait();
	vid::newline();

	vid::print_strln("Listato della directory Dir0:");
	blk::change_dir("Dir0");
	blk::change_dir(".");
	blk::make_dir("Subdir1");
	blk::make_dir("Subdir2");
	blk::list_dir();
	
	utl::wait();
	vid::newline();
	
	vid::print_strln("Listato della directory di root:");
	blk::change_dir("..");
	blk::list_dir();

	utl::wait();
}
