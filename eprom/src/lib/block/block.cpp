#include "block.h"
#include <cstdint>
#include "../util/util.h"
#include "../video/video.h"
#include "../string/string.h"

namespace blk {
	int storage_size = 16 * 1024 * 1024;
	
	int sector_size = 512;

	int num_sectors = storage_size / sector_size;

	int read_cmd = 0x00;

	int write_cmd = 0x01;

	void wait_for_disk() {
		while(*blk::disk.ctl_prt != 1);
	}

	void give_disk_command(int addr, int scn, int cmd) {
		if(addr < 0 || addr > num_sectors) {
			utl::panic("Indirizzo LBA errato");
		}

		*blk::disk.lba_prt = addr;
		*blk::disk.scn_prt = scn;
		*blk::disk.ctl_prt = cmd;
	}

	void read_sector(int addr, void* data) {
		// give command and wait
		give_disk_command(addr, 1, read_cmd);
		wait_for_disk();

		// read sector
		char* bdata = (char*) data;
		for(int i = 0; i < sector_size; i += 2) {
			uint16_t dat = *blk::disk.buf_prt;
			bdata[i] = dat;
			bdata[i + 1] = dat >> 8;
		}
	}

	void write_sector(int addr, void* data) {
		// give command and wait
		give_disk_command(addr, 1, write_cmd);
		wait_for_disk();

		// write sector
		char* bdata = (char*) data;
		for(int i = 0; i < sector_size; i += 2) {
			uint16_t dat = (0xff & bdata[i]) | (bdata[i + 1] << 8);
			*blk::disk.buf_prt = dat;
		}
	}

	void zero_sector(int addr) {
		// give command and wait
		give_disk_command(addr, 1, write_cmd);
		wait_for_disk();

		// zero sector
		for(int i = 0; i < sector_size; i += 2) {
			*blk::disk.buf_prt = 0;
		}
	}

	fat::vbr def_vbr = {
		.jump = {0},
		.oem_name = "micsim ",
		.param = {
			.log_sec_len = (uint16_t) sector_size,
			.cluster_len = 1,
			.reserved_secs = 1,
			.n_fats = 1,
			.root_dir_entries = 16,
			.n_log_secs = (uint16_t)num_sectors,
			.media_desc = 0xf8,
			.fat_len = 128,
			.phys_sec_per_track = 63,
			.n_heads = 255,
			.hidden_secs = 0,
			.large_secs = 0
		},
		.ex_param = {
			.phys_drive_num = 0,
			.reserved = 0,
			.ex_boot_sign = 0x29,
			.serial = 2025,
			.label = "micsim vol",
			.fs_type = "FAT16  "
		},
		.boot_code = {0},
		.magic = 0xaa55
	};

	void check_disk() {
		// load (supposed) boot sector from disk
		fat::vbr disk_vbr;
		read_sector(0, &disk_vbr);

		// check for magic
		if(disk_vbr.magic == def_vbr.magic) return;

		// not a valid fat disk, format
		vid::print_strln("Disco non formattato, formatto a FAT16...");

		// write boot sector
		write_sector(0, &def_vbr);

		// get fat bounds
		int fat_beg = def_vbr.param.reserved_secs;
		int fat_len = def_vbr.param.fat_len;

		// prepare first fat sector 
		uint16_t fat_sector[256];
		str::mset(fat_sector, 0, 512);
		fat_sector[0] = 0xfff8;
		fat_sector[1] = 0xffff;
		write_sector(fat_beg, fat_sector);

		// zero following sectors
		for(int i = 1; i < fat_len; i++) {
			zero_sector(fat_beg + i);
		}

		// notify formatting has finished
		vid::print_strln("Formattazione completata");
		utl::wait();
		vid::clear();
	}
} // blk::
