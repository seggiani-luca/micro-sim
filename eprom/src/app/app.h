#ifndef APP_H
#define APP_H

#include "../lib/lib.h"

/**
 * Namespace for application definitions, including global application table.
 */
namespace app {
	/**
	 * Shell entry point, declared here for boot.
	 */
	extern ENTRY(shell);

	/**
	 * Other entry points.
	 */
	extern ENTRY(pacman);
	extern ENTRY(editor);

	/**
	 * Global application table entry, not to be confused with exe::entry 
	 * (which defines a program entry point). 
	 */
	struct entry {
		/**
		 * Name of application.
		 */
		const char* name;

		/**
		 * Short description.
		 */
		const char* desc;

		/**
		 * Application entry point.
		 */
		exe::entry ent;
	};

	/**
	 * Global application table.
	 */
	inline entry table[] = {
		{
			"sh",
			"shell di default",
			shell
		},
		{
			"riscman",
			"clone del gioco PACMAN (C) NAMCO",
			pacman
		},
		{
			"ed",
			"editor di testo",
			editor
		}
	};
	#define APP_TABLE_SIZE (sizeof(::app::table) / sizeof(::app::entry))
}

#endif
