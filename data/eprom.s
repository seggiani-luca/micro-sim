mov $0020, %a // counter 
mov $8000, %b // video pointer

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

_wait_loop:
	jmp _wait_loop

hlt
