#ifndef TABLE_H
#define TABLE_H

#include "../fat/fat16.h"

namespace blk {
	/**
	 * Namespace for file system FAT (File Allocation Table= handling, 
	 * including FAT table handling, FAT chain creation and buffer allocation
	 * in filesystem clusters. Also contains functions related to filesystem
	 * initialization and VBR (Volume Boot Record) handling.
	 */
	namespace tab {
		/**
		 * Default boot sector.
		 */
		extern fat::vbr def_vbr;

		/**
		 * Boot sector of mounted disk.
		 */
		extern fat::vbr cur_vbr;

		/**
		 * Checks mounted disk and formats if needed.
		 */
		extern "C" void check_disk();

		/**
		 * Looks up a given FAT table entry.
		 *
		 * @param FAT table entry to look up (in FAT table index space)
		 * @return the FAT entry
		 */
		uint16_t lookup(uint16_t entry);

		/**
		 * Finds the first free entry in the FAT table.
		 *
		 * @param ignore entry to ignore (used for chaining)
		 * @return first free entry (in FAT table index space)
		 */
		uint16_t find(uint16_t ignore = 0);

		/**
		 * Sets an entry of the FAT table.
		 *
		 * @param entry entry to set (in FAT table index space)
		 * @param val value to set entry to
		 */
		void set(uint16_t entry, uint16_t val);

		/**
		 * Gets a FAT table chain of the given size.
		 *
		 * @param size size (in bytes) of FAT table chain
		 * @return beginning of FAT table chain (in FAT table index space)
		 */
		uint16_t chain(int size);

		/**
		 * Clears the FAT table chain beg points to.
		 *
		 * @param beg beginning of FAT tabl chain (in FAT space)
		 */
		void unchain(uint16_t beg);

		/**
		 * Allocates a FAT table chain and writes a buffer to it.
		 *
		 * @param buf buffer to write
		 * @param size size of buffer
		 * @return first cluster of allocated chain
		 */
		uint16_t create_file(const void* buf, int size);

		/**
		 * Reads a buffer from an allocated FAT table chain. 
		 *
		 * @param which first cluster of chain to read from
		 * @param buf buffer to read
		 * @param size size of buffer
		 * @return first cluster of allocated chain
		 */
		uint16_t read_file(uint16_t which, void* buf, int size);

		/**
		 * Updates the buffer allocated in a FAT table chain. For now 
		 * deallocates the original chain and allocates a new one.
		 *
		 * @param which first cluster of chain to update 
		 * @param buf buffer to write 
		 * @param size size of buffer
		 * @return first cluster of newly allocated chain
		 */
		uint16_t update_file(uint16_t which, void* buf, int size);

		/**
		 * Deletes an allocated FAT table chain.
		 *
		 * @param which first cluster of chain to delete 
		 */
		void delete_file(uint16_t which);
	} // tab::
} // blk::

#endif
