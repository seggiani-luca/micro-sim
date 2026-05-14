#include "block.h"
#include <cstdint>
#include "../util/util.h"
#include "../video/video.h"
#include "../string/string.h"
#include "fat16.h"

namespace blk {
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

	void zero_cluster(int idx) {
		// get cluster address
		int addr = fat::get_cluster(cur_vbr, idx);
		for(int i = 0; i < fat::get_cluster_secs(cur_vbr); i++) {
			zero_sector(addr + i);
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
		return false;
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
	 * Converts a character to uppercase, for 8.3 naming.
	 *
	 * @param c character to convert
	 * @return converted character
	 */
	inline char to_upper(char c) {
		if(c >= 'a' && c <= 'z') return c - ('a' - 'A');
		return c;
	}

	/**
	 * Copies a filename according to 8.3 specification.
	 *
	 * @param dest array to write resulting 8.3 filename into
	 * @param src presentation filename
	 */
	inline void copy_filename(char dest[11], const char* src) {
		// fill with spaces
		for(int i = 0; i < 11; i++) dest[i] = ' ';

		// basename
		int i = 0;
		while(*src && *src != '.' && i < 8) dest[i++] = to_upper(*src++);
		while(*src && *src != '.') src++;

		// extension
		if(*src == '.') src++;
		i = 8;
		while(*src && i < 11) dest[i++] = to_upper(*src++);
	}

	/**
	 * Compares filenames according to 8.3 specification.
	 *
	 * @param dest original 8.3 filename
	 * @param src presentation filename to compare 
	 */
	inline bool compare_filename(char dest[11], const char* src) {
		// shortcut for . filenames
		if(src[0] == '.') {
			for(int i = 0; i < 11; i++) {
				if(!src[i]) {
					if(dest[i] == ' ') break;
					else return false;
				}
				if(dest[i] != src[i]) return false;
			}

			return true;
		}

		// convert 8.3 to presentation
		char tmp[11];
		copy_filename(tmp, src);

		// compare
		for(int i = 0; i < 11; i++) {
			if(dest[i] != tmp[i])
				return false;
		}

		return true;
	}

	/**
	 * Enum for directory traversal modes.
	 */
	enum traversal_mode {
		WALK,
		FIND,
		ALLOC
	};

	/**
	 * Helper for walking through the current directory, in several modes.
	 *
	 * @param fun the function to run on each entry, expected to return bool
	 * @param mode traversal mode
	 * @param ctx additional data (context) pointer to send to function
	 * @return meaning based on mode:
	 *   - WALK: not significant
	 *   - FIND: entry found
	 *   - ALLOC: entry allocated
	 */
	bool walk_dir(bool (*fun)(fat::dir_ent&, void*),
								traversal_mode mode,
	              void* ctx = 0) {
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
					fat::dir_ent& ent = dir[j];

					// check flags
					if(mode != ALLOC) {
						if(fat::is_free(ent)) continue;
						if(fat::is_end(ent)) return false;
					} else {
						if(!fat::is_free(ent) & !fat::is_end(ent)) continue;
					}

					// execute
					bool ret = fun(dir[j], ctx);

					switch(mode) {
						case WALK: continue;
						case FIND: if(ret) return true;
						case ALLOC: if(ret) {
													write_sector(base + i, dir); 
													return true;
												}
					}
				}
			}

			if(mode == ALLOC) {
				utl::panic("Spazio nella directory di root esaurito");
			} else return false;

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
				// read directory cluster 
				read_cluster(cur, dir, cluster_len);

				// go through all entries in cluster 
				for(int j = 0; j < entries; j++) {
					fat::dir_ent& ent = dir[j];

					// check flags
					if(mode != ALLOC) {
						if(fat::is_free(ent)) continue;
						if(fat::is_end(ent)) return false;
					} else {
						if(!fat::is_free(ent) & !fat::is_end(ent)) continue;
					}

					// execute
					bool ret = fun(dir[j], ctx);

					switch(mode) {
						case WALK: continue;
						case FIND: if(ret) return true;
						case ALLOC: if(ret) {
													write_cluster(cur, dir, cluster_len); 
													return true;
												}
					}
				}

				// get next cluster 
				uint16_t next = fat_lookup(cur);
				if(fat::is_end_of_chain(next)) {
					if(mode == ALLOC) {
						// need to allocate more space for directory
						next = fat_chain(cluster_len);
						zero_cluster(next);

						// append to fat table
						fat_set(cur, next);
					} else return false;
				}
				
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
		int prev_tab = vid::tab_size;
		vid::tab_size = 13;

		// basename size
		int base_siz = 8;
		while(ent.filename[base_siz - 1] == ' ') base_siz--;

		// print basename
		for(int i = 0; i < base_siz; i++) {
			char c = ent.filename[i];
			vid::print_char(c);
		}

		// directory
		if(fat::is_dir(ent)) {
			vid::print_str("\t<dir>");
	
		// file
		} else {
			// extension size
			int ext_siz = 3;
			while(ent.filename[ext_siz + 8] == ' ') ext_siz--;
	
			// print extension
			vid::print_char('.');
			for(int i = 0; i < ext_siz; i++) {
				char c = ent.filename[8 + i];
				vid::print_char(c);
			}

			vid::print_char('\t');

			// print size
			vid::print_int(ent.filesize);
			vid::print_str(" bytes ");
		}

		vid::newline();
		vid::tab_size = prev_tab;
		return true;
	}

