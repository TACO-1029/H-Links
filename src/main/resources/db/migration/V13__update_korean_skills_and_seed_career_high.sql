-- [1] 기존 영문 스킬 명칭을 한글로 업데이트
UPDATE SKILL SET SKILL_NAME = '협업' WHERE SKILL_ID = 1001;
UPDATE SKILL SET SKILL_NAME = '리더십관리' WHERE SKILL_ID = 1002;
UPDATE SKILL SET SKILL_NAME = '글로벌' WHERE SKILL_ID = 1003;
UPDATE SKILL SET SKILL_NAME = '창의기획' WHERE SKILL_ID = 1004;
UPDATE SKILL SET SKILL_NAME = '팀워크' WHERE SKILL_ID = 1101;
UPDATE SKILL SET SKILL_NAME = '프레젠테이션' WHERE SKILL_ID = 1102;
UPDATE SKILL SET SKILL_NAME = '커뮤니케이션' WHERE SKILL_ID = 1103;
UPDATE SKILL SET SKILL_NAME = '리더십' WHERE SKILL_ID = 1201;
UPDATE SKILL SET SKILL_NAME = '프로젝트 관리' WHERE SKILL_ID = 1202;
UPDATE SKILL SET SKILL_NAME = '언어' WHERE SKILL_ID = 1301;
UPDATE SKILL SET SKILL_NAME = '글로벌 IT' WHERE SKILL_ID = 1302;
UPDATE SKILL SET SKILL_NAME = '기획/UX' WHERE SKILL_ID = 1401;
UPDATE SKILL SET SKILL_NAME = '최신기술' WHERE SKILL_ID = 1402;

-- [2] 해당 skill들의 커리어하이 더미데이터 강의들 만들기
DECLARE
  v_cnt NUMBER;
  v_course_id NUMBER;
  v_chapter_id NUMBER;
