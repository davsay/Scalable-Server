<<COMMENT
Note:
- Placeholders are denoted by []; they should be replaced by according values.
- This script is useful to students who are testing their code from machines outside of CS department.
- This script must be placed at <project_root_directory>

Usage:
- Remotely login into any of the CS department machine and run this script.
- ./test-scalability-online.sh <number_of_client_on_each_machine>

How it works:
- This script works same as 'test-scalability.sh' except, instead of opening terminals and displaying logs on terminal, this redirects them to files located at '/tmp/$USER/cs455/HW2-PC'.
- This script is used to start multiple Client processes. 
- This script depends on following 2 things
   - 'machine_list' file
   - Command-line argument given when running this script.
- This script starts (number_of_machines_in_machine_list * number_of_client_on_each_machine) Client processes.
- For ex, your 'machine_list' file has 20 hosts and when you run './test-scalability-online.sh 5', this script starts 20*5 = 100 Client processes.
   - Each machine starts <number_of_client_on_each_machine> Client processes and their outputs will be redirected to '/tmp/$USER/cs455/HW2-PC' directory. You don't have to make this directory. If it doesn't exist, it will be created. Each file in this directory is output of a Client process.
- Every Client process will be started by remotely logging into a machine, creating the logfile and running 'java' command.
- If you want to close Client processes, please refer 'stop-clients.sh' and use it carefully as it kills all java processes.
COMMENT

CLASSES=<project_root_directory>
SCRIPT="cd $CLASSES;
java -cp . cs455.scaling.client.Client 3000 10"

#$1 is the command-line argument
for ((j=1; j<=$1; j++));
do
	for i in `cat machine_list`
	do
		echo 'logging into '$i
		FOLDER="/tmp/$USER/cs455/HW2-PC"
		FILE="$FOLDER/$j"
		ssh $i "mkdir -p $FOLDER;touch $FILE;$SCRIPT$FILE &"
	done
	eval $COMMAND &
done
