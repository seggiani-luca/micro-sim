#include "time.h"

namespace tim {
	void wait_ticks(int ticks) {
		while(ticks > 0) {
			if((*timer.sts_reg)) {
				ticks--;
			}
		}
	}
}
