#include "table.h"
#include "../sector/sector.h"
#include "../cluster/cluster.h"
#include "../../video/video.h"
#include "../../string/string.h"
#include "../../util/util.h"

namespace blk {
	namespace tab {
		fat::vbr def_vbr = {
			.jump = {0},
			.oem_name = {'m', 'i', 'c', 'r', 'o','s', 'i', 'm'},
			.param = {
				.log_sec_len = (uint16_t) sec::size,
				.cluster_len = 1,
				.reserved_secs = 1,
				.n_fats = 1,
				.root_dir_entries = 16,
				.n_log_secs = (uint16_t) sec::num,
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
			sec::read(0, &cur_vbr);

			// check for magic
			if(cur_vbr.magic == def_vbr.magic) return;

			// not a valid fat disk, format
			vid::print_strln("Disco non formattato, formatto a FAT16...");

			// write boot sector
			mem::cpy(&cur_vbr, &def_vbr, sizeof(fat::vbr));
			sec::write(0, &def_vbr);

			// get fat beginning
			int fat_beg = fat::get_fat(def_vbr, 0);

			// create fat (fill first sector) 
			uint16_t fat_sector[sec::size / 2];
			mem::set(fat_sector, 0, sec::size);
			fat_sector[0] = 0xfff8;
			fat_sector[1] = 0xffff;
			sec::write(fat_beg, fat_sector);

			// zero following sectors
			for(int i = 1; i < fat::get_fat_len(def_vbr); i++) {
				sec::zero(fat_beg + i);
			}
			
			// get fat beginning
			int rootdir_beg = fat::get_rootdir(def_vbr);

			// create rootdir (fill sector)
			fat::dir_ent rootdir[def_vbr.param.root_dir_entries];
			mem::set(rootdir, 0, sec::size);
			mem::cpy(rootdir[0].filename, def_vbr.ex_param.label, 11);
			rootdir[0].attrib = fat::vol_id_attrib;
			sec::write(rootdir_beg, rootdir);

			// zero following sectors
			for(int i = 1; i < fat::get_rootdir_len(def_vbr); i++) {
				sec::zero(rootdir_beg + i);
			}

			// notify formatting has finished
			vid::print_strln("Formattazione completata");
			utl::wait();
			vid::clear();
		}
		
		uint16_t lookup(uint16_t entry) {
			// get fat beginning
			int beg = fat::get_fat(cur_vbr, 0);

			// get sector from beginning entry is in, and entry in sector
			int sec_entries = sec::size / 2;
			int sec_addr = entry / sec_entries + beg;
			entry = entry % sec_entries;

			// read sector
			uint16_t sector[sec_entries];
			sec::read(sec_addr, sector);

			// read entry
			return sector[entry];	
		}
		
		uint16_t find(uint16_t ignore) {
			// get fat beginning
			int beg = fat::get_fat(cur_vbr, 0);

			// go through each fat sector
			int sec_entries = sec::size / 2;
			for(int i = 0; i < fat::get_fat_len(cur_vbr); i++) {
				uint16_t sector[sec_entries];
				sec::read(beg + i, sector);

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

		void set(uint16_t entry, uint16_t val) {
			// get fat beginning
			int beg = fat::get_fat(cur_vbr, 0);

			// get sector from beginning entry is in, and entry in sector
			int sec_entries = sec::size / 2;
			int sec_addr = entry / sec_entries + beg;
			entry = entry % sec_entries;
			
			// read sector
			uint16_t sector[sec_entries];
			sec::read(sec_addr, sector);

			// modify sector
			sector[entry] = val;

			// write sector
			sec::write(sec_addr, sector);
		}

		uint16_t chain(int size) {
			// get number of clusters
			int cluster_len = fat::get_cluster_bts(cur_vbr);
			int n_clusters = (size + cluster_len - 1) / cluster_len;

			// init pointers
			uint16_t first;
			uint16_t prev = 0;

			// go through all needed clusters
			for(int i = 0; i < n_clusters; i++) {
				// allocate a cluster
				uint16_t free = find(prev);

				// if first, keep track
				if(i == 0) first = free;
				// if not first, set previous to point to this
				else set(prev, free);

				// update previous
				prev = free;
			}

			// close chain
			set(prev, fat::end_of_chain);

			// return first
			return first;
		}

		void unchain(uint16_t beg) {
			// get fat beginning
			int fat_beg = fat::get_fat(cur_vbr, 0);

			// unroll chain to end
			while(true) {
				// get next cluster
				uint16_t next = lookup(beg);

				// free it
				set(beg, fat::free_cluster);

				// return at end of chain
				if(fat::is_end_of_chain(next)) return;

				// update beginning (current)
				beg = next;
			}
		}

		uint16_t create_file(const void* buf, int size) {
			// get fat chain of given size
			uint16_t beg = chain(size);
			uint16_t cur = beg;

			// get cluster size
			int cluster_len = fat::get_cluster_bts(cur_vbr);

			// write into fat chain
			const uint8_t* bbuf = (const uint8_t*) buf;
			while(true) {
				// write cluster
				int to_write = size > cluster_len ? cluster_len : size;
				clu::write(cur, bbuf, to_write);

				// advance data pointer and decrease remaining
				bbuf += to_write;
				size -= to_write;

				// get next cluster 
				uint16_t next = lookup(cur);
				if(fat::is_end_of_chain(next)) return beg;

				// update current cluster 
				cur = next;
			}
		}

		uint16_t read_file(uint16_t which, void* buf, int size) {
			// remember beg
			uint16_t cur = which;

			// get cluster size
			int cluster_len = fat::get_cluster_bts(cur_vbr);
		
			// read from fat chain
			uint8_t* bbuf = (uint8_t*) buf;
			while(true) {
				// read cluster
				int to_read = size > cluster_len ? cluster_len : size;
				clu::read(cur, bbuf, to_read);
				
				// advance data pointer and decrease remaining
				bbuf += to_read;
				size -= to_read;
				
				// get next cluster 
				uint16_t next = lookup(cur);
				if(fat::is_end_of_chain(next)) return which;
				
				// update current cluster 
				cur = next;
			}
		}

		uint16_t update_file(uint16_t which, void* buf, int size) {
			// remove the chain
			unchain(which);

			// reallocate file
			return create_file(buf, size);	
		}

		void delete_file(uint16_t which) {
			// remove the chain
			unchain(which);
		}
	} // tab::
} // blk::
