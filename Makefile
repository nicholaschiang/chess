all: run

clean:
	mvn clean

compile: clean
	mvn package -Dmaven.test.skip

run: compile
	java -jar server/target/server-jar-with-dependencies.jar

start-server:
	java -jar server/target/server-jar-with-dependencies.jar

start-client:
	java -jar client/target/client-jar-with-dependencies.jar http://localhost:$(server)

format:
	find . -name "*.java" -type f -print | xargs google-java-format --replace

test:
	mvn test -Dtest="WebSocketTests#validMove" -Dsurefire.failIfNoSpecifiedTests=false

db:
	docker run --name chess -p 3306:3306 -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=chess -d mysql
