PushD		addr2
PStack
PushD		format-int
Printf
Halt
#
DLabel		str1
DataC		104						// h
DataC		105						// i
DataC		0
DLabel		str2
DataC		110						// n
DataC		111						// o
DataC		0
DLabel 		format-int
DataC		37						// %
DataC		100						// d
DataC		0						// <null>
DLabel		format-string
DataC		37						// %
DataC		115						// s
DataC		0						// <null>
DLabel		addr1
DataD		str1
DLabel		addr2
DataD		str2