	void list_dir() {
		walk_dir(list_dir_ent, WALK);
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
		
		// check if valid directory
		if(!fat::is_dir(ent)) return false;

		// check filename
		if(!compare_filename(ent.filename, name)) return false;

		// change to directory
		cur_dir = ent.cluster_lo;
		return true;
	}

	bool change_dir(const char* name) {
		return walk_dir(change_dir_ent, FIND, (void*)name);
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

		// set parameters
		str::mset(&ent, 0, sizeof(fat::dir_ent));
		copy_filename(ent.filename, name);
		ent.cluster_lo = fat_chain(fat::get_cluster_len(cur_vbr));
		ent.attrib = fat::dir_attrib;

		// setup default entries
		fat::dir_ent init_entries[2];
		str::mset(init_entries, 0, sizeof(init_entries));

		// this directory 
		str::mcpy(init_entries[0].filename, fat::dot_filename, 11);
		init_entries[0].attrib = fat::dir_attrib;
		init_entries[0].cluster_lo = ent.cluster_lo;
		
		// previous directory 
		str::mcpy(init_entries[1].filename, fat::ddot_filename, 11);
		init_entries[1].attrib = fat::dir_attrib;
		init_entries[1].cluster_lo = cur_dir;

		// zero and write default entries
		zero_cluster(ent.cluster_lo);
		write_cluster(ent.cluster_lo, init_entries, sizeof(init_entries));

		return true;
	}
	
	bool make_dir(const char* name) {
		return walk_dir(make_dir_ent, ALLOC, (void*)name);
	}

	/**
	 * Helper to check a single directory entry, used by check_dir.
	 *
	 * @param ent not significant
	 * @param ctx not significant
	 * @return always true
	 */
	bool check_dir_ent(fat::dir_ent& ent, void* ctx) { return true; }

	/**
	 * Helper to check if a directory is empty.
	 *
	 * @param dir fat index of directory to check
	 * @return is directory empty?
	 */
	bool check_dir(uint16_t dir) {
		// move to directory
		uint16_t temp = cur_dir;
		cur_dir = dir;
		
		// check all entries
		bool ret = walk_dir(check_dir_ent, FIND);

		// reset current directory
		cur_dir = temp;

		return !ret;
	}

	/**
	 * Helper for removing a directory entry.
	 *
	 * @param entry entry to check
	 * @param ctx name of targeted directory
	 * @return true if directory was removed
	 */
	bool remove_dir_ent(fat::dir_ent& ent, void* ctx) {
		// extract context
		char* name = (char*) ctx;
		
		// check if valid directory
		if(!fat::is_dir(ent)) return false;

		// check filename
		if(!compare_filename(ent.filename, name)) return false;

		// check if directory is empty
		if(!check_dir(ent.cluster_lo)) return false;

		// remove entry
		fat_unchain(ent.cluster_lo);
		ent.filename[0] = fat::free_cluster;

		return true;
	}

	bool remove_dir(const char* name) {
		return walk_dir(remove_dir_ent, ALLOC, (void*)name);
	}
} // blk::
