#ifndef LIB_TIME_H
#define LIB_TIME_H

#include "lib_video.h"
#include <stdint.h>

namespace time {
	/*
	 * Timer status register.
	 */
	inline volatile uint32_t*	timer_status = (volatile uint32_t*) 0x00050000;

	/*
	 * Waits for the given amount of timer ticks. Timer tick rate is 1 KHz.
	 */
	void wait_ticks(int ticks) {
		while(ticks > 0) {
			if((*timer_status)) {
				ticks--;
			}
		}
	}
}

#endif
