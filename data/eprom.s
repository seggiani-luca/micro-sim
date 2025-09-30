mov $0020, %a // counter 
mov $8000, %b // video pointer

mov $7FFE, %sp // init stack

call _trash

nop

call _loop

nop

_wait_loop:
	jmp _wait_loop

_loop:
	mov %a, %c
	shl $0008, %c
	sto %c, %b

	add $0002, %b // next character in vram

	// loop iteration
	inc %a
	cmp $007f, %a
	jz _post
	jmp _loop
_post:
	ret

_trash:
	push %a
	push %b

	mov $aaaa, %a
	mov $bbbb, %b

	pop %b
	pop %a
	ret
