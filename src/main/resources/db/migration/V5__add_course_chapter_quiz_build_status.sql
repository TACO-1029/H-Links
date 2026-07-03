/* V5__add_course_chapter_quiz_build_status.sql */

ALTER TABLE COURSE_CHAPTER ADD (
    QUIZ_BUILD_STATUS VARCHAR2(30) DEFAULT 'PENDING' NOT NULL
    );

ALTER TABLE COURSE_CHAPTER
    ADD CONSTRAINT CK_CHAPTER_QUIZ_BUILD_STATUS
        CHECK (QUIZ_BUILD_STATUS IN (
                                     'PENDING',
                                     'PROCESSING',
                                     'COMPLETED',
                                     'FAILED'
            ));

COMMIT;