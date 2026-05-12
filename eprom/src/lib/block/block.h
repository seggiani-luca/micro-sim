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
	 * Boot sector of mounted disk.
	 */
	extern fat::vbr cur_vbr;

	/**
	 * Checks mounted disk and formats if needed.
	 */
	extern "C" void check_disk();

	/**
	 * Reads a single cluster into a buffer.
	 *
	 * @param addr index of clusser
	 * @param data buffer to write into
	 */
	void read_cluster(int idx, void* data);

	/**
	 * Writes a single cluster from a buffer.
	 *
	 * @param addr index of sector
	 * @param data buffer to read from 
	 */
	void write_cluster(int idx, void* data);

	/**
	 * Looks up a given fat entry.
	 *
	 * @param fat entry to look up
	 * @return the fat entry
	 */
	uint16_t fat_lookup(uint16_t entry);

	/**
	 * Allocates a fat entry (inserting end of chain) and returns its index.
	 *
	 * @return the allocated fat entry
	 */
	uint16_t fat_allocate();

	/**
	 * Writes into a fat entry.
	 *
	 * @param entry to write in
	 * @param val what to write in entry
	 */
	void fat_write(uint16_t entry, uint16_t val);

	/**
	 * Reads a file into a buffer. Assuming buffer to be sized to file.
	 *
	 * @param entry directory entry of file to read
	 * @param buf buffer to read file into
	 */
	void read_file(fat::dir_ent entry, void* buf);
	
	/**
	 * Writes a buffer into a file, truncating original file.
	 *
	 * @param entry directory entry of file to write 
	 * @param buf buffer to write to file 
	 */
	void trunc_file(fat::dir_ent entry, void* buf);
	
	/**
	 * Writes a buffer into a file, appending to original file.
	 *
	 * @param entry directory entry of file to write 
	 * @param buf buffer to write to file 
	 */
	void append_file(fat::dir_ent entry, void* buf);
} // blk::table

#endif
