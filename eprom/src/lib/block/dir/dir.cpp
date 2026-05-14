#include "dir.h"
#include "../table/table.h"
#include "../sector/sector.h"
#include "../cluster/cluster.h"
#include "../../video/video.h"
#include "../../util/util.h"
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

		bool walk(bool (*fun)(fat::dir_ent&, void*),
		          traversal_mode mode,
		          void* ctx) {
			// root directory
			if(cur_dir == ROOT_ALIAS) {
				// get length and base of rootdir
				int dim = fat::get_rootdir_len(tab::cur_vbr);
				int base = fat::get_rootdir(tab::cur_vbr);

				// prepare directory buffer
				int entries = sec::size / sizeof(fat::dir_ent);
				fat::dir_ent dir[entries];

				// go through each rootdir sector 
				for(int i = 0; i < dim; i++) {
					// read rootdir sector
					sec::read(base + i, dir);

					// go through all entries in sector
					for(int j = 0; j < entries; j++) {
						fat::dir_ent& ent = dir[j];

						// check flags
						if(mode != ALLOC) {
							if(fat::is_free(ent)) continue;
							if(fat::is_end(ent)) return false;
						} else {
							if(!fat::is_free(ent) & !fat::is_end(ent)) 
								continue;
						}

						// execute
						bool ret = fun(dir[j], ctx);

						switch(mode) {
							case WALK:  continue;
							case FIND:  if(ret) return true;
							case ALLOC: if(ret) {
							            	sec::write(base + i, dir); 
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
				int cluster_len = fat::get_cluster_bts(tab::cur_vbr);
				uint16_t cur = cur_dir;

				// prepare directory buffer
				int entries = cluster_len / sizeof(fat::dir_ent);
				fat::dir_ent dir[entries];

				// go through each directory cluster 
				while(true) {
					// read directory cluster 
					clu::read(cur, dir, cluster_len);

					// go through all entries in cluster 
					for(int j = 0; j < entries; j++) {
						fat::dir_ent& ent = dir[j];

						// check flags
						if(mode != ALLOC) {
							if(fat::is_free(ent)) continue;
							if(fat::is_end(ent)) return false;
						} else {
							if(!fat::is_free(ent) & !fat::is_end(ent)) 
								continue;
						}

						// execute
						bool ret = fun(dir[j], ctx);

						switch(mode) {
							case WALK: continue;
							case FIND: if(ret) return true;
							case ALLOC: if(ret) {
											clu::write(cur, dir, cluster_len); 
							            	return true;
							            }
						}
					}

					// get next cluster 
					uint16_t next = tab::lookup(cur);
					if(fat::is_end_of_chain(next)) {
						if(mode == ALLOC) {
							// need to allocate more space for directory
							next = tab::chain(cluster_len);
							clu::zero(next);

							// append to fat table
							tab::set(cur, next);
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
		bool list_dir_ent(fat::dir_ent& ent, void* ctx __attribute__((unused))) 
		{
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

		void list() {
			walk(list_dir_ent, WALK);
		}

		/**
		 * Helper to check if we should change to a given directory entry. 
		 * Changes current directory as a side effect.
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

		bool change(const char* name) {
			return walk(change_dir_ent, FIND, (void*)name);
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
			mem::set(&ent, 0, sizeof(fat::dir_ent));
			copy_filename(ent.filename, name);
			ent.cluster_lo = tab::chain(fat::get_cluster_bts(tab::cur_vbr));
			ent.attrib = fat::dir_attrib;

			// setup default entries
			fat::dir_ent init_entries[2];
			mem::set(init_entries, 0, sizeof(init_entries));

			// this directory 
			mem::cpy(init_entries[0].filename, fat::dot_filename, 11);
			init_entries[0].attrib = fat::dir_attrib;
			init_entries[0].cluster_lo = ent.cluster_lo;
			
			// previous directory 
			mem::cpy(init_entries[1].filename, fat::ddot_filename, 11);
			init_entries[1].attrib = fat::dir_attrib;
			init_entries[1].cluster_lo = cur_dir;

			// zero and write default entries
			clu::zero(ent.cluster_lo);
			clu::write(ent.cluster_lo, init_entries, sizeof(init_entries));

			return true;
		}
		
		bool make(const char* name) {
			return walk(make_dir_ent, ALLOC, (void*)name);
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
		bool check(uint16_t dir) {
			// move to directory
			uint16_t temp = cur_dir;
			cur_dir = dir;
			
			// check all entries
			bool ret = walk(check_dir_ent, FIND);

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
			if(!check(ent.cluster_lo)) return false;

			// remove entry
			tab::unchain(ent.cluster_lo);
			ent.filename[0] = fat::free_cluster;

			return true;
		}

		bool remove(const char* name) {
			return walk(remove_dir_ent, ALLOC, (void*)name);
		}
	} // dir::
} // blk::
