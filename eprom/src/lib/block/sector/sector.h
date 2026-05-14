#ifndef SECTOR_H
#define SECTOR_H

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
		 * Waits for disk.
		 */
		void wait_for_disk();

		/**
		 * Gives disk a read/write command.
		 *
		 * @param addr address of sector
		 * @param scn number of sectors
		 * @parm cmd command to give
		 */
		void give_disk_command(int addr, int scn, int cmd);

		/**
		 * Reads a single sector into a buffer.
		 *
		 * @param addr address of sector
		 * @param data buffer to write into
		 */
		void read(int addr, void* data);

		/**
		 * Writes a single sector from a buffer.
		 *
		 * @param addr address of sector
		 * @param data buffer to read from 
		 */
		void write(int addr, void* data);

		/**
		 * Zeroes a single sector. 
		 *
		 * @param addr address of sector
		 */
		void zero(int addr);
	} // sec::
} // blk::

#endif
