#ifndef TIME_H
#define TIME_H

#include <stdint.h>

namespace tim {
	/*
	 * Waits for the given amount of timer ticks.
	 */
	void wait_ticks(int);
}

#endif
