#include "hardware.h"

#define _64K 64 * 1024

namespace hwr {
	namespace mem {
		volatile uint8_t vram[_64K] __attribute__((section(".video")));	
		uint32_t vram_size = sizeof(vram);

		bool vram_reads_allowed = true;
	} // mem::

	namespace dev {
		video_device video = {
			.cur_row_reg = (volatile uint32_t*) 0x00030000,
			.cur_col_reg = (volatile uint32_t*) 0x00030004,
			.cols = 80,
			.rows = 30
		};

		keyboard_device keyboard = {
			.sts_reg = (volatile uint32_t*) 0x00040000,
			.buf_reg = (volatile uint32_t*) 0x00040004
		};

		timer_device timer = {
			.sts_reg = (volatile uint32_t*) 0x00050000
		};
	} // dev::
} // hwr::
