#ifndef DIR_H
#define DIR_H

#include <cstdint>
#include "../fat/fat16.h"

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
		 * Enum for directory traversal modes.
		 */
		enum traversal_mode {
			/**
			 * Walks the directory, applying a function to each entry. Return 
			 * value is not significant.
			 */
			WALK,
			/**
			 * Walks the directory, looking for an entry that makes a certain
			 * boolean function true. Returns true if found, false otherwise.
			 */
			FIND,
			/**
			 * Walks the directory, looking for a free entry, which it then 
			 * allocates using a function.
			 */
			ALLOC
		};

		/**
		 * Helper for walking through the current directory, in several modes.
		 *
		 * @param fun the function to run on each entry, should return bool
		 * @param mode traversal mode
		 * @param ctx additional data (context) pointer to send to function
		 * @return meaning based on mode:
		 *   - WALK: not significant
		 *   - FIND: entry found
		 *   - ALLOC: entry allocated
		 */
		bool walk(bool (*fun)(fat::dir_ent&, void*),
		          traversal_mode mode,
		          void* ctx = 0);

		/**
		 * Lists contents of current directory.
		 */
		void list();

		/**
		 * Changes current dir to directory with given filename (if found).
		 *
		 * @param name name of directory to change to 
		 * @return boolean representing if operation was succesful
		 */
		bool change(const char* name);

		/**
		 * Creates a new directory.
		 *
		 * @param name name of new directory
		 * @return boolean representing if operation was succesful
		 */
		bool make(const char* name);

		/**
		 * Removes an existing directory.
		 *
		 * @param name name of directory to remove
		 * @return boolean representing if operation was succesful
		 */
		bool remove(const char* name);
		
		/**
		 * Allocates a file and writes a buffer to it.
		 *
		 * @param name name of file to allocate 
		 * @param buf buffer to write
		 * @param size size of buffer
		 * @return boolean representing if operation was succesful
		 */
		bool create_file(const char* name, const void* buf, int size);

		/**
		 * Reads a buffer from an allocated file. 
		 *
		 * @param name name of allocated file 
		 * @param buf buffer to read
		 * @param size size of buffer
		 * @return boolean representing if operation was succesful
		 */
		bool read_file(const char* name, void* buf, int size);
		
		/**
		 * Updates the buffer allocated in a file.
		 *
		 * @param name name of allocated file 
		 * @param buf buffer to write 
		 * @param size size of buffer
		 * @return boolean representing if operation was succesful
		 */
		bool update_file(const char* name, void* buf, int size);
		
		/**
		 * Deletes a file.
		 *
		 * @param name name of file to delete 
		 * @return boolean representing if operation was succesful
		 */
		bool delete_file(const char* name); 
	} // dir::
} // blk::

#endif
