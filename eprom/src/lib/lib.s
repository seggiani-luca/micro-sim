	.section .start
	.extern main 

	.extern __init_array_start
	.extern __init_array_end

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

	/* call static constructors */
	la t0, __init_array_start
	la s0, __init_array_end

_static_const_loop:
	beq t0, s0, _static_const_end
	lw t2, 0(t0)
	jalr t2
	addi t0, t0, 4
	j _static_const_loop

_static_const_end:

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
