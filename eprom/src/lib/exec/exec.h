#ifndef EXEC_H
#define EXEC_H


/**
 * Namespace for program execution handling.
 */
namespace exe {
	/**
	 * Function pointer for executable entry point.
	 */
	typedef int (*entry)(int argc, char* argv[]);

	#define ENTRY(name) int name(int argc, char* argv[])
} // exe::

#endif
