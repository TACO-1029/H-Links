// 수강현황 목록 필터링 스크립트
function filterCourses(status, btnElement) {
    // 액티브 버튼 스타일 교체
    const buttons = document.querySelectorAll('.filter-btn');
    buttons.forEach(btn => btn.classList.remove('is-active'));
    btnElement.classList.add('is-active');

    // 카드들 필터링
    const cards = document.querySelectorAll('.course-item-card');
    cards.forEach(card => {
        const cardStatus = card.getAttribute('data-status');
        if (status === 'ALL') {
            card.style.display = 'flex';
        } else if (status === cardStatus) {
            card.style.display = 'flex';
        } else {
            card.style.display = 'none';
        }
    });
}

// 오답노트 모달 핸들러
function openQuizModal(cardElement) {
    const questionText = cardElement.getAttribute('data-question');
    const explanation = cardElement.getAttribute('data-explanation');
    const chapterTitle = cardElement.getAttribute('data-chapter');
    const submitted = cardElement.getAttribute('data-submitted');
    const correct = cardElement.getAttribute('data-correct');

    document.getElementById('modalChapterTitle').innerText = chapterTitle || '퀴즈 상세 정보';
    document.getElementById('modalQuestionText').innerText = questionText || '질문 내용이 없습니다.';
    document.getElementById('modalSubmittedOption').innerText = submitted || '제출한 오답이 없습니다.';
    document.getElementById('modalCorrectOption').innerText = correct || '등록된 정답 선지가 없습니다.';
    document.getElementById('modalExplanationText').innerText = explanation || '등록된 오답 해설이 없습니다.';

    document.getElementById('quizModal').classList.add('is-open');
}

function closeQuizModal() {
    document.getElementById('quizModal').classList.remove('is-open');
}

// 오답노트 전용 강의별 그룹 렌더링
function groupWrongNotesByCourse() {
    const wrongNotesPage = document.querySelector('.wrong-notes-page');
    if (!wrongNotesPage) {
        return;
    }

    const noteGrid = wrongNotesPage.querySelector('.quiz-grid');
    if (!noteGrid) {
        return;
    }

    const cards = Array.from(noteGrid.querySelectorAll(':scope > .quiz-card'));
    if (cards.length === 0) {
        return;
    }

    const groups = new Map();
    cards.forEach(card => {
        const titleElement = card.querySelector('.quiz-course-title');
        const courseTitle = card.dataset.courseTitle || titleElement?.textContent?.trim() || '강의명 없음';

        if (!groups.has(courseTitle)) {
            groups.set(courseTitle, []);
        }
        groups.get(courseTitle).push(card);
    });

    const groupedContent = document.createDocumentFragment();
    groups.forEach((groupCards, courseTitle) => {
        const group = document.createElement('section');
        group.className = 'wrong-note-course-group';

        const header = document.createElement('div');
        header.className = 'wrong-note-course-header';

        const title = document.createElement('h3');
        title.className = 'wrong-note-course-title';
        title.textContent = courseTitle;

        const count = document.createElement('span');
        count.className = 'wrong-note-course-count';
        count.textContent = `${groupCards.length}개`;

        const cardGrid = document.createElement('div');
        cardGrid.className = 'wrong-note-card-grid';

        groupCards.forEach(card => cardGrid.appendChild(card));
        header.append(title, count);
        group.append(header, cardGrid);
        groupedContent.appendChild(group);
    });

    noteGrid.replaceChildren(groupedContent);
}

document.addEventListener('DOMContentLoaded', groupWrongNotesByCourse);

// 강의 상세 모달 핸들러
function openCourseDetailModal(btnElement) {
    const title = btnElement.getAttribute('data-title');
    const category = btnElement.getAttribute('data-category');
    const appno = btnElement.getAttribute('data-appno');
    const lastplay = btnElement.getAttribute('data-lastplay');
    const completion = btnElement.getAttribute('data-completion');
    const type = btnElement.getAttribute('data-type');

    document.getElementById('detailCourseTitle').innerText = title || '강의 상세 정보';
    document.getElementById('detailCategory').innerText = category || '-';
    document.getElementById('detailAppNo').innerText = appno || '-';
    document.getElementById('detailLastPlay').innerText = lastplay || '-';
    document.getElementById('detailCompletion').innerText = completion || '-';
    document.getElementById('detailType').innerText = type || '-';

    document.getElementById('courseDetailModal').classList.add('is-open');
}

function closeCourseDetailModal() {
    document.getElementById('courseDetailModal').classList.remove('is-open');
}

// 모달 바깥 영역 클릭시 닫기
window.onclick = function(event) {
    const quizModal = document.getElementById('quizModal');
    const detailModal = document.getElementById('courseDetailModal');
    if (event.target === quizModal) {
        closeQuizModal();
    }
    if (event.target === detailModal) {
        closeCourseDetailModal();
    }
}
