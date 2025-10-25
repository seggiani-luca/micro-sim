#ifndef HARDWARE_H
#define HARDWARE_H

#include <cstdint>

/*
 * Namespace including hardware definitions, including memory layout and devices.
 */
namespace hwr {
	/*
	 * Specifies memory layout.
	 */
	namespace mem {
		/*
		 * EPROM byte array.
		 */
		extern volatile uint8_t eprom[];

		/*
		 * Size of EPROM byte array.
		 */
		extern uint32_t eprom_size;

		/*
		 * RAM byte array.
		 */
		extern volatile uint8_t ram[];
		
		/*
		 * RAM byte array.
		 */
		extern uint32_t ram_size;

		/*
		 * VRAM byte array.
		 */
		extern volatile uint8_t vram[];
		
		/*
		 * VRAM byte array.
		 */
		extern uint32_t vram_size;

		/*
		 * Does hardware allow EPROM writes?
		 */
		extern bool eprom_writes_allowed;

		/*
		 * Does hardware allow VRAM reads?
		 */
		extern bool vram_reads_allowed;
	} // mem::

	/*
	 * Specifies devices.
	 */
	namespace dev {
		/*
		 * Defines a video device.
		 */
		struct video_device {
			/*
			 * Cursor row register.
			 */
			volatile uint32_t* cur_row_reg;
			
			/*
			 * Cursor column register.
			 */
			volatile uint32_t* cur_col_reg;

			/*
			 * Text mode columns.
			 */
			int cols;
			
			/*
			 * Text mode rows.
			 */
			int rows;
		};

		/*
		 * Video device mounted on system.
		 */
		extern video_device video;

		/*
		 * Defines a keyboard device.
		 */
		struct keyboard_device {
			/*
			 * Status register, signals if buffer is full.
			 */
			volatile uint32_t* sts_reg;
			
			/*
			 * Buffer register, contains character.
			 */
			volatile uint32_t* buf_reg;
		};
		
		/*
		 * Keyboard device mounted on system.
		 */
		extern keyboard_device keyboard;

		/*
		 * Defines a timer device.
		 */
		struct timer_device {
			/*
			 * Status register, sets on timer ticks and resets on reads.
			 */
			volatile uint32_t* sts_reg;
		};
		
		/*
		 * Timer device mounted on system.
		 */
		extern timer_device timer;

	} // dev::
} // hwr::

#endif
