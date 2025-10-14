#include "lib/lib.h"

void main() {
	int i = 0;
	while(true) {
		vid::clear();
		
		i++;
		if(i >= vid::ROWS) {
			i = 0;
		}

		vid::put_char(vid::Coords(i, i), vid::SYM_HEART);
		time::wait_ticks(50);
	}

	utl::wait();
	return;
}
