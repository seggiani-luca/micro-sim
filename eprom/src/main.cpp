#include "lib/lib.h"

void main() {
	blk::make_dir("bin");
	blk::make_dir("home");
	if(!blk::change_dir("home")) utl::panic("(6) Change a home fallito");
	blk::make_dir("docs");
	blk::make_dir("work");
	blk::make_dir("code");
	if(!blk::change_dir("..")) utl::panic("(10) Change a .. fallito");

	vid::print_strln("Listato di /:");
	blk::list_dir();
	vid::newline();
	
	vid::print_strln("Listato di /bin:");
	if(!blk::change_dir("bin")) utl::panic("(17) Change a bin fallito");
	blk::list_dir();
	vid::newline();

	vid::print_strln("Listato di /home:");
	if(!blk::change_dir("..")) utl::panic("(22) Change a .. fallito");
	if(!blk::change_dir("home")) utl::panic("(23) Change a home fallito");
	blk::list_dir();
	vid::newline();

	utl::wait();
}
