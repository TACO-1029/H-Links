function pollStatus() {
    fetch('/courses/career-path/build-status?diagnosisId=' + diagnosisId)
        .then(response => response.json())
        .then(data => {
            if (data.status === 'COMPLETED') {
                window.location.href = '/courses/career-path/level-test?diagnosisId=' + diagnosisId;
            } else if (data.status === 'FAILED') {
                alert('AI 시험지 생성에 실패했습니다. 설문 페이지로 돌아갑니다.');
                window.location.href = '/courses/career-path/survey?diagnosisId=' + diagnosisId;
            } else {
                // PENDING or PROCESSING, poll again in 2 seconds
                setTimeout(pollStatus, 2000);
            }
        })
        .catch(error => {
            console.error('Error polling status:', error);
            setTimeout(pollStatus, 2000);
        });
}

// Start polling
setTimeout(pollStatus, 2000);
