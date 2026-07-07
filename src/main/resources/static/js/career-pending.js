let pollAttempts = 0;
const maxAttempts = 30; // Max 30 attempts
const baseDelay = 2000; // Base delay: 2 seconds
const pendingTitles = [
    '테스트 문항을 구성하고 있습니다.',
    '난이도에 맞춰 문제를 조절 중입니다.',
    '최적의 학습 경험을 준비하고 있습니다.',
    '잠시만 기다려 주세요, 곧 시작합니다!'
];

function rotatePendingTitle() {
    const titleElement = document.querySelector('.pending-title');
    if (!titleElement) {
        return;
    }

    let currentTitleIndex = 0;

    setInterval(() => {
        currentTitleIndex = (currentTitleIndex + 1) % pendingTitles.length;
        titleElement.textContent = pendingTitles[currentTitleIndex];
    }, 2200);
}

function pollStatus() {
    pollAttempts++;
    if (pollAttempts > maxAttempts) {
        alert('AI 시험지 생성 시간이 너무 오래 걸립니다. 설문조사 페이지로 이동하여 다시 시도해 주세요.');
        window.location.href = '/courses/career-path/survey?diagnosisId=' + diagnosisId;
        return;
    }

    fetch('/courses/career-path/build-status?diagnosisId=' + diagnosisId)
        .then(response => response.json())
        .then(data => {
            if (data.status === 'COMPLETED') {
                window.location.href = '/courses/career-path/level-test?diagnosisId=' + diagnosisId;
            } else if (data.status === 'FAILED') {
                alert('AI 시험지 생성에 실패했습니다. 설문 페이지로 돌아갑니다.');
                window.location.href = '/courses/career-path/survey?diagnosisId=' + diagnosisId;
            } else {
                // Calculate backoff: increase delay slightly for subsequent polls (up to max 5 seconds)
                const nextDelay = Math.min(baseDelay + (pollAttempts * 200), 5000);
                setTimeout(pollStatus, nextDelay);
            }
        })
        .catch(error => {
            console.error('Error polling status:', error);
            // Apply backoff on fetch errors (up to max 10 seconds)
            const errorDelay = Math.min(baseDelay * Math.pow(1.5, Math.min(pollAttempts, 6)), 10000);
            setTimeout(pollStatus, errorDelay);
        });
}

rotatePendingTitle();

// Start polling
setTimeout(pollStatus, baseDelay);
