#ifndef TIME_H
#define TIME_H

#include <stdint.h>
#include "../conf/hardware.h"

/**
 * Namespace for time-related functions, and timer handling.
 */
namespace tim {
	/**
	 * Reference to timer device.
	 */
	inline hwr::dev::timer_device& timer = hwr::dev::timer;

	/**
	 * Sleeps for the specified amount of time.
	 *
	 * @param millis time to sleep for (in milliseconds)
	 */
	void sleep(int millis);
} // tim::

#endif
