	.section .start
	.extern main

/* start routine */

	.global _start
_start:
	/* init stack to top */
	la sp, __stack_top

	/* copy data to RAM */
	la a0, __data_ram_start
	la a1, __data_ram_end
	la a2, __data_eprom_start

_data_cpy_loop:
	beq a0, a1, _data_cpy_end
	
	lb t0, 0(a2)
	sb t0, 0(a0)

	addi a0, a0, 1
	addi a2, a2, 1
	j _data_cpy_loop

_data_cpy_end:
	/* jump to entry point */
	call main

	/* halt */
	call halt 

.section .text

/* utility functions */

	.global spin
spin:
	j spin
	
	.global halt
halt:
	ecall

	.global debugger
debugger:
	ebreak
	ret
