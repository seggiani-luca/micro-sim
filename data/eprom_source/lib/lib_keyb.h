#ifndef LIB_KEYB_H
#define LIB_KEYB_H

#include <cstdint>

namespace keyb {
	volatile uint32_t*	keyb_status = (volatile uint32_t*) 0x00040000;
	volatile uint32_t*	keyb_data = (volatile uint32_t*) 0x00040001;

	char get_char() {
		while(*keyb_status != 1); // busy wait
		return *keyb_data;
	}
}

#endif
