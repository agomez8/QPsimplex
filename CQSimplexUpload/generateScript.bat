REM Running FileGeneratorWindows takes two parameters.
REM First parameter is a number 1-6, corresponding to a table number in the paper. Replace "1" by the desired set of experiments
REM Second parameter is the path to CPLEX.dll. Replace "C:\Program Files\IBM\ILOG\CPLEX_Studio1262\cplex\bin\x64_win64" by the path to CPLEX .dll
java -cp ./dist/LagrangeanConic.jar instances.FileGeneratorWindows 1 "C:\Program Files\IBM\ILOG\CPLEX_Studio1262\cplex\bin\x64_win64"
run.bat