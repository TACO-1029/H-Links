let pollAttempts = 0;
const maxAttempts = 30; // Max 30 attempts
const baseDelay = 2000; // Base delay: 2 seconds

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

// Start polling
setTimeout(pollStatus, baseDelay);
