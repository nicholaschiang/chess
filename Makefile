all: run

compile:
	mvn package -Dmaven.test.skip

run: compile
	java -jar server/target/server-jar-with-dependencies.jar

format:
	find . -name "*.java" -type f -print | xargs google-java-format --replace

db:
	docker run --name chess -p 3306:3306 -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=chess -d mysql
