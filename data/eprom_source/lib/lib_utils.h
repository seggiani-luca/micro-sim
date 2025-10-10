#ifndef LIB_UTILS
#define LIB_UTILS

#include "lib_keyb.h"
namespace utl {	
	/*
	 * Spins indefinitely.
	 */
	extern "C" void spin();

	/*
	 * Halts processor.
	 */
	extern "C" void halt();

	/*
	 * Signals to launch debug shell. 
	 */
	extern "C" void debugger();

	/*
	 * Waits for any character.
	 */
	void wait() {
		vid::print_str("Premi un tasto qualsiasi...");
		keyb::get_char();
		vid::newline();

		return;
	}
}

#endif
