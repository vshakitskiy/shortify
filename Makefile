run-server:
	java -jar build/shortify-server.jar

run-client:
	java -jar build/shortify-client.jar

build-jars:
	rm -r build && mkdir build && \
	lein package-server && mv target/shortify-server.jar build/ && \
	lein package-client && mv target/shortify-client.jar build/