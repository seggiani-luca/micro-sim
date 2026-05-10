#ifndef BLOCK_H
#define BLOCK_H

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
	 * Size of disk device
	 */
	extern int storage_size;

	/**
	* Command to begin reading from disk.
	*/
	extern int read_cmd;

	/**
	* Command to end reading from disk.
	*/
	extern int write_cmd;
} // blk::

#endif
