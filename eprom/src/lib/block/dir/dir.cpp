#include "dir.h"
#include "../table/table.h"
#include "../sector/sector.h"
#include "../cluster/cluster.h"
#include "../../video/video.h"
#include "../../string/string.h"

namespace blk {
	namespace dir {
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

		dir_iter::dir_iter(uint16_t dir) {
			// initialize iterator
			entry = 0;
			valid = true;

			// rootdir 
			if(dir == ROOT_ALIAS) {
				// setup rootdir iterator
				root = true;
				block.sector = fat::get_rootdir(tab::cur_vbr);

				// prepare directory buffer
				entries = sec::size / sizeof(fat::dir_ent);
				sec::read(block.sector, cache);
				
			// normal directory
			} else {
				// setup normal directory iterator
				root = false;
				block.cluster = dir;

				// prepare directory buffer
				int cluster_len = fat::get_cluster_bts(tab::cur_vbr);
				entries = cluster_len / sizeof(fat::dir_ent);
				clu::read(block.cluster, cache, cluster_len);
			}	
		}
		
		fat::dir_ent& dir_iter::get_entry() {
			return cache[entry];
		}

		void dir_iter::sync() {
			// rootdir
			if(root) {
				sec::write(block.sector, cache);

			// normal directory
			} else {
				int cluster_len = fat::get_cluster_bts(tab::cur_vbr);
				clu::write(block.cluster, cache, cluster_len);	
			}
		}	

		bool dir_iter::next(bool alloc) {
			if(!valid) return false;

			// advance entry
			entry++;
			if(entry != entries) return true;
			// have to move to next block 

			// rootdir
			if(root) {
				// get next sector
				block.sector++;

				// check if end reached
				int end = fat::get_rootdir(tab::cur_vbr) +
				          fat::get_rootdir_len(tab::cur_vbr);
				if(block.sector >= end) {
					entry--;
					valid = false;
					return false;
				}
				
				// cache this sector
				sec::read(block.sector, cache);

			// normal directory
			} else {
				// get next cluster 
				block.cluster = tab::lookup(block.cluster);

				// check if end reached
				if(fat::is_end_of_chain(block.cluster)) {
					if(alloc) {
						// need to allocate more space for directory
						int cluster_len = fat::get_cluster_len(tab::cur_vbr);
						int next = tab::chain(cluster_len);
						clu::zero(next);

						// append to fat table
						tab::set(block.cluster, next);
					} else {
						entry--;;
						valid = false;
						return false;
					}
				}
				
				// cache this cluster 
				int cluster_len = fat::get_cluster_len(tab::cur_vbr);
				clu::read(block.cluster, cache, cluster_len);
			}

			// reset entry
			entry = 0;
			return true;
		}
		
		bool is_empty(uint16_t dir) {
			// create iterator on given directory
			dir_iter itr = dir_iter(dir);
			
			do {
				fat::dir_ent& ent = itr.get_entry();

				// ignore dot entries
				if(compare_filename(ent.filename, fat::dot_filename)) 
					continue;
				if(compare_filename(ent.filename, fat::ddot_filename)) 
					continue;

				// check that there are no filled entries
				if(fat::is_end(ent)) return true;
				if(!fat::is_free(ent)) return false;
			} while(itr.next());

			return true;
		}

		void list(uint16_t dir) {
			// tabulate to 8.3 filenames
			int prev_tab = vid::tab_size;
			vid::tab_size = 13;

			// create iterator on given directory
			dir_iter itr = dir_iter(dir);
			
			do {
				fat::dir_ent& ent = itr.get_entry();

				// check if valid
				if(fat::is_free(ent)) continue;
				if(fat::is_end(ent)) break;

				// get basename size
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
					// get extension size
					int ext_siz = 3;
					while(ent.filename[ext_siz + 7] == ' ') ext_siz--;
			
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
			} while(itr.next());

			// reset tabulation
			vid::tab_size = prev_tab;
		}

		bool find(const char* name, uint16_t dir, fat::dir_ent& out) {
			// create iterator on given directory
			dir_iter itr = dir_iter(dir);
			
			do {
				fat::dir_ent& ent = itr.get_entry();

				// check if valid
				if(fat::is_free(ent)) continue;
				if(fat::is_end(ent)) break;
				if(!compare_filename(ent.filename, name)) continue; 

				// return entry
				mem::cpy(&out, &ent, sizeof(fat::dir_ent));
				return true;
			} while(itr.next());

			return false; 
		}
		
