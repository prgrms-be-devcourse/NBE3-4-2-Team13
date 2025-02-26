# NBE3-4-2-Team13

## 서비스 시작하기

### 1. Redis 설정
이 프로젝트는 Redis를 사용하고 있습니다. Docker를 통해 Redis를 설치하고 실행해주세요.

#### 1. Redis 이미지 다운로드
아래 명령어로 Docker에서 Redis 이미지를 다운로드합니다.
```bash
docker pull redis:latest
```
#### 2. Redis 컨테이너 실행
아래 명령어로 Redis 컨테이너를 실행합니다.
```bash
docker run -d --name redis-server -p 6379:6379 redis:latest
```
#### 3. Redis 실행 확인 (선택 사항)
Redis 컨테이너가 정상적으로 실행 중인지 확인하려면 아래 명령어를 사용하세요.
```bash
docker ps
```
#### 4. Redis CLI 접속 (선택 사항)
Redis CLI에 접속하려면 아래 명령어를 사용하세요.
```bash
docker exec -it redis-server redis-cli
```
CLI에 접속 후, 간단한 명령어로 Redis가 정상 작동하는지 확인할 수 있습니다.
```bash
ping
```
응답으로 pong이 표시되면 정상 작동 중입니다.

## Kafka 컨테이너 설치
설정 파일은 프로젝트 디렉토리의 docker-compose.yml 이 있습니다.

고로 아래 명령어를 통해 컨테이너를 만들어주시면 됩니다.
```bash
docker-compose up 
```

만약 명령어로 실행할 때 실행 로그를 안보고 싶다 ? 면 -d를 붙여주세요.
```bash
docker-compose up -d
```

## 주의사항 
- Kafka 에서 9092 포트를 사용하는데, 간혹 사용중인 포트라고 뜨는 경우가 있습니다.
- 고로 되도록이면 Kafka 컨테이너 먼저 실행하는 것을 추천드립니다...