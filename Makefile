all: run

compile:
	mvn package -Dmaven.test.skip

run: compile
	java -jar server/target/server-jar-with-dependencies.jar

format:
	find . -name "*.java" -type f -print | xargs google-java-format --replace
