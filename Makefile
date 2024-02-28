all: run

compile:
	mvn package -Dmaven.test.skip

run: compile
	java -jar server/target/server-jar-with-dependencies.jar
