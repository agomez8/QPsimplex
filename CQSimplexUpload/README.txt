GENERATE_SCRIPT DESCRIPTION

There are two scripts files: generateScript.bat for Windows, and generateScript.sh for Linux.

To use, edit the file generateScript. There are two parameters:
- a number, which corresponds to a Table in the paper. For example, using "1" will replicate the experiments shown in Table 1 of the paper.
- a path to CPLEX 12.6.2 .dll. 

The first command in the file would generate another file, called run.bat/run.sh, which contains the commands for running the required sets of experiments. The second commands just runs the newly created file.

Note for Linux users: you may need to set permissions for files generateScript.sh and run.sh before being able to run.


RUN DESCRIPTION

Each line of this file (except the last one) corresponds a single instance run. Each call to execute.Main receives 10 parameters.

Parameters:
1: Size. Corresponds to the size of the grid for path instances, and number of variables for cardinality instances.
2: Value of the nonlinearity parameter Omega.
3: Rank r of the matrix Sigma.
4: A parameter controlling the diagonal component of the matrix Sigma. Set to 1 in all experiments.
5: Value of the sparsity parameter alpha.
6: Has value true if the incidence matrix F has only nonnegative entries, and false otherwise. Set to false in all experiments.
7: Seed of the random number generator.
8: Integer number corresponding to the instance: 1 corresponds to path instances, 2 corresponds to cardinality instances.
9: Algorithm used. Negative values correspond to algorithms for convex instances, and non-negative values correspond to algorithms for discrete instances. Possible values:
	- -1: Algorithm 1, initializing t_0:=1. (not used)
	- -2: ALG2.
	- -3: BAR.
	- -4: ALG1.
	- -5: Classic Bisection algorithm. (not used)
	- 0: CXD.
	- 1: CXLPE.
	- 2: CXSP.
	- 3: BBA1.
	- 4: BBBR.
	- 5: CXLP.
10: Precision parameter. If the value is 0, uses default precision parameters.

The result of each run will be stored in /results/resultsObj.xml. 

The last line of the file run, "java -cp ./dist/LagrangeanConic.jar results.Printer", will parse the file resultsObj.xml, and create a file resultsPrinted.csv, with a summary of the results.

