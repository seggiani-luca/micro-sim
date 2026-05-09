# start section, used for start routine
.section .start

.extern main 
.global _start

# start routine
_start:

	# init stack top
	la sp, __stack_top

	# copy EPROM data to RAM
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

	# call C++ static constructors
	la s0, __init_array_start
	la s1, __init_array_end

_static_const_loop:
	beq s0, s1, _static_const_end
	lw t2, 0(s0)
	jalr t2
	addi s0, s0, 4
	j _static_const_loop

_static_const_end:

	# jump to entry point
	call main 

	# on return from entry point, halt the system
	call halt 

# text section, used for utility functions
.section .text
.global spin
.global halt
.global debugger

# spins the processor indefinitely
spin:
	j spin

# halts the system
halt:
	ecall

# calls the debugger
debugger:
	ebreak
	ret
