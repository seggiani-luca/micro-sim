#include "lib/lib.h"

void main() {
	blk::dir::make("bin");
	blk::dir::make("home");
	if(!blk::dir::change("home")) utl::panic("(6) Change a home fallito");
	blk::dir::make("docs");
	blk::dir::make("work");
	blk::dir::make("code");
	if(!blk::dir::change("..")) utl::panic("(10) Change a .. fallito");

	vid::print_strln("Listato di /:");
	blk::dir::list();
	vid::newline();
	
	vid::print_strln("Listato di /bin:");
	if(!blk::dir::change("bin")) utl::panic("(17) Change a bin fallito");
	blk::dir::list();
	vid::newline();

	vid::print_strln("Listato di /home:");
	if(!blk::dir::change("..")) utl::panic("(22) Change a .. fallito");
	if(!blk::dir::change("home")) utl::panic("(23) Change a home fallito");
	blk::dir::list();
	vid::newline();

	utl::wait();
}
