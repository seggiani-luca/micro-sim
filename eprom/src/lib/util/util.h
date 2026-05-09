#ifndef UTIL_H 
#define UTIL_H

/**
 * Namespace for general utility functions, including program exiting, waiting
 * on input, debugging and error reporting.
 */
namespace utl {	
	/**
	 * Spins indefinitely.
	 */
	extern "C" [[noreturn]] void spin();

	/**
	 * Halts processor.
	 */
	extern "C" [[noreturn]] void halt();

	/**
	 * Signals to launch debug shell. 
	 */
	extern "C" void debugger();

	/**
	 * Waits for any character.
	 */
	void wait();

	/**
	 * Panics printing a message and quits.
	 *
	 * @param msg message to print 
	 */
	[[noreturn]] void panic(const char* msg);
} // utl::

#endif
