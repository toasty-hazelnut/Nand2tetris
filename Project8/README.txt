How to compile and run this program:

This is a Java program.

To compile it, be in the src directory where the source code resides, and run "javac *.java".

To run the program, be in the src directory and use "java Project8 <path of the folder>". The program
accepts one command line argument, which is the path of the folder containing .vm files to be translated.

P.S. This program doesn't recursively search the given folder. It supposes that all .vm files are directly in
the folder. 


Functionality:

This program is a full-scale Virtual Machine Language Translator, able to handle stack arithmetic commands, 
memory access commands, program flow and function-calling commands of the VM language. It translates all .vm 
files in a given folder into a single .asm file. The output file <folder name>.asm will be in the given folder 
and have the same filename as the folder.

