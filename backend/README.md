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

# Redisson 락 사용을 위한 Redis 인스턴스 추가

- 기존 Redis 서버(포트 6379)에서 사용 중인 Pub/Sub 기능과 Redisson이 서로 충돌하는 경우 발생
- Redisson 락 사용을 위해 새로운 Redis 서버 생성 필요

```bash
docker run -d --name redis-lock -p 6380:6379 redis:latest
```

- 기본 설정값은 application-dev_db.yml & test_db.yml에서 참조

```yaml
redisson:
  host: localhost
  port: 6380
  password:
```

## Redisson Lock AOP 적용

- 구현 목표: 서비스 레이어 비즈니스 메서드 대상, 충돌 가능성이 존재하는 경우(조회를 제외한 생성, 수정, 삭제 등)
- 적용 방법: @CustomLock을 적용하려는 메서드에 반영하고 key 파라미터를 반드시 작성, 파라미터는 총 3가지

```
String key();

long maxWaitTime() default 1000L;

long leaseTime() default 5000L;

TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
```

    - key: 락 적용 시 사용하는 고유키 파라미터, 필수 입력
    - maxWaitTime: 락 획득 실패 시 최대 대기 시간, 이 시간 동안 일정 간격(백오프)으로 락 획득 재시도
    - leaseTime: 락 획득 시 최대 유지 시간, 초과 후 강제 락 해제
    - timeUnit: 사용할 시간의 단위

- key 값은 SPEL 문법으로 작성, 구분 가능한 파라미터가 반드시 적용되어야 함
- 적용 예시

```java

public class EntityService {
    @CustomLock(key = "'entityA:' + #aId + '-entityB:' + #bId")
    @Transactional
    public boolean modify(final Long aId, final Long bId) {
        // ...
    }
} 
```

- 예시 기준으로 aId가 1이고, bId가 2일 때 생성되는 최종 락 키값은 "modify:entityA:1-entityB:2"가 됨
- 생성된 키로 락 객체 생성 및 락 적용(lock)이 되면 해당 락을 획득한 스레드가 작업을 종료할 때까지 같은 키 값으로 접근이 불가능함
    - 클라이언트 N개의 요청 중 2개 이상의 요청이 aId가 1, bId가 2인 경우 최초로 락을 획득한 스레드가 작업을 종료할 때까지 같은 키 값을 갖는 나머지 스레드들은 대기
    - 락 획득에 실패한 경우 또는 락을 획득한 스레드가 일정 시간 이상 락을 반납하지 못하는 경우 무한 대기를 방지하기 위해 maxWaitTime과 leaseTime을 사용
- 작업이 완료되면 락을 반납(unlock)하고 대기 중인 키가 같은 다른 스레드 중 하나가 락을 획득하고 작업을 수행함