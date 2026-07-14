#ifndef SECTOR_H
#define SECTOR_H

#include "../../string/string.h"

namespace blk {
	/**
	 * Namespace for raw block device handling, including writing/reading 
	 * logical sectors with LBA (Logical Block Addressing) addressing. 
	 */
	namespace sec {
		/**
		 * Size of disk sector
		 */
		inline int size = 512;

		/**
		 * Size of disk device
		 */
		inline int storage_size = 1024 * 1024 * 16;

		/**
		 * Number of valid sectors in disk device.
		 */
		inline int num = storage_size / size;

		/**
		* Command to begin reading from disk.
		*/
		extern int read_cmd;

		/**
		* Command to end reading from disk.
		*/
		extern int write_cmd;
		
		/**
		 * Error bit.
		 */
		extern int err_bit;

		/**
		 * Data request bit.
		 */
		extern int drq_bit;

		/**
		 * Busy bit.
		 */
		extern int bsy_bit;

		/**
		 * Waits for disk.
		 */
		void wait_for_disk();

		/**
		 * Gives disk a read/write command.
		 *
		 * @param addr address of sector
		 * @param scn number of sectors
		 * @param cmd command to give
		 */
		void give_disk_command(uint32_t addr, int scn, int cmd);

		/**
		 * Reads a single sector into a buffer.
		 *
		 * @param addr address of sector
		 * @param data buffer to write into, must be size to at least 512 bytes
		 */
		void read(uint32_t addr, void* data);

		/**
		 * Writes a single sector from a buffer.
		 *
		 * @param addr address of sector
		 * @param data buffer to read from, must be sized to at least 512 btyes
		 */
		void write(uint32_t addr, const void* data);

		/**
		 * Zeroes a single sector. 
		 *
		 * @param addr address of sector
		 */
		void zero(uint32_t addr);
	} // sec::
} // blk::

#endif
