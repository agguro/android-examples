.text
.align	2
.global     _increment
	.type	_increment, %function
_increment:
	add			r0, r0, 1
	mov			pc, lr