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
			.row_prt = (volatile uint32_t*) 0x00030000,
			.col_prt = (volatile uint32_t*) 0x00030004,
			.cols = 80,
			.rows = 30
		};

		keyboard_device keyboard = {
			.sts_prt = (volatile uint32_t*) 0x00040000,
			.dat_prt = (volatile uint32_t*) 0x00040004
		};

		timer_device timer = {
			.gat0_prt = (volatile uint32_t*) 0x00050000,
			.gat1_prt = (volatile uint32_t*) 0x00050004,
			.gat2_prt = (volatile uint32_t*) 0x00050008,
			.con0_prt = (volatile uint32_t*) 0x0005000c,
			.con1_prt = (volatile uint32_t*) 0x00050010,
			.con2_prt = (volatile uint32_t*) 0x00050014
		};

		network_device network = {
			.txb_prt = (volatile uint32_t*) 0x00060000,
			.txr_prt = (volatile uint32_t*) 0x00060004,
			.rxb_prt = (volatile uint32_t*) 0x00060008,
			.rxr_prt = (volatile uint32_t*) 0x0006000c,
			.addr = *((volatile uint32_t*) 0x00060010)
		};
	} // dev::
} // hwr::
