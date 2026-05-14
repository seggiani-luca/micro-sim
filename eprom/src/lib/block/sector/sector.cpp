#include "sector.h"
#include "../block.h"
#include "../../util/util.h"

namespace blk {
	namespace sec {
		int read_cmd = 0x00;
		int write_cmd = 0x01;

		void wait_for_disk() {
			while(*blk::disk.ctl_prt != 1);
		}

		void give_disk_command(int addr, int scn, int cmd) {
			// check for valid address
			if(addr < 0 || addr > num) {
				utl::panic("Indirizzo LBA errato");
			}

			// give command
			*blk::disk.lba_prt = addr;
			*blk::disk.scn_prt = scn;
			*blk::disk.ctl_prt = cmd;
		}

		void read(int addr, void* data) {
			// give command and wait
			give_disk_command(addr, 1, read_cmd);
			wait_for_disk();

			// read sector
			uint8_t* bdata = (uint8_t*) data;
			for(int i = 0; i < size; i += 2) {
				uint16_t dat = *blk::disk.buf_prt;
				bdata[i] = dat;
				bdata[i + 1] = dat >> 8;
			}
		}

		void write(int addr, void* data) {
			// give command and wait
			give_disk_command(addr, 1, write_cmd);
			wait_for_disk();

			// write sector
			uint8_t* bdata = (uint8_t*) data;
			for(int i = 0; i < size; i += 2) {
				uint16_t dat = (0xff & bdata[i]) | (bdata[i + 1] << 8);
				*blk::disk.buf_prt = dat;
			}
		}

		void zero(int addr) {
			// give command and wait
			give_disk_command(addr, 1, write_cmd);
			wait_for_disk();

			// zero sector
			for(int i = 0; i < size; i += 2) {
				*blk::disk.buf_prt = 0;
			}
		}
	}
}
