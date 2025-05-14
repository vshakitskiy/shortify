# shortify

## Requirements:

- clojure
- java
- leiningen
- docker
- make

## Usage
1. run postgres via docker compose:
```bash
docker compose up -d
```

2. build server and client:
```bash
make build-jars
```

3. run the server and client in different terminals:
```bash
make run-server
make run-client
```
