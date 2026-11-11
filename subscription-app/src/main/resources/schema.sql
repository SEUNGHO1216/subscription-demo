-- 스케줄러 락(ShedLock) 테이블. 엔티티가 아니라 ddl-auto 로 생기지 않으므로 기동 시 만든다.
CREATE TABLE IF NOT EXISTS shedlock (
  name       VARCHAR(64)  NOT NULL,
  lock_until TIMESTAMP(3) NOT NULL,
  locked_at  TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  locked_by  VARCHAR(255) NOT NULL,
  PRIMARY KEY (name)
);
