# maven-project-inspect
used to get compile message from a maven project with run the compilation

for Java static analysis tools, when do the analysis, the AST of the java file sould be generated first.

This project provide a tool to get the options needed without compilation.

All options needed:
- source file version(or jdk version)
- encoding of java file(mainly UTF-8)
- all source files
- test source files(if also analyze test source files)
- directory of the class files(for some tools analyze .class file)
- all dependencies.
