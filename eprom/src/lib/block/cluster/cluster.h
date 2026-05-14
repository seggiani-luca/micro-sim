#ifndef CLUSTER_H
#define CLUSTER_H

namespace blk {
	/**
	 * Namespace for filesystem cluster handling, including writing/reading 
	 * clusters with FAT table indexing.
	 */
	namespace clu {
		/**
		 * Reads a single cluster into a buffer.
		 *
		 * @param idx index of cluster (in FAT table index space)
		 * @param data buffer to write into
		 * @param size size of buffer
		 */
		void read(int idx, void* data, int size);

		/**
		 * Writes a single cluster from a buffer.
		 *
		 * @param idx index of cluster (in FAT table index space)
		 * @param data buffer to read from
		 * @param size size of buffer
		 */
		void write(int idx, const void* data, int size);

		/**
		 * Zeroes a single cluster. 
		 *
		 * @param idx index of cluster (in cluster space)
		 */
		void zero(int idx);
	} // clu::
} // blk::

#endif
