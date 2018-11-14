# Running FileGeneratorLinux takes two parameters.
# First parameter is a number 1-6, corresponding to a table number in the paper. Replace "1" by the desired set of experiments
# Second parameter is the path to CPLEX.dll. Replace "/ILOG/CPLEX1262/cplex/bin/x86-64_linux" by the path to CPLEX .dll
java -cp ./dist/LagrangeanConic.jar instances.FileGeneratorLinux 3 /opt/ibm/ILOG/CPLEX_Enterprise_Server126/CPLEX_Studio/cplex/bin/x86-64_linux
./run.sh
