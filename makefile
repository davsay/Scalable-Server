all: clean compile
	echo -e '[INFO] Done!'

clean:
	echo -e '[INFO] Cleaning Up'
	-rm -rf cs455/scaling/**/*.class Sahud_David_ASG2.tar

target:
	-rm *.tar
	tar -czvf Sahud_David_HW2-PC.tar * --exclude=*.class

compile:
	echo -e '[INFO] Compiling the Source'
	javac cs455/scaling/**/*.java