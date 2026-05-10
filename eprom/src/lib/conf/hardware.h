#ifndef HARDWARE_H
#define HARDWARE_H

#include <cstdint>

/**
 * Namespace for hardware definitions, including memory layout and devices.
 */
namespace hwr {
	/**
	 * Specifies memory layout.
	 */
	namespace mem {
		/**
		 * EPROM byte array.
		 */
		extern volatile uint8_t eprom[];

		/**
		 * Size of EPROM byte array.
		 */
		extern uint32_t eprom_size;

		/**
		 * RAM byte array.
		 */
		extern volatile uint8_t ram[];
		
		/**
		 * Size of RAM byte array.
		 */
		extern uint32_t ram_size;

		/**
		 * VRAM byte array.
		 */
		extern volatile uint8_t vram[];
		
		/**
		 * Size of VRAM byte array.
		 */
		extern uint32_t vram_size;

		/**
		 * Does hardware allow EPROM writes?
		 */
		extern bool eprom_writes_allowed;

		/**
		 * Does hardware allow VRAM reads?
		 */
		extern bool vram_reads_allowed;
	} // mem::

	/**
	 * Specifies devices.
	 */
	namespace dev {
		/**
		 * Defines a video device.
		 */
		struct video_device {
			/**
			 * Cursor row port.
			 */
			volatile uint32_t* row_prt;
			
			/**
			 * Cursor column port.
			 */
			volatile uint32_t* col_prt;

			/**
			 * Text mode column number.
			 */
			int cols;
			
			/**
			 * Text mode row number.
			 */
			int rows;
		};

		/**
		 * Video device mounted on system.
		 */
		extern video_device video;

		/**
		 * Defines a keyboard device.
		 */
		struct keyboard_device {
			/**
			 * Status port, signals if buffer is full.
			 */
			volatile uint32_t* sts_prt;
			
			/**
			 * Data port, contains next key make/break code.
			 */
			volatile uint32_t* dat_prt;
		};
		
		/**
		 * Keyboard device mounted on system.
		 */
		extern keyboard_device keyboard;

		/**
		 * Defines a timer device.
		 */
		struct timer_device {
			/**
			 * Timer 0 gate register.
			 */
			volatile uint32_t* gat0_prt;

			/**
			 * Timer 1 gate register.
			 */
			volatile uint32_t* gat1_prt;
			
			/**
			 * Timer 2 gate register.
			 */
			volatile uint32_t* gat2_prt;
			
			/**
			 * Timer 0 configuration register.
			 */
			volatile uint32_t* con0_prt;

			/**
			 * Timer 1 configuration register.
			 */
			volatile uint32_t* con1_prt;
			
			/**
			 * Timer 2 configuration register.
			 */
			volatile uint32_t* con2_prt;
		};
		
		/**
		 * Timer device mounted on system.
		 */
		extern timer_device timer;

		/**
		 * Defines a network device.
		 */
		struct network_device {
			/**
			 * Transmit buffer port.
			 */
			volatile uint32_t* txb_prt;
			
			/**
			 * Transmit buffer ready (empty) port.
			 */
			volatile uint32_t* txr_prt;
			
			/**
			 * Receive buffer port.
			 */
			volatile uint32_t* rxb_prt;
			
			/**
			 * Receive buffer ready (full) port.
			 */
			volatile uint32_t* rxr_prt;

			/**
			 * Address of this interface.
			 */
			uint32_t addr;
		};

		/**
		 * Network device mounted on system.
		 */
		extern network_device network;

		/**
		 * Defines a block device.
		 */
		struct block_device {
			/**
			 * Data port.
			 */
			volatile uint32_t* buf_prt;
			
			/**
			 * Error port.
			 */
			volatile uint32_t* err_prt;
			
			/**
			 * LBA address port.
			 */
			volatile uint32_t* lba_prt;
			
			/**
			 * Sector number port.
			 */
			volatile uint32_t* scn_prt;
			
			/**
			 * Status/command port.
			 */
			volatile uint32_t* ctl_prt;
		};
		
		/**
		 * Block device (disk) mounted on system.
		 */
		extern block_device disk;

	} // dev::
} // hwr::

#endif
