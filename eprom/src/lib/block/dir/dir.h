#ifndef DIR_H
#define DIR_H

#include "../../string/string.h" 
#include "../fat/fat16.h"
#include "../table/table.h"

namespace blk {
	/**
	 * Namespace for directory handling, including directory 
	 * listing/finding/creating/removing, and file CRUD operations.
	 */
	namespace dir {
		/**
		 * FAT table index of current directory. 0xffff means root.
		 */
		#define ROOT_ALIAS 0 

		/**
		 * FAT table index of current directory.
		 */
		extern uint16_t cur_dir;
		
		/**
		 * Iterator for directory traversal.
		 */
		struct dir_iter {
		private:
			/**
			 * Is this iterator on the rootdir?
			 */
			bool root;
			
			/**
			 * Block address of iterator, can be sector (for rootdir) or 
			 * cluster (for normal dirs).
			 */
			union {
				int sector;
				uint16_t cluster;
			} block;

			/**
			 * Current (cached) block. 
			 */
			fat::dir_ent cache[MAX_CLUSTER_SIZE / sizeof(fat::dir_ent)];

			/**
			 * Entries in current block.
			 */
			int entries;

			/**
			 * Entry in the current block.
			 */
			int entry;

			/**
			 * Did the iterator reach end?
			 */
			bool valid;
		public:
			/**
			 * Returns an iterator for preorder traversal of directory.
			 *
			 * @param dir directory to iterate over 
			 */
			dir_iter(uint16_t dir);

			/**
			 * Returns the current entry.
			 */
			fat::dir_ent& get_entry();

			/**
			 * Syncs cached block to disk.
			 */
			void sync();

			/**
			 * Moves iterator to next directory entry.
			 *
			 * @param alloc should a new cluster be allocated if at the end of
			 *        the directory?
			 * @return did iterator reach end?
			 */
			bool next(bool alloc = false);
			
			/**
			 * Moves iterator to previous directory entry. TODO
			 *
			 * @return did iterator reach end?
			 */
			bool prev();

			/**
			 * Moves iterator down into directory. TODO
			 *
			 * @return did iterator reach end?
			 */
			bool down();
			
			/**
			 * Moves iterator down into previous directory. TODO
			 *
			 * @return did iterator reach end?
			 */
			bool up();
		};

		/**
		 * Checks if a directory is empty.
		 *
		 * @param dir directory to check
		 * @return is directory empty?
		 */
		bool is_empty(uint16_t dir);

		/**
		 * Lists contents of directory.
		 *
		 * @param dir directory to list
		 */
		void list(uint16_t dir);

		/**
		 * Finds entry with given filename (shallow).
		 *
		 * @param name name of entry to find
		 * @param dir directory to look into
		 * @param out output parameter for found entry
		 * @return was the entry found? 
		 */
		bool find(const char* name, uint16_t dir, fat::dir_ent& out);

		/**
		 * Creates a new directory.
		 *
		 * @param name name of new directory
		 * @param dir directory to create new directory into
		 * @return was the new directory created? 
		 */
		bool make(const char* name, uint16_t dir);

		/**
		 * Removes an existing directory, matching filename (shallow).
		 *
		 * @param name name of directory to remove
		 * @param dir directory to look into 
		 * @return was the directory removed? 
		 */
		bool remove(const char* name, uint16_t dir);
		
		/**
		 * Allocates a file and writes a buffer to it.
		 *
		 * @param name name of file to allocate 
		 * @param buf buffer to write
		 * @param size size of buffer
		 * @param dir directory to create file in
		 * @return was the file created? 
		 */
		bool create_file(const char* name, const void* buf, int size, 
				uint16_t dir);

		/**
		 * Reads a buffer from an allocated file. Might not fill the buffer:
		 * writes only up to filesize. 
		 *
		 * @param name name of allocated file 
		 * @param buf buffer to read
		 * @param size size of buffer
		 * @param dir directory to read file in 
		 * @return was the file read? 
		 */
		bool read_file(const char* name, void* buf, int size, uint16_t dir);
		
		/**
		 * Updates the buffer allocated in a file.
		 *
		 * @param name name of allocated file 
		 * @param buf buffer to write 
		 * @param size size of buffer
		 * @param dir directory to update file in
		 * @return was the file updated? 
		 */
		bool update_file(const char* name, void* buf, int size, uint16_t dir);
		
		/**
		 * Deletes a file.
		 *
		 * @param name name of file to delete 
		 * @param dir directory to delete file in
		 * @return was the file deleted? 
		 */
		bool delete_file(const char* name, uint16_t dir); 
	} // dir::
} // blk::

#endif