		bool make(const char* name, uint16_t dir) {
			// don't duplicate files
			fat::dir_ent ent;
			if(find(name, dir, ent)) return false;

			// create iterator on given directory
			dir_iter itr = dir_iter(dir);
			
			do {
				fat::dir_ent& ent = itr.get_entry();

				// check if valid
				if(!fat::is_free(ent) && !fat::is_end(ent)) continue;

				// set entry parameters
				mem::set(&ent, 0, sizeof(fat::dir_ent));
				copy_filename(ent.filename, name);
				ent.cluster_lo 
					= tab::chain(fat::get_cluster_bts(tab::cur_vbr));
				ent.attrib = fat::dir_attrib;

				// setup default entries
				fat::dir_ent init_entries[2];
				mem::set(init_entries, 0, sizeof(init_entries));

				// point to this directory 
				mem::cpy(init_entries[0].filename, fat::dot_filename, 11);
				init_entries[0].attrib = fat::dir_attrib;
				init_entries[0].cluster_lo = ent.cluster_lo;
				
				// point to previous directory 
				mem::cpy(init_entries[1].filename, fat::ddot_filename, 11);
				init_entries[1].attrib = fat::dir_attrib;
				init_entries[1].cluster_lo = cur_dir;

				// zero and write default entries
				clu::zero(ent.cluster_lo);
				clu::write(ent.cluster_lo, init_entries, sizeof(init_entries));
	
				// sync changes
				itr.sync();

				return true;
			} while(itr.next(true));

			return false; 
		}

		bool remove(const char* name, uint16_t dir) {
			// create iterator on given directory
			dir_iter itr = dir_iter(dir);
			
			do {
				fat::dir_ent& ent = itr.get_entry();

				// check if valid
				if(fat::is_free(ent)) continue;
				if(fat::is_end(ent)) break;
				if(!compare_filename(ent.filename, name)) continue; 

				// don't delete non empty directories
				if(!is_empty(ent.cluster_lo)) return false;

				// remove entry
				tab::unchain(ent.cluster_lo);
				ent.filename[0] = fat::free_dir_ent;
				
				// sync changes
				itr.sync();
				
				return true;
			} while(itr.next());

			return false; 
		}

		bool create_file(const char* name, const void* buf, int size, 
				uint16_t dir) {
			// don't duplicate files
			fat::dir_ent ent;
			if(find(name, dir, ent)) return false;
			
			// create iterator on given directory
			dir_iter itr = dir_iter(dir);
			
			do {
				fat::dir_ent& ent = itr.get_entry();

				// check if valid
				if(!fat::is_free(ent) && !fat::is_end(ent)) continue;

				// allocate FAT chain
				uint16_t chain = tab::create_file(buf, size);
		
				// set parameters 
				copy_filename(ent.filename, name);
				ent.cluster_lo = chain;
				ent.filesize = size;
				ent.attrib = fat::file_attrib;
					
				// sync changes
				itr.sync();
				
				return true;
			} while(itr.next(true));

			return false;
		}

		bool read_file(const char* name, void* buf, int size, uint16_t dir) {
			// find file
			fat::dir_ent ent;
			if(!find(name, dir, ent)) return false;
	
			// check if valid
			if(!fat::is_file(ent)) return false;

			// extract filesize and check 
			int fil_size = ent.filesize;
			if(size < fil_size) return false;

			// read file
			tab::read_file(ent.cluster_lo, buf, fil_size);

			return true;
		}
		
		bool update_file(const char* name, void* buf, int size, uint16_t dir) {
			// find file
			fat::dir_ent ent;
			if(!find(name, dir, ent)) return false;
	
			// check if valid
			if(!fat::is_file(ent)) return false;

			// update file
			tab::update_file(ent.cluster_lo, buf, size);

			return true;
		}
		
		bool delete_file(const char* name, uint16_t dir) {
			// create iterator on given directory
			dir_iter itr = dir_iter(dir);
			
			do {
				fat::dir_ent& ent = itr.get_entry();

				// check if valid
				if(fat::is_free(ent)) continue;
				if(fat::is_end(ent)) break;
				if(!fat::is_file(ent)) continue; 
				if(!compare_filename(ent.filename, name)) continue; 

				// delete file
				tab::delete_file(ent.cluster_lo);
				ent.filename[0] = fat::free_dir_ent;

				// sync changes
				itr.sync();

				return true;
			} while(itr.next());

			return false; 
		} 
	} // dir::
} // blk::
