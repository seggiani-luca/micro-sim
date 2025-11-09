#ifndef TIME_H
#define TIME_H

#include <stdint.h>
#include "../conf/hardware.h"

namespace tim {
	/*
	 * Reference to timer device.
	 */
	inline hwr::dev::timer_device& timer = hwr::dev::timer;
	
	/*
	 * Waits for the given amount of timer ticks.
	 */
	void wait_ticks(int ticks);
}

#endif
