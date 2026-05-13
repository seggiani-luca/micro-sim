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

	fat::vbr cur_vbr;

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
	
	void read_cluster(int idx, void* data, int size) {
		// get cluster address
		int addr = fat::get_cluster(cur_vbr, idx);
		
		// read clusters until size reached 
		uint8_t* bdata = (uint8_t*) data;	
		for(int i = 0; i < fat::get_cluster_secs(cur_vbr); i++) {
			if(size >= sector_size) {
				// read full sector
				read_sector(addr + i, bdata);
				bdata += sector_size;
				size -= sector_size;
			} else {
				// read sector
				uint8_t last[sector_size];
				read_sector(addr + i, last);

				// read last sector and break
				str::mcpy(bdata, last, size);
				break;
			}
		}
	}

	void write_cluster(int idx, const void* data, int size) {
		// get cluster address
		int addr = fat::get_cluster(cur_vbr, idx);

		// write all sectors of cluster
		uint8_t* bdata = (uint8_t*) data;	
		for(int i = 0; i < fat::get_cluster_secs(cur_vbr); i++) {
			if(size >= sector_size) {
				// write full sector 
				write_sector(addr + i, bdata);
				bdata += sector_size;
				size -=  sector_size;
			} else {
				// prepare last sector
				uint8_t last[sector_size];
				str::mset(last, 0, sector_size);
				str::mcpy(last, bdata, size);

				// write last sector and break
				write_sector(addr + i, last);
				break;
			}
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
	
	uint16_t fat_find(uint16_t ignore) {
		// get fat beginning
		int beg = fat::get_fat(cur_vbr, 0);

		// go through each fat sector
		int sec_entries = sector_size / 2;
		for(int i = 0; i < fat::get_fat_len(cur_vbr); i++) {
			uint16_t sector[sec_entries];
			read_sector(beg + i, sector);

			// go through each fat sector entry
			for(int j = 0; j < sec_entries; j++) {
				uint16_t val = sector[j];
				uint16_t entry = j + i * sec_entries;
				if(val == fat::free_cluster && entry != ignore) {
					// found it, return free cluster 
					return entry;
				}
			}
		}

		// no suitable sectors found
		utl::panic("Spazio su disco esaurito");
	}

	void fat_set(uint16_t entry, uint16_t val) {
		// get fat beginning
		int beg = fat::get_fat(cur_vbr, 0);

		// get sector from beginning entry is in, and entry in sector
		int sec_entries = sector_size / 2;
		int sec_addr = entry / sec_entries + beg;
		entry = entry % sec_entries;
		
		// read sector
		uint16_t sector[sec_entries];
		read_sector(sec_addr, sector);

		// modify sector
		sector[entry] = val;

		// write sector
		write_sector(sec_addr, sector);
	}

	uint16_t fat_chain(int size) {
		// get number of clusters
		int cluster_len = fat::get_cluster_len(cur_vbr);
		int n_clusters = (size + cluster_len - 1) / cluster_len;

		// init pointers
		uint16_t first;
		uint16_t prev = 0;

		// go through all needed clusters
		for(int i = 0; i < n_clusters; i++) {
			// allocate a cluster
			uint16_t free = fat_find(prev);

			// if first, keep track
			if(i == 0) first = free;
			// if not first, set previous to point to this
			else fat_set(prev, free);

			// update previous
			prev = free;
		}

		// close chain
		fat_set(prev, fat::end_of_chain);

		// return first
		return first;
	}

	void fat_unchain(uint16_t beg) {
		// get fat beginning
		int fat_beg = fat::get_fat(cur_vbr, 0);

		// unroll chain to end
		while(true) {
			// get next cluster
			uint16_t next = fat_lookup(beg);

			// free it
			fat_set(beg, fat::free_cluster);

			// return at end of chain
			if(fat::is_end_of_chain(next)) return;

			// update beginning (current)
			beg = next;
		}
	}

	uint16_t fat_creat_file(const void* buf, int size) {
		// get fat chain of given size
		uint16_t beg = fat_chain(size);
		uint16_t cur = beg;

		// get cluster size
		int cluster_len = fat::get_cluster_len(cur_vbr);

		// write into fat chain
		const uint8_t* bbuf = (const uint8_t*) buf;
		while(true) {
			// write cluster
			int to_write = size > cluster_len ? cluster_len : size;
			write_cluster(cur, bbuf, to_write);

			// advance data pointer and decrease remaining
			bbuf += to_write;
			size -= to_write;

			// get next cluster 
			uint16_t next = fat_lookup(cur);
			if(fat::is_end_of_chain(next)) return beg;

			// update current cluster 
			cur = next;
		}
	}

	uint16_t fat_read_file(uint16_t which, void* buf, int size) {
		// remember beg
		uint16_t cur = which;

		// get cluster size
		int cluster_len = fat::get_cluster_len(cur_vbr);
	
		// read from fat chain
		uint8_t* bbuf = (uint8_t*) buf;
		while(true) {
			// read cluster
			int to_read = size > cluster_len ? cluster_len : size;
			read_cluster(cur, bbuf, to_read);
			
			// advance data pointer and decrease remaining
			bbuf += to_read;
			size -= to_read;
			
			// get next cluster 
			uint16_t next = fat_lookup(cur);
			if(fat::is_end_of_chain(next)) return which;
			
			// update current cluster 
			cur = next;
		}
	}

	uint16_t fat_update_file(uint16_t which, void* buf, int size) {
		// remove the chain
		fat_unchain(which);

		// reallocate file
		return fat_creat_file(buf, size);	
	}

	void fat_delete_file(uint16_t which) {
		// remove the chain
		fat_unchain(which);
	}

	uint16_t cur_dir = ROOT_ALIAS;

	/**
	 * Helper for walking through the current directory, running a function on 
	 * all entries (or just the first one).
	 *
	 * @param fun the function to run on each entry, expected to return bool
	 * @param ctx additional data (context) pointer to send to function
	 * @param first should the function shortcircuit on the first entry?
	 * @param write will the function modify the entries
	 */
	bool walk_dir(bool (*fun)(fat::dir_ent&, void*), 
	              void* ctx = 0, 
	              bool first = false,
				  bool write = false) {
		// root directory
		if(cur_dir == ROOT_ALIAS) {
			// get length and base of rootdir
			int dim = fat::get_rootdir_len(cur_vbr);
			int base = fat::get_rootdir(cur_vbr);

			// prepare directory buffer
			int entries = sector_size / sizeof(fat::dir_ent);
			fat::dir_ent dir[entries];

			// go through each rootdir sector 
			for(int i = 0; i < dim; i++) {
				// read rootdir sector
				read_sector(base + i, dir);

				// go through all entries in sector
				for(int j = 0; j < entries; j++) {
					bool ret = fun(dir[j], ctx);
					if(first && ret) {
						// write if needed
						if(write) write_sector(base + i, dir);
						return true;
					}
				}

				// write if needed
				if(write) write_sector(base + i, dir);
			}

			return false;

		// normal directory
		} else {
			// get cluster size and init cluster index 
			int cluster_len = fat::get_cluster_len(cur_vbr);
			uint16_t cur = cur_dir;

			// prepare directory buffer
			int entries = cluster_len / sizeof(fat::dir_ent);
			fat::dir_ent dir[entries];

			// go through each directory cluster 
			while(true) {
				// read rootdir sector
				read_cluster(cur, dir, cluster_len);

				// go through all entries in cluster 
				for(int j = 0; j < entries; j++) {
					bool ret = fun(dir[j], ctx);
					if(first && ret) {
						// write if needed
						if(write) write_cluster(cur, dir, cluster_len);
						return true;
					}
				}
						
				// write if needed
				if(write) write_cluster(cur, dir, cluster_len);

				// get next cluster 
				uint16_t next = fat_lookup(cur);
				if(fat::is_end_of_chain(next)) return false;
				
				// update current cluster 
				cur = next;
			}
		}
	}

	/**
	 * Helper that lists a single directory entry.
	 *
	 * @param ent the directory entry
	 * @param ctx unused
	 * @return true if directory was listed
	 */
	bool list_dir_ent(fat::dir_ent& ent, void* ctx __attribute__((unused))) {
		// get flags
		char flag = ent.filename[0];
		if(flag == fat::free_dir_ent || flag == '\0') return false;

		// print filename
		for(int i = 0; i < 8; i++) {
			char c = ent.filename[i];
			if(!c) break;
			vid::print_char(c);
		}
		vid::print_char('.');
		for(int i = 0; i < 3; i++) {
			char c = ent.filename[8 + i];
			if(!c) break;
			vid::print_char(c);
		}
		vid::print_str(" ");

		// print size
		vid::print_int(ent.filesize);
		vid::print_str(" bytes ");

		// print dir flag if dir
		if(ent.attrib == fat::dir_attrib) vid::print_strln("<dir>");
		else vid::newline();

		return true;
	}

	void list_dir() {
		walk_dir(list_dir_ent);
	}

	/**
	 * Helper to check if we should change to a given directory entry. Changes
	 * current directory as a side effect.
	 *
	 * @param entry entry to check
	 * @param ctx name of targeted directory
	 * @return true if directory was changed to
	 */
	bool change_dir_ent(fat::dir_ent& ent, void* ctx) {
		// extract context
		char* name = (char*) ctx;
		
		// get flags
		char flag = ent.filename[0];
		if(flag == fat::free_dir_ent || flag == '\0') return false;

		// check if valid directory
		if(ent.attrib != fat::dir_attrib) return false;

		// check filename
		if(str::len(name) > 8) return false;
		for(int i = 0; i < 8; i++) {
			if(name[i] != ent.filename[i]) return false;
		}

		// change to directory
		cur_dir = ent.cluster_lo;
		return true;
	}

	bool change_dir(const char* name) {
		return walk_dir(change_dir_ent, (void*)name);
	}

	/**
	 * Helper to make a directory at a given entry.
	 *
	 * @parem entry entry to try creating directory at
	 * @param ctx name of directory to create
	 * @return true if directory was created
	 */
	bool make_dir_ent(fat::dir_ent& ent, void* ctx) {
		// extract context
		char* name = (char*) ctx;
		
		// get flags
		char flag = ent.filename[0];
		if(flag != fat::free_dir_ent && flag != '\0') return false;

		// set parameters
		str::mset(&ent, 0, sizeof(fat::dir_ent));
		str::mcpy(ent.filename, name, 8);
		ent.cluster_lo = fat_chain(fat::get_cluster_len(cur_vbr));
		ent.attrib = fat::dir_attrib;
		return true;
	}
	
	bool make_dir(const char* name) {
		return walk_dir(make_dir_ent, (void*)name, true, true);
	}
} // blk::
