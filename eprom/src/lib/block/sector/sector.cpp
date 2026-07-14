#include "sector.h"
#include "../block.h"
#include "../../util/util.h"

namespace blk {
	namespace sec {
		int read_cmd = 0x20;
		int write_cmd = 0x30;
		int err_bit = 0x01;
		int drq_bit = 0x08;
		int bsy_bit = 0x80;

		void wait_for_disk() {
			while((*blk::disk.ctl_prt & (drq_bit | bsy_bit)) != drq_bit);
		}

		/**
		 * LBA constant.
		 */
		#define LBA 0xe

		void give_disk_command(uint32_t addr, int scn, int cmd) {
			// check for valid address
			if(addr < 0 || addr >= num) {
				utl::panic("Indirizzo LBA errato");
			}

			// give command
			*blk::disk.lba_prt = (LBA << 28) | (addr & 0x0FFFFFFF);
			*blk::disk.scn_prt = scn;
			*blk::disk.ctl_prt = cmd;

			// check for error
			if(*blk::disk.ctl_prt & err_bit) {
				utl::panic("Errore disco");
			}
		}

		void read(uint32_t addr, void* data) {
			// give command and wait
			give_disk_command(addr, 1, read_cmd);
			wait_for_disk();

			// read sector
			uint16_t* bdata = (uint16_t*) data;
			for(int i = 0; i < size / 2; i++) {
				bdata[i] = *blk::disk.buf_prt;
			}
		}

		void write(uint32_t addr, const void* data) {
			// give command and wait
			give_disk_command(addr, 1, write_cmd);
			wait_for_disk();

			// write sector
			uint16_t* bdata = (uint16_t*) data;
			for(int i = 0; i < size / 2; i++) {
				*blk::disk.buf_prt = bdata[i];
			}
		}

		void zero(uint32_t addr) {
			// give command and wait
			give_disk_command(addr, 1, write_cmd);
			wait_for_disk();

			// zero sector
			for(int i = 0; i < size / 2; i++) {
				*blk::disk.buf_prt = 0;
			}
		}
	}
}
