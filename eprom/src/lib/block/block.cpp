#include "block.h"
#include <cstdint>
#include "../util/util.h"
#include "../video/video.h"
#include "../string/string.h"
#include "fat16.h"

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
		uint8_t* bdata = (uint8_t*) data;
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
		uint8_t* bdata = (uint8_t*) data;
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
		.oem_name = {'m', 'i', 'c', 'r', 'o','s', 'i', 'm'},
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
			.label = {'M', 'I', 'C', 'R', 'O','S', 'I', 'M', 'V', 'O', 'L'},
			.fs_type = {'F', 'A', 'T', '1', '6',' ', ' ', ' '}
		},
		.boot_code = {0},
		.magic = 0xaa55
	};

	void check_disk() {
		// load (supposed) boot sector from disk
		read_sector(0, &cur_vbr);

		// check for magic
		if(cur_vbr.magic == def_vbr.magic) return;

		// not a valid fat disk, format
		vid::print_strln("Disco non formattato, formatto a FAT16...");

		// write boot sector
		str::mcpy(&cur_vbr, &def_vbr, sizeof(fat::vbr));
		write_sector(0, &def_vbr);

		// get fat beginning
		int fat_beg = fat::get_fat(def_vbr, 0);

		// create fat (fill first sector) 
		uint16_t fat_sector[sector_size / 2];
		str::mset(fat_sector, 0, sector_size);
		fat_sector[0] = 0xfff8;
		fat_sector[1] = 0xffff;
		write_sector(fat_beg, fat_sector);

		// zero following sectors
		for(int i = 1; i < fat::get_fat_len(def_vbr); i++) {
			zero_sector(fat_beg + i);
		}
		
		// get fat beginning
		int rootdir_beg = fat::get_rootdir(def_vbr);

		// create rootdir (fill sector)
		fat::dir_ent rootdir[def_vbr.param.root_dir_entries];
		str::mset(rootdir, 0, sector_size);
		str::mcpy(rootdir[0].filename, def_vbr.ex_param.label, 11);
		rootdir[0].attrib = fat::vol_id_attrib;
		write_sector(rootdir_beg, rootdir);

		// zero following sectors
		for(int i = 1; i < fat::get_rootdir_len(def_vbr); i++) {
			zero_sector(rootdir_beg + i);
		}

		// notify formatting has finished
		vid::print_strln("Formattazione completata");
		utl::wait();
		vid::clear();
	}
	
	void read_cluster(int idx, void* data) {
		// get cluster address
		int addr = fat::get_cluster(cur_vbr, idx);

		// read all sectors of cluster
		uint8_t* bdata = (uint8_t*) data;	
		for(int i = 0; i < fat::get_cluster_secs(cur_vbr); i++) {
			uint8_t sector[sector_size];
			read_sector(addr + i, sector);
			str::mcpy(bdata + sector_size * i, sector, sector_size);
		}
	}

	void write_cluster(int idx, void* data) {
		// get cluster address
		int addr = fat::get_cluster(cur_vbr, idx);

		// write all sectors of cluster
		uint8_t* bdata = (uint8_t*) data;	
		for(int i = 0; i < fat::get_cluster_secs(cur_vbr); i++) {
			write_sector(addr + i, bdata + sector_size * i);
		}
	}

	uint16_t fat_lookup(uint16_t entry) {
		// get fat beginning
		int beg = fat::get_fat(cur_vbr, 0);

		// get sector from beginning entry is in, and entry in sector
		int sec_entries = sector_size / 2;
		int sec_addr = entry / sec_entries + beg;
		entry = entry % sec_entries;

		// read sector
		uint16_t sector[sec_entries];
		read_sector(sec_addr, sector);

		// read entry
		return sector[entry];	
	}
	
	uint16_t fat_allocate() {
		// scan fat table, get first unused entry and return
	}

	void fat_write(uint16_t entry, uint16_t val) {
		// lookup fat and write to it
	}

	void read_file(fat::dir_ent entry, void* buf) {
		// get filesize and first cluster
		int size = entry.filesize;
		uint16_t cluster_idx = entry.cluster_lo;
		int cluster_len = fat::get_cluster_len(cur_vbr);

		// cursor in file
		int i = 0;

		// copy all needed clusters
		uint8_t* bbuf = (uint8_t*) buf;
		while(size - i > 0) {
			// read cluster
			uint8_t cluster[cluster_len];
			read_cluster(cluster_idx, cluster);

			// copy cluster in remaining space
			int rem = size - i;
			if(rem >= cluster_len) rem = cluster_len;
			str::mcpy(bbuf + i, cluster, rem);
			i += rem;
		
			if(size - i > 0) {
				// get next cluster
				cluster_idx = fat_lookup(cluster_idx);
				if(fat::is_end_of_chain(cluster_idx) 
						|| cluster_idx == fat::free_cluster) {
					utl::panic("Trovato cluster invalido nel file");
				}
			}
		}
	}

	void trunc_file(fat::dir_ent entry, void* buf) {

	}

	void append_file(fat::dir_ent entry, void* buf) {

	}

} // blk::
