#ifndef BLOCK_H
#define BLOCK_H

#include "fat16.h"
#include "../conf/hardware.h"

/**
 * Namespace for block devices, mainly disk handling. 
 */
namespace blk {
	/**
	 * Reference to disk device.
	 */
	inline hwr::dev::block_device& disk = hwr::dev::disk;

	/**
	 * Size of disk sector
	 */
	extern int sector_size;

	/**
	 * Size of disk device
	 */
	extern int storage_size;

	/**
	 * Number of valid sectors in disk device.
	 */
	extern int num_sectors;

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
	void read_sector(int addr, void* data);

	/**
	 * Writes a single sector from a buffer.
	 *
	 * @param addr address of sector
	 * @param data buffer to read from 
	 */
	void write_sector(int addr, void* data);

	/**
	 * Zeroes a single sector. 
	 *
	 * @param addr address of sector
	 */
	void zero_sector(int addr);

	/**
	 * Default boot sector.
	 */
	extern fat::vbr def_vbr;

	/**
	 * Checks mounted disk and formats if needed.
	 */
	extern "C" void check_disk();
} // blk::

#endif
