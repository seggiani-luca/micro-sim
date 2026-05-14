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
	inline int sector_size = 512;

	/**
	 * Size of disk device
	 */
	inline int storage_size = 1024 * 1024 * 16;

	/**
	 * Number of valid sectors in disk device.
	 */
	inline int num_sectors = storage_size / sector_size;

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
	 * @param idx index of cluster (in cluster space)
	 * @param data buffer to write into
	 * @param size size of buffer
	 */
	void read_cluster(int idx, void* data, int size);

	/**
	 * Writes a single cluster from a buffer.
	 *
	 * @param idx index of cluster (in cluster space)
	 * @param data buffer to read from
	 * @param size size of buffer
	 */
	void write_cluster(int idx, const void* data, int size);

	/**
	 * Zeroes a single cluster. 
	 *
	 * @param idx index of cluster (in cluster space)
	 */
	void zero_cluster(int idx);
	
	/**
	 * Looks up a given fat entry.
	 *
	 * @param fat entry to look up (in fat space)
	 * @return the fat entry
	 */
	uint16_t fat_lookup(uint16_t entry);

	/**
	 * Finds the first free entry in the fat table.
	 *
	 * @param ignore entry to ignore (used for chaining)
	 * @return first free entry (in fat space)
	 */
	uint16_t fat_find(uint16_t ignore = 0);

	/**
	 * Sets an entry of the fat table.
	 *
	 * @param entry entry to set (in fat space)
	 * @param val value to set entry to
	 */
	void fat_set(uint16_t entry, uint16_t val);

	/**
	 * Gets a fat chain of the given size.
	 *
	 * @param size size (in bytes) of fat chain
	 * @return beginning of fat chain (in fat space)
	 */
	uint16_t fat_chain(int size);

	/**
	 * Clears the fat chain beg points to.
	 *
	 * @param beginning of fat chain (in fat space)
	 */
	void fat_unchain(uint16_t beg);

	/**
	 * Allocates a fat chain and writes a buffer to it.
	 *
	 * @param buf buffer to write
	 * @param size size of buffer
	 * @return first cluster of allocated chain
	 */
	uint16_t fat_creat_file(const void* buf, int size);

	/**
	 * Reads a buffer from an allocated fat chain. 
	 *
	 * @param which first cluster of chain to read from
	 * @param buf buffer to read
	 * @param size size of buffer
	 * @return first cluster of allocated chain
	 */
	uint16_t fat_read_file(uint16_t which, void* buf, int size);

	/**
	 * Updates the buffer allocated fat chain. For now deallocates the original
	 * chain and allocates a new one.
	 *
	 * @param which first cluster of chain to update 
	 * @param buf buffer to write 
	 * @param size size of buffer
	 * @return first cluster of newly allocated chain
	 */
	uint16_t fat_update_file(uint16_t which, void* buf, int size);

	/**
	 * Deletes an allocated fat chain.
	 *
	 * @param which first cluster of chain to delete 
	 */
	void fat_delete_file(uint16_t which);

	/**
	 * Fat index of current directory. 0xffff means root.
	 */
	#define ROOT_ALIAS 0xffff
	extern uint16_t cur_dir;

	/**
	 * Lists contents of current directory.
	 */
	void list_dir();

	/**
	 * Changes current dir to directory with given filename (if found).
	 *
	 * @param name name of directory to change to 
	 * @return boolean representing if operation was succesful
	 */
	bool change_dir(const char* name);

	/**
	 * Creates a new directory.
	 *
	 * @param name name of new directory
	 * @return boolean representing if operation was succesful
	 */
	 bool make_dir(const char* name);

	/**
	 * Removes an existing directory.
	 *
	 * @param name name of directory to remove
	 * @return boolean representing if operation was succesful
	 */
	 bool remove_dir(const char* name);

} // blk::table

#endif
