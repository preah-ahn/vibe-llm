-- docker-entrypoint-initdb.d 스크립트: 컨테이너 최초 기동(빈 데이터 디렉터리) 시에만 실행된다.
-- pgdata 볼륨이 이미 존재하면 재실행되지 않으므로, 다시 적용하려면 볼륨을 삭제해야 한다.

DROP DATABASE IF EXISTS llm;
CREATE DATABASE llm;
