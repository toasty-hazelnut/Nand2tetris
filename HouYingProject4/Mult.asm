// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)
//
// This program only needs to handle arguments that satisfy
// R0 >= 0, R1 >= 0, and R0*R1 < 32768.

// Put your code here.

    @R2
    M=0     // RAM[R2]=0. initialize RAM[R2] to 0
    @i
    M=0     // i=0. initialize i to 0

// use a loop with RAM[R1] iterations to do the multiplication. In each iteration, add RAM[R0] to the result
(LOOP)      
    @i
    D=M     // D=i
    @R1
    D=D-M   // D=i-RAM[R1]
    @END
    D;JGE   // if (i-RAM[R1]) >= 0, jump to END
    
    @R0
    D=M     // D=RAM[R0]
    @R2
    M=M+D   // RAM[R2]=RAM[R2]+RAM[R0]
    
    @i
    M=M+1   // i++
    
    @LOOP
    0;JMP   // go to LOOP
    
(END)
    @END
    0;JMP   // infinite loop to "terminate" the execution