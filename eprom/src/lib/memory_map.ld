MEMORY {
	EPROM (rx)	: ORIGIN = 0x00000000, LENGTH = 64K
	RAM (rwx)		: ORIGIN = 0x00010000, LENGTH = 64K
	VIDEO (rx)	: ORIGIN = 0x00020000, LENGTH = 3K
}

ENTRY(_start)

SECTIONS {
	/*
	 * Section 1
	 * - start routine
	 * - program code
	 * - read only data
	 */
	.text : {
		KEEP(*(.start))
		*(.text*)
		*(.rodata*)
	} > EPROM

	/*
	 * Section 2
	 * - data (stored in EPROM, loaded in RAM)
	 */
	.data : AT(ADDR(.text) + SIZEOF(.text)) {
		__data_ram_start = .;
		*(.data*)
		__data_ram_end = .;
	} > RAM
	__data_eprom_start = LOADADDR(.data);
	__data_eprom_end = __data_eprom_start + SIZEOF(.data);

	/*
	 * Section 3 (ignored)
	 * - uninitialized data
	 * - unallocated symbols
	 */
	.data (NOLOAD) : AT(__data_eprom_start) {
		*(.bss*)
		*(COMMON)
	} > RAM
	
	/*
	 * Section 4 (ignored)
	 * - video memory
	 */
	.video (NOLOAD) : {
		*(.video*)
	} > VIDEO

	/* sets up stack top */
	__stack_top = ORIGIN(RAM) + LENGTH(RAM);
}
