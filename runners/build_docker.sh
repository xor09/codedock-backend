docker rmi -f runner-cpp
docker rmi -f runner-java
docker rmi -f runner-python
docker build -t runner-cpp -f runners/dockerfiles/Dockerfile-cpp .
docker build -t runner-java -f runners/dockerfiles/Dockerfile-java .
docker build -t runner-python -f runners/dockerfiles/Dockerfile-python .
