#ifndef BLOCK_H
#define BLOCK_H

#include "../conf/hardware.h"
#include "sector/sector.h"
#include "cluster/cluster.h"
#include "table/table.h"
#include "dir/dir.h"
#include "fat/fat16.h"

/**
 * Namespace for block devices, mainly disk handling, file system handling
 * and file/directory CRUD operations.
 */
namespace blk {
	/**
	 * Reference to disk device.
	 */
	inline hwr::dev::block_device& disk = hwr::dev::disk;
} // blk::

#endif
