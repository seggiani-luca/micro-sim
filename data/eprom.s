_program:
	call _subprogram
	mov $0000, %sp
	add %a, %ip

_subprogram:
	sub %ip, %sp
	call _program
