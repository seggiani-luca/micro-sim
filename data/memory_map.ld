MEMORY {
	EPROM (rx)	: ORIGIN = 0x00000000, LENGTH = 64K
	RAM (rwx)		: ORIGIN = 0x00010000, LENGTH = 64K
	VIDEO (rx)	: ORIGIN = 0x00020000, LENGTH = 5K
}

SECTIONS {
	.text : {
		*(.text*)
		*(.rodata*)
	} > EPROM

	.data : {
		*(.data*)
	} > RAM

	.bss : {
		*(.bss*)
		*(COMMON)
	} > RAM
	
	.video (NOLOAD) : {
		*(.video*)
	} > VIDEO
}
