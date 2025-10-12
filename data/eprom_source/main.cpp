#include "lib/lib.h"

void main() {
	using namespace vid;

	// draw table
	tab::draw(Coords(4, 2), Coords(74, 26), SYM_TABLE_DOTTED);

	tab::draw(Coords(4, 4), Coords(74, 26), SYM_TABLE_DOUBLE);
	for(int i = 1; i < 7; i++) {
		tab::horiz_line(Coords(4, 4 + i * 3), 70, SYM_TABLE_DOUBLE);
	}
	
	tab::vert_line(Coords(16, 2), 1, SYM_TABLE_DOTTED);
	tab::vert_line(Coords(32, 2), 1, SYM_TABLE_DOTTED);
	tab::vert_line(Coords(56, 2), 1, SYM_TABLE_DOTTED);

	tab::vert_line(Coords(16, 4), 22, SYM_TABLE_DOUBLE);
	tab::vert_line(Coords(32, 4), 22, SYM_TABLE_DOUBLE);
	tab::vert_line(Coords(56, 4), 22, SYM_TABLE_DOUBLE);

	put_str(Coords(5, 3), "Prova 1");
	put_str(Coords(17, 3), "Prova 2");
	put_str(Coords(33, 3), "Prova 3");
	put_str(Coords(57, 3), "Prova 4");

	set_cursor(Coords(0, ROWS - 1));
	utl::wait();
	return;
}
