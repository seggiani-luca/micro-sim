#include "time.h"

namespace tim {
	void sleep(int millis) {
		// setup timer
		millis &= 0x7fffffff;
		*timer.con0_prt = millis;

		// wait for tick
		while(*timer.gat0_prt != 1); // busy wait
		
		// return
	}
} // tim::
