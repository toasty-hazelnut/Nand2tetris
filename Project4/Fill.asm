// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// Put your code here.

    @SCREEN 
    D=A         // D=SCREEN
    @ptr 
    M=D         // initialize ptr. ptr=SCREEN

(LOOP)
    @KBD 
    D=M         // D=RAM[KBD]
    @NOTPRESS
    D;JEQ       // if RAM[KBD]==0, i.e. no key is pressed, jump to NOTPRESS

// pressed
    @ptr
    A=M         // A=ptr
    D=0         // initialize D to 0
    M=!D        // M is RAM[ptr]. write !D to RAM[ptr], to fill RAM[ptr] black

    D=A+1       // D=ptr+1
    @KBD    
    D=D-A       // D=ptr+1-KBD
    @LOOP       
    D;JGE       // if (ptr+1-KBD) >= 0, jump to LOOP; otherwise, ptr++ and then jump to LOOP

    @ptr
    M=M+1       // ptr++
    @LOOP
    0;JMP       // jump to LOOP

// not pressed
(NOTPRESS)
    @ptr
    A=M         // A=ptr
    M=0         // M is RAM[ptr]. write 0 to RAM[ptr], to fill it white

    D=A         // D=ptr
    @SCREEN
    D=D-A       // D=ptr-SCREEN
    @LOOP       
    D;JLE       // if (ptr-SCREEN) <= 0, jump to LOOP; otherwise, ptr-- and then jump to LOOP

    @ptr
    M=M-1       // ptr--
    @LOOP
    0;JMP       // jump to LOOP