#include "time.h"
#include "../conf/hardware.h"

// reference timer device
hwr::dev::timer_device& timer = hwr::dev::timer;

namespace tim {
	void wait_ticks(int ticks) {
		while(ticks > 0) {
			if((*timer.sts_reg)) {
				ticks--;
			}
		}
	}
}
