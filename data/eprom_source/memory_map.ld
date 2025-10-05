MEMORY {
	EPROM (rx)	: ORIGIN = 0x00000000, LENGTH = 64K
	RAM (rwx)		: ORIGIN = 0x00010000, LENGTH = 64K
	VIDEO (rx)	: ORIGIN = 0x00020000, LENGTH = 5K
}

ENTRY(_start)

SECTIONS {
	.text : {
		KEEP(*(.start))
		*(.text*)
		*(.rodata*)
		__data_eprom_start = .;
		*(.data*)
		__data_eprom_end = .;
	} > EPROM

	.data (NOLOAD) : AT(__data_eprom_start) {
		. = ALIGN(4);
		__data_ram_start = .;
		. = __data_ram_start + (__data_eprom_end - __data_eprom_start);
		. = ALIGN(4);
		__data_ram_end = .;
		*(.bss*)
		*(.sbss*)
		*(COMMON)
	} > RAM
	
	.video (NOLOAD) : {
		*(.video*)
	} > VIDEO

	__stack_top = ORIGIN(RAM) + LENGTH(RAM);
}
