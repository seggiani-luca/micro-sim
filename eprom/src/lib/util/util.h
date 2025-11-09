#ifndef UTIL_H 
#define UTIL_H

/*
 * Namespace for general utility functions, including program exiting, waiting on input, debugging
 * and error reporting.
 */
namespace utl {	
	/*
	 * Spins indefinitely.
	 */
	extern "C" [[noreturn]] void spin();

	/*
	 * Halts processor.
	 */
	extern "C" [[noreturn]] void halt();

	/*
	 * Signals to launch debug shell. 
	 */
	extern "C" void debugger();

	/*
	 * Waits for any character.
	 */
	extern void wait();

	/*
	 * Panics printing a message and quits.
	 */
	[[noreturn]] extern void panic(const char* msg);
} // utl::

#endif