BEGIN

  -- [1] 팀워크 (SKILL_ID: 1101)
  SELECT COUNT(*) INTO v_cnt FROM COURSE WHERE COURSE_TITLE = '성공적인 협업을 위한 팀워크 빌딩' AND CATEGORY_TYPE = 'CAREER_HIGH';
  IF v_cnt = 0 THEN
    INSERT INTO COURSE (COURSE_ID, CREATED_BY, CATEGORY_TYPE, COURSE_TYPE, COURSE_TITLE, DESCRIPTION, INSTRUCTOR_NAME, TOTAL_DURATION_TIME, THUMBNAIL_URL, CREATED_AT)
    VALUES (COURSE_SEQ.NEXTVAL, NULL, 'CAREER_HIGH', 'ONLINE', '성공적인 협업을 위한 팀워크 빌딩', '효율적인 팀워크와 조직 내 협업 시너지 창출을 위한 실무 가이드', '김협업', 8, '/images/course/ch_on_teamwork.png', SYSDATE)
    RETURNING COURSE_ID INTO v_course_id;

    INSERT INTO ONLINE_COURSE (COURSE_ID, COURSE_MATERIAL_URL, STATUS)
    VALUES (v_course_id, '/files/course/ch_on_teamwork_material.pdf', 'OPEN');

    INSERT INTO COURSE_SKILL (COURSE_SKILL_ID, COURSE_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (COURSE_SKILL_SEQ.NEXTVAL, v_course_id, 1101, 1.0, 'BASIC', '팀워크 향상을 위한 기초 이론과 적용 방법을 학습합니다.');

    -- 챕터 1
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '팀워크의 이해와 역할 분담', 1, '/videos/courses/'||v_course_id||'/chapters/1', '/storage/courses/'||v_course_id||'/chapters/1/lecture.mp4', 'chapter_01.mp4', NULL, 7200, '팀워크의 의의와 효율적인 R&R 설정 기법을 설명합니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1101, 1.0, 'BASIC', '역할 분담의 기초 개념을 다룹니다.');

    -- 챕터 2
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '조직 내 갈등 해결과 협력 방안', 2, '/videos/courses/'||v_course_id||'/chapters/2', '/storage/courses/'||v_course_id||'/chapters/2/lecture.mp4', 'chapter_02.mp4', NULL, 10800, '조직 내 발생할 수 있는 소통 장애와 갈등 극복 방안을 설명합니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1101, 1.0, 'BASIC', '갈등 해결 및 소통 기초를 설명합니다.');

    -- 챕터 3
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '실전 팀 빌딩 워크숍 사례', 3, '/videos/courses/'||v_course_id||'/chapters/3', '/storage/courses/'||v_course_id||'/chapters/3/lecture.mp4', 'chapter_03.mp4', NULL, 10800, '다양한 기업에서 활용되는 팀 빌딩 활동과 모범 사례를 분석합니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1101, 1.0, 'BASIC', '워크숍 및 협업 실습 사례를 다룹니다.');
  END IF;


  -- [2] 프레젠테이션 (SKILL_ID: 1102)
  SELECT COUNT(*) INTO v_cnt FROM COURSE WHERE COURSE_TITLE = '설득력 있는 비즈니스 프레젠테이션' AND CATEGORY_TYPE = 'CAREER_HIGH';
  IF v_cnt = 0 THEN
    INSERT INTO COURSE (COURSE_ID, CREATED_BY, CATEGORY_TYPE, COURSE_TYPE, COURSE_TITLE, DESCRIPTION, INSTRUCTOR_NAME, TOTAL_DURATION_TIME, THUMBNAIL_URL, CREATED_AT)
    VALUES (COURSE_SEQ.NEXTVAL, NULL, 'CAREER_HIGH', 'ONLINE', '설득력 있는 비즈니스 프레젠테이션', '기획부터 발표까지, 청중의 마음을 움직이는 프레젠테이션 설계', '발표왕', 6, '/images/course/ch_on_presentation.png', SYSDATE)
    RETURNING COURSE_ID INTO v_course_id;

    INSERT INTO ONLINE_COURSE (COURSE_ID, COURSE_MATERIAL_URL, STATUS)
    VALUES (v_course_id, '/files/course/ch_on_presentation_material.pdf', 'OPEN');

    INSERT INTO COURSE_SKILL (COURSE_SKILL_ID, COURSE_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (COURSE_SKILL_SEQ.NEXTVAL, v_course_id, 1102, 1.0, 'BASIC', '프레젠테이션의 기초적인 구조화 및 전달력을 향상시킵니다.');

    -- 챕터 1
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '설득력 있는 슬라이드 기획 및 메시지 구성', 1, '/videos/courses/'||v_course_id||'/chapters/1', '/storage/courses/'||v_course_id||'/chapters/1/lecture.mp4', 'chapter_01.mp4', NULL, 7200, '비즈니스 목적에 맞는 발표 기획 프로세스를 다룹니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1102, 1.0, 'BASIC', '메시지 기획 및 핵심 논리 구성을 배웁니다.');

    -- 챕터 2
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '효과적인 시각 자료 제작 기법', 2, '/videos/courses/'||v_course_id||'/chapters/2', '/storage/courses/'||v_course_id||'/chapters/2/lecture.mp4', 'chapter_02.mp4', NULL, 7200, '텍스트 위주가 아닌 직관적인 도해와 디자인 설계 방법을 실습합니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1102, 1.0, 'BASIC', '시각 자료 디자인 및 슬라이드 구성 기법을 배웁니다.');

    -- 챕터 3
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '실전 발표 전략 및 딜리버리 스킬', 3, '/videos/courses/'||v_course_id||'/chapters/3', '/storage/courses/'||v_course_id||'/chapters/3/lecture.mp4', 'chapter_03.mp4', NULL, 7200, '제스처, 시선 처리, 질의응답 대응 방법 등을 익힙니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1102, 1.0, 'BASIC', '발표 태도와 실전 스피치 능력을 훈련합니다.');
  END IF;


  -- [3] 커뮤니케이션 (SKILL_ID: 1103)
  SELECT COUNT(*) INTO v_cnt FROM COURSE WHERE COURSE_TITLE = '조직을 살리는 실전 비즈니스 소통 기술' AND CATEGORY_TYPE = 'CAREER_HIGH';
  IF v_cnt = 0 THEN
    INSERT INTO COURSE (COURSE_ID, CREATED_BY, CATEGORY_TYPE, COURSE_TYPE, COURSE_TITLE, DESCRIPTION, INSTRUCTOR_NAME, TOTAL_DURATION_TIME, THUMBNAIL_URL, CREATED_AT)
    VALUES (COURSE_SEQ.NEXTVAL, NULL, 'CAREER_HIGH', 'ONLINE', '조직을 살리는 실전 비즈니스 소통 기술', '경청과 피드백을 통해 협업 효율을 극대화하는 소통 가이드', '소통마스터', 9, '/images/course/ch_on_communication.png', SYSDATE)
    RETURNING COURSE_ID INTO v_course_id;

    INSERT INTO ONLINE_COURSE (COURSE_ID, COURSE_MATERIAL_URL, STATUS)
    VALUES (v_course_id, '/files/course/ch_on_communication_material.pdf', 'OPEN');

    INSERT INTO COURSE_SKILL (COURSE_SKILL_ID, COURSE_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (COURSE_SKILL_SEQ.NEXTVAL, v_course_id, 1103, 1.0, 'BASIC', '협업 효율을 끌어올리는 효과적인 상호 소통 기술을 익힙니다.');

    -- 챕터 1
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '경청의 힘과 비언어적 커뮤니케이션', 1, '/videos/courses/'||v_course_id||'/chapters/1', '/storage/courses/'||v_course_id||'/chapters/1/lecture.mp4', 'chapter_01.mp4', NULL, 10800, '상대방의 의도를 명확히 경청하고 호감을 얻는 비언어적 표현을 배웁니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1103, 1.0, 'BASIC', '경청의 태도와 비언어적 공감 능력을 학습합니다.');

    -- 챕터 2
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '업무 지시 및 보고의 올바른 피드백 방법', 2, '/videos/courses/'||v_course_id||'/chapters/2', '/storage/courses/'||v_course_id||'/chapters/2/lecture.mp4', 'chapter_02.mp4', NULL, 10800, '5W1H에 맞춘 효과적인 지시 요령과 중간 보고 프로세스를 익힙니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1103, 1.0, 'BASIC', '상하 간 보고 및 협력적 피드백을 실습합니다.');

    -- 챕터 3
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '갈등 조율과 협상을 위한 대화법', 3, '/videos/courses/'||v_course_id||'/chapters/3', '/storage/courses/'||v_course_id||'/chapters/3/lecture.mp4', 'chapter_03.mp4', NULL, 10800, '이해관계가 충돌할 때 윈-윈(Win-Win)할 수 있는 조율과 협상 기술을 학습합니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1103, 1.0, 'BASIC', '부서 간 또는 고객 간 협상 역량을 다룹니다.');
  END IF;


  -- [4] 리더십 (SKILL_ID: 1201)
  SELECT COUNT(*) INTO v_cnt FROM COURSE WHERE COURSE_TITLE = '변화와 혁신을 이끄는 현대적 리더십' AND CATEGORY_TYPE = 'CAREER_HIGH';
  IF v_cnt = 0 THEN
    INSERT INTO COURSE (COURSE_ID, CREATED_BY, CATEGORY_TYPE, COURSE_TYPE, COURSE_TITLE, DESCRIPTION, INSTRUCTOR_NAME, TOTAL_DURATION_TIME, THUMBNAIL_URL, CREATED_AT)
    VALUES (COURSE_SEQ.NEXTVAL, NULL, 'CAREER_HIGH', 'ONLINE', '변화와 혁신을 이끄는 현대적 리더십', '팀원의 역량을 극대화하고 동기부여를 이끄는 코칭 중심 리더십 가이드', '리더킴', 10, '/images/course/ch_on_leadership.png', SYSDATE)
    RETURNING COURSE_ID INTO v_course_id;

    INSERT INTO ONLINE_COURSE (COURSE_ID, COURSE_MATERIAL_URL, STATUS)
    VALUES (v_course_id, '/files/course/ch_on_leadership_material.pdf', 'OPEN');

    INSERT INTO COURSE_SKILL (COURSE_SKILL_ID, COURSE_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (COURSE_SKILL_SEQ.NEXTVAL, v_course_id, 1201, 1.0, 'BASIC', '중간 관리자 및 팀장급에게 필요한 리더십 기초 이론을 다룹니다.');

    -- 챕터 1
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '리더십의 패러다임 변화와 서번트 리더십', 1, '/videos/courses/'||v_course_id||'/chapters/1', '/storage/courses/'||v_course_id||'/chapters/1/lecture.mp4', 'chapter_01.mp4', NULL, 10800, '지시형 리더십에서 자율을 보장하는 코칭형 서번트 리더십으로의 전환을 배웁니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1201, 1.0, 'BASIC', '리더십 유형 이해 및 기본 변화 방향을 배웁니다.');

    -- 챕터 2
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '팀 동기부여와 역량 개발 코칭', 2, '/videos/courses/'||v_course_id||'/chapters/2', '/storage/courses/'||v_course_id||'/chapters/2/lecture.mp4', 'chapter_02.mp4', NULL, 10800, '팀원들의 주도적 업무 몰입을 돕기 위한 코칭 모델(GROW) 실전 기법을 학습합니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1201, 1.0, 'BASIC', '동기부여 및 1:1 면담 기법을 학습합니다.');

    -- 챕터 3
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '목표 달성을 위한 리더의 성과 관리', 3, '/videos/courses/'||v_course_id||'/chapters/3', '/storage/courses/'||v_course_id||'/chapters/3/lecture.mp4', 'chapter_03.mp4', NULL, 14400, 'OKR 및 KPI 성과 지표 도출과 공정한 다면 평가 전략을 분석합니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1201, 1.0, 'BASIC', '조직 성과 지표 설정 및 관리 역량을 다룹니다.');
  END IF;


  -- [5] 프로젝트 관리 (SKILL_ID: 1202)
  SELECT COUNT(*) INTO v_cnt FROM COURSE WHERE COURSE_TITLE = '애자일과 워터폴로 완성하는 프로젝트 관리(PM)' AND CATEGORY_TYPE = 'CAREER_HIGH';
  IF v_cnt = 0 THEN
    INSERT INTO COURSE (COURSE_ID, CREATED_BY, CATEGORY_TYPE, COURSE_TYPE, COURSE_TITLE, DESCRIPTION, INSTRUCTOR_NAME, TOTAL_DURATION_TIME, THUMBNAIL_URL, CREATED_AT)
    VALUES (COURSE_SEQ.NEXTVAL, NULL, 'CAREER_HIGH', 'ONLINE', '애자일과 워터폴로 완성하는 프로젝트 관리(PM)', '프로젝트 기획부터 일정 통제, 리스크 대응까지의 라이프사이클 마스터', '피엠조', 12, '/images/course/ch_on_pm.png', SYSDATE)
    RETURNING COURSE_ID INTO v_course_id;

    INSERT INTO ONLINE_COURSE (COURSE_ID, COURSE_MATERIAL_URL, STATUS)
    VALUES (v_course_id, '/files/course/ch_on_pm_material.pdf', 'OPEN');

    INSERT INTO COURSE_SKILL (COURSE_SKILL_ID, COURSE_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (COURSE_SKILL_SEQ.NEXTVAL, v_course_id, 1202, 1.0, 'BASIC', '프로젝트 성격에 맞는 방법론(Agile, Waterfall) 선택 및 관리 프로세스를 학습합니다.');

    -- 챕터 1
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '프로젝트 범위 정의와 WBS 수립', 1, '/videos/courses/'||v_course_id||'/chapters/1', '/storage/courses/'||v_course_id||'/chapters/1/lecture.mp4', 'chapter_01.mp4', NULL, 14400, '요구사항 수집 및 작업 분할 구조(WBS) 생성 방법을 학습합니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1202, 1.0, 'BASIC', 'WBS 수립 및 범위 통제 기초를 학습합니다.');

    -- 챕터 2
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '일정 계획 수립 및 리스크 통제', 2, '/videos/courses/'||v_course_id||'/chapters/2', '/storage/courses/'||v_course_id||'/chapters/2/lecture.mp4', 'chapter_02.mp4', NULL, 14400, '임계경로(CPM) 일정 계획과 위기 상황 완화 계획을 도출합니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1202, 1.0, 'BASIC', '리스크 관리 및 마일스톤 관리를 학습합니다.');

    -- 챕터 3
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '애자일 스크럼과 스프린트 운영 실무', 3, '/videos/courses/'||v_course_id||'/chapters/3', '/storage/courses/'||v_course_id||'/chapters/3/lecture.mp4', 'chapter_03.mp4', NULL, 14400, '스크럼 보드, 데일리 스탠드업, 회고 등 민첩한 프로젝트 운영 요령을 습득합니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1202, 1.0, 'BASIC', '애자일 스프린트 기획 및 운영 실무를 습득합니다.');
  END IF;


  -- [6] 언어 (SKILL_ID: 1301)
  SELECT COUNT(*) INTO v_cnt FROM COURSE WHERE COURSE_TITLE = '글로벌 비즈니스를 위한 실무 비즈니스 영어' AND CATEGORY_TYPE = 'CAREER_HIGH';
  IF v_cnt = 0 THEN
    INSERT INTO COURSE (COURSE_ID, CREATED_BY, CATEGORY_TYPE, COURSE_TYPE, COURSE_TITLE, DESCRIPTION, INSTRUCTOR_NAME, TOTAL_DURATION_TIME, THUMBNAIL_URL, CREATED_AT)
    VALUES (COURSE_SEQ.NEXTVAL, NULL, 'CAREER_HIGH', 'ONLINE', '글로벌 비즈니스를 위한 실무 비즈니스 영어', '이메일, 회의, 협상 등 실제 비즈니스 환경에서 쓰이는 실용 영어 가이드', '존스미스', 8, '/images/course/ch_on_english.png', SYSDATE)
    RETURNING COURSE_ID INTO v_course_id;

    INSERT INTO ONLINE_COURSE (COURSE_ID, COURSE_MATERIAL_URL, STATUS)
    VALUES (v_course_id, '/files/course/ch_on_english_material.pdf', 'OPEN');

    INSERT INTO COURSE_SKILL (COURSE_SKILL_ID, COURSE_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (COURSE_SKILL_SEQ.NEXTVAL, v_course_id, 1301, 1.0, 'BASIC', '업무에서 가장 자주 사용되는 실전 영작문 및 스피킹을 다룹니다.');

    -- 챕터 1
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '세련된 영문 이메일 작성 공식', 1, '/videos/courses/'||v_course_id||'/chapters/1', '/storage/courses/'||v_course_id||'/chapters/1/lecture.mp4', 'chapter_01.mp4', NULL, 7200, '정중한 요청, 불만 제기, 피드백 등 메일 템플릿과 정형화된 비즈니스 톤을 배웁니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1301, 1.0, 'BASIC', '영문 이메일 형식과 핵심 문구를 학습합니다.');

    -- 챕터 2
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '글로벌 화상 회의 핵심 표현과 토론 스킬', 2, '/videos/courses/'||v_course_id||'/chapters/2', '/storage/courses/'||v_course_id||'/chapters/2/lecture.mp4', 'chapter_02.mp4', NULL, 10800, '컨퍼런스 콜 및 줌 회의 시 의사 개진, 발언권 확보, 동의 및 반대 표현을 익힙니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1301, 1.0, 'BASIC', '비즈니스 스피킹과 회의 참여 요령을 학습합니다.');

    -- 챕터 3
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '영문 프레젠테이션 및 보고서 작성 기법', 3, '/videos/courses/'||v_course_id||'/chapters/3', '/storage/courses/'||v_course_id||'/chapters/3/lecture.mp4', 'chapter_03.mp4', NULL, 10800, '차트와 지표를 영어로 매끄럽게 설명하고 한 페이지 제안서를 영작하는 전략을 다룹니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1301, 1.0, 'BASIC', '영문 기획안 및 프레젠테이션 스킬을 체득합니다.');
  END IF;


  -- [7] 글로벌 IT (SKILL_ID: 1302)
  SELECT COUNT(*) INTO v_cnt FROM COURSE WHERE COURSE_TITLE = '글로벌 IT 트렌드와 해외 협업 실무' AND CATEGORY_TYPE = 'CAREER_HIGH';
  IF v_cnt = 0 THEN
    INSERT INTO COURSE (COURSE_ID, CREATED_BY, CATEGORY_TYPE, COURSE_TYPE, COURSE_TITLE, DESCRIPTION, INSTRUCTOR_NAME, TOTAL_DURATION_TIME, THUMBNAIL_URL, CREATED_AT)
    VALUES (COURSE_SEQ.NEXTVAL, NULL, 'CAREER_HIGH', 'ONLINE', '글로벌 IT 트렌드와 해외 협업 실무', '해외 분산 협업 프로세스 이해 및 영미권 IT 트렌드 이해', '글로벌강', 6, '/images/course/ch_on_global_it.png', SYSDATE)
    RETURNING COURSE_ID INTO v_course_id;

    INSERT INTO ONLINE_COURSE (COURSE_ID, COURSE_MATERIAL_URL, STATUS)
    VALUES (v_course_id, '/files/course/ch_on_global_it_material.pdf', 'OPEN');

    INSERT INTO COURSE_SKILL (COURSE_SKILL_ID, COURSE_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (COURSE_SKILL_SEQ.NEXTVAL, v_course_id, 1302, 1.0, 'BASIC', '글로벌 환경에서 소프트웨어 프로젝트를 조율하기 위한 기본 소양을 기릅니다.');

    -- 챕터 1
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '글로벌 IT 비즈니스 생태계 분석', 1, '/videos/courses/'||v_course_id||'/chapters/1', '/storage/courses/'||v_course_id||'/chapters/1/lecture.mp4', 'chapter_01.mp4', NULL, 7200, '실리콘밸리와 유럽 주요 IT 빅테크 기업들의 아웃소싱 구조를 배웁니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1302, 1.0, 'BASIC', '글로벌 테크 생태계 기초를 이해합니다.');

    -- 챕터 2
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '글로벌 분산 팀의 협업 프로세스 및 툴 활용', 2, '/videos/courses/'||v_course_id||'/chapters/2', '/storage/courses/'||v_course_id||'/chapters/2/lecture.mp4', 'chapter_02.mp4', NULL, 7200, '시차가 다른 원격 개발팀과의 Slack, Jira, Notion 활용 협업 모델을 파악합니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1302, 1.0, 'BASIC', '원격 분산 협업 관리 능력을 기릅니다.');

    -- 챕터 3
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '이종 문화 이해와 해외 파트너 커뮤니케이션', 3, '/videos/courses/'||v_course_id||'/chapters/3', '/storage/courses/'||v_course_id||'/chapters/3/lecture.mp4', 'chapter_03.mp4', NULL, 7200, '컨텍스트 기반 문화 차이를 고려한 영미권 및 아시아권 소통 매너를 다룹니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1302, 1.0, 'BASIC', '글로벌 오프쇼어링/니어쇼어링 파트너십을 학습합니다.');
  END IF;


  -- [8] 기획/UX (SKILL_ID: 1401)
  SELECT COUNT(*) INTO v_cnt FROM COURSE WHERE COURSE_TITLE = 'UX/UI 중심의 비즈니스 서비스 기획 실무' AND CATEGORY_TYPE = 'CAREER_HIGH';
  IF v_cnt = 0 THEN
    INSERT INTO COURSE (COURSE_ID, CREATED_BY, CATEGORY_TYPE, COURSE_TYPE, COURSE_TITLE, DESCRIPTION, INSTRUCTOR_NAME, TOTAL_DURATION_TIME, THUMBNAIL_URL, CREATED_AT)
    VALUES (COURSE_SEQ.NEXTVAL, NULL, 'CAREER_HIGH', 'ONLINE', 'UX/UI 중심의 비즈니스 서비스 기획 실무', '사용자 분석부터 와이어프레임 작성 및 서비스 설계 프로세스 가이드', '기획왕', 10, '/images/course/ch_on_ux.png', SYSDATE)
    RETURNING COURSE_ID INTO v_course_id;

    INSERT INTO ONLINE_COURSE (COURSE_ID, COURSE_MATERIAL_URL, STATUS)
    VALUES (v_course_id, '/files/course/ch_on_ux_material.pdf', 'OPEN');

    INSERT INTO COURSE_SKILL (COURSE_SKILL_ID, COURSE_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (COURSE_SKILL_SEQ.NEXTVAL, v_course_id, 1401, 1.0, 'BASIC', '사용자 관점의 기획을 도입하기 위한 기본 설계 단계를 학습합니다.');

    -- 챕터 1
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '사용자 경험(UX) 리서치와 페르소나 정의', 1, '/videos/courses/'||v_course_id||'/chapters/1', '/storage/courses/'||v_course_id||'/chapters/1/lecture.mp4', 'chapter_01.mp4', NULL, 10800, '사용자 인터뷰, 설문조사 방법 및 가상 페르소나 정교화 기법을 다룹니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1401, 1.0, 'BASIC', 'UX 리서치와 사용자 타겟팅 분석 기초를 익힙니다.');

    -- 챕터 2
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '정보 구조(IA) 설계와 와이어프레임 작성', 2, '/videos/courses/'||v_course_id||'/chapters/2', '/storage/courses/'||v_course_id||'/chapters/2/lecture.mp4', 'chapter_02.mp4', NULL, 10800, '사용성 흐름에 기반한 정보 배치 및 손쉬운 목업 제작 실습을 다룹니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1401, 1.0, 'BASIC', '화면 기획서(SB) 및 IA 기획 설계를 배웁니다.');

    -- 챕터 3
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '사용자 테스트(UT) 설계 및 피드백 반영 기법', 3, '/videos/courses/'||v_course_id||'/chapters/3', '/storage/courses/'||v_course_id||'/chapters/3/lecture.mp4', 'chapter_03.mp4', NULL, 14400, '프로토타입 검증 방법 및 UT 결과 피드백을 우선순위에 따라 조율하는 역량을 다룹니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1401, 1.0, 'BASIC', '테스트 및 설계 피드백 반영을 학습합니다.');
  END IF;


  -- [9] 최신기술 (SKILL_ID: 1402)
  SELECT COUNT(*) INTO v_cnt FROM COURSE WHERE COURSE_TITLE = 'IT 트렌드 핵심: 생성형 AI와 블록체인' AND CATEGORY_TYPE = 'CAREER_HIGH';
  IF v_cnt = 0 THEN
    INSERT INTO COURSE (COURSE_ID, CREATED_BY, CATEGORY_TYPE, COURSE_TYPE, COURSE_TITLE, DESCRIPTION, INSTRUCTOR_NAME, TOTAL_DURATION_TIME, THUMBNAIL_URL, CREATED_AT)
    VALUES (COURSE_SEQ.NEXTVAL, NULL, 'CAREER_HIGH', 'ONLINE', 'IT 트렌드 핵심: 생성형 AI와 블록체인', '실무 적용 관점에서 바라본 파괴적 IT 트렌드 완벽 흐름 정리', '최신기술연구원', 8, '/images/course/ch_on_new_tech.png', SYSDATE)
    RETURNING COURSE_ID INTO v_course_id;

    INSERT INTO ONLINE_COURSE (COURSE_ID, COURSE_MATERIAL_URL, STATUS)
    VALUES (v_course_id, '/files/course/ch_on_new_tech_material.pdf', 'OPEN');

    INSERT INTO COURSE_SKILL (COURSE_SKILL_ID, COURSE_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (COURSE_SKILL_SEQ.NEXTVAL, v_course_id, 1402, 1.0, 'BASIC', '급변하는 IT 환경에 대비해 혁신 기술들의 개념과 상용화 흐름을 파악합니다.');

    -- 챕터 1
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '생성형 AI 비즈니스 활용 전략', 1, '/videos/courses/'||v_course_id||'/chapters/1', '/storage/courses/'||v_course_id||'/chapters/1/lecture.mp4', 'chapter_01.mp4', NULL, 10800, 'ChatGPT, Midjourney 등 비즈니스 프로세스 혁신 사례를 다룹니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1402, 1.0, 'BASIC', '생성형 AI 도구와 업무 효율화 사례를 배웁니다.');

    -- 챕터 2
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '블록체인과 Web3 기술 트렌드', 2, '/videos/courses/'||v_course_id||'/chapters/2', '/storage/courses/'||v_course_id||'/chapters/2/lecture.mp4', 'chapter_02.mp4', NULL, 10800, '탈중앙화 금융(DeFi), 대체불가토큰(NFT), 스마트 콘트랙트 동작 원리를 분석합니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1402, 1.0, 'BASIC', '웹3 및 분산원장 핵심 원리를 분석합니다.');

    -- 챕터 3
    INSERT INTO COURSE_CHAPTER (CHAPTER_ID, COURSE_ID, CHAPTER_TITLE, CHAPTER_ORDER, VIDEO_URL, VIDEO_PATH, ORIGINAL_FILE_NAME, FILE_SIZE, DURATION_SECONDS, SUMMARY_TEXT, TRANSCRIPT_TEXT, AI_GENERATED_YN, USE_YN, CREATED_AT)
    VALUES (COURSE_CHAPTER_SEQ.NEXTVAL, v_course_id, '미래 핵심 기술 융합과 실무 적용 사례', 3, '/videos/courses/'||v_course_id||'/chapters/3', '/storage/courses/'||v_course_id||'/chapters/3/lecture.mp4', 'chapter_03.mp4', NULL, 7200, 'AI, 블록체인, IoT가 결합된 신사업 BM과 법적 규제 사항을 학습합니다.', NULL, 'N', 'Y', SYSDATE)
    RETURNING CHAPTER_ID INTO v_chapter_id;

    INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
    VALUES (CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1402, 1.0, 'BASIC', '기술 융합 시나리오 실무 검토 방식을 배웁니다.');
  END IF;

END;
/
