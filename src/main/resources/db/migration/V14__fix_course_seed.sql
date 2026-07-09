-- V13 더미 데이터의 오탈자 및 외래키 매핑 오류 수정
DECLARE
  v_course_id NUMBER;
  v_chapter_id NUMBER;
BEGIN
  -- 1. 성공적인 협업을 위한 팀워크 빌딩 코스의 설명 오탈자 수정 ('창너을' -> '창출을')
  UPDATE COURSE
  SET DESCRIPTION = '효율적인 팀워크와 조직 내 협업 시너지 창출을 위한 실무 가이드'
  WHERE COURSE_TITLE = '성공적인 협업을 위한 팀워크 빌딩' AND CATEGORY_TYPE = 'CAREER_HIGH';

  -- 2. 기획/UX 코스의 사용자 테스트 챕터 스킬 매핑 오류 수정 (CHAPTER_ID에 v_course_id가 들어간 부분 삭제 후 올바른 v_chapter_id 매핑)
  SELECT COURSE_ID INTO v_course_id
  FROM COURSE
  WHERE COURSE_TITLE = 'UX/UI 중심의 비즈니스 서비스 기획 실무' AND CATEGORY_TYPE = 'CAREER_HIGH';

  SELECT CHAPTER_ID INTO v_chapter_id
  FROM COURSE_CHAPTER
  WHERE COURSE_ID = v_course_id AND CHAPTER_TITLE = '사용자 테스트(UT) 설계 및 피드백 반영 기법';

  -- 잘못 매핑된 행 제거 (CHAPTER_ID 대신 COURSE_ID가 들어간 것)
  DELETE FROM CHAPTER_SKILL
  WHERE CHAPTER_ID = v_course_id AND SKILL_ID = 1401;

  -- 올바른 챕터 ID로 등록 (존재하지 않는 경우에만 삽입)
  INSERT INTO CHAPTER_SKILL (CHAPTER_SKILL_ID, CHAPTER_ID, SKILL_ID, WEIGHT, COVERAGE_LEVEL, COVERAGE_REASON)
  SELECT CHAPTER_SKILL_SEQ.NEXTVAL, v_chapter_id, 1401, 1.0, 'BASIC', '사용자 테스트 설계 및 피드백 반영 방법을 학습합니다.'
  FROM DUAL
  WHERE NOT EXISTS (
    SELECT 1 FROM CHAPTER_SKILL WHERE CHAPTER_ID = v_chapter_id AND SKILL_ID = 1401
  );
END;
/
