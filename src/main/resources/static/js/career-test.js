document.addEventListener('DOMContentLoaded', () => {
    const optionItems = document.querySelectorAll('.option-item');
    const form = document.getElementById('levelTestForm');
    
    if (!form) return;
    
    const diagnosisIdInput = form.querySelector('input[name="diagnosisId"]');
    const diagnosisId = diagnosisIdInput ? diagnosisIdInput.value : 'default';
    const storageKey = `career_level_test_answers_${diagnosisId}`;

    // Load saved answers from localStorage
    let savedAnswers = {};
    const savedData = localStorage.getItem(storageKey);
    if (savedData) {
        try {
            savedAnswers = JSON.parse(savedData);
        } catch (e) {
            console.error('Failed to parse saved answers from localStorage', e);
        }
    }

    // Shared selection function for click and keyboard
    const selectOption = (item) => {
        const qId = item.getAttribute('data-q-id');
        const optId = item.getAttribute('data-opt-id');

        // Find other options of the same question and remove active class and set aria-checked false
        const sisterOptions = document.querySelectorAll(`.option-item[data-q-id="${qId}"]`);
        sisterOptions.forEach(opt => {
            opt.classList.remove('active');
            opt.setAttribute('aria-checked', 'false');
        });

        // Add active class and set aria-checked true to selected option
        item.classList.add('active');
        item.setAttribute('aria-checked', 'true');

        // Update the hidden input value
        const input = document.getElementById('selected-option-input-' + qId);
        if (input) {
            input.value = optId;
        }

        // Save to localStorage
        savedAnswers[qId] = optId;
        localStorage.setItem(storageKey, JSON.stringify(savedAnswers));
    };

    // Restore selected answers visually and in hidden inputs
    Object.entries(savedAnswers).forEach(([qId, optId]) => {
        const optionItem = document.querySelector(`.option-item[data-q-id="${qId}"][data-opt-id="${optId}"]`);
        if (optionItem) {
            optionItem.classList.add('active');
            optionItem.setAttribute('aria-checked', 'true');
            const input = document.getElementById('selected-option-input-' + qId);
            if (input) {
                input.value = optId;
            }
        }
    });

    // Handle option click and keyboard Enter/Space selection
    optionItems.forEach(item => {
        item.addEventListener('click', () => {
            selectOption(item);
        });

        item.addEventListener('keydown', (e) => {
            if (e.key === ' ' || e.key === 'Spacebar' || e.key === 'Enter') {
                e.preventDefault(); // Prevent space key from scrolling the page
                selectOption(item);
            }
        });
    });

    // Handle form submission
    form.addEventListener('submit', (e) => {
        const hiddenInputs = document.querySelectorAll('input[name="selectedOptionIds"]');
        const submitButton = form.querySelector('.btn-submit-test');
        const pendingModal = document.getElementById('levelTestPendingModal');
        let allAnswered = true;
        hiddenInputs.forEach((input) => {
            if (!input.value) {
                allAnswered = false;
            }
        });

        if (!allAnswered) {
            e.preventDefault();
            alert('아직 풀지 않은 문제가 있습니다. 모든 문제의 답안을 선택해 주세요.');
        } else {
            // Clear localStorage on success submit
            localStorage.removeItem(storageKey);

            if (pendingModal) {
                pendingModal.classList.add('is-open');
                pendingModal.setAttribute('aria-hidden', 'false');
            }

            if (submitButton) {
                submitButton.disabled = true;
                submitButton.textContent = '진단 결과 생성 중...';
            }
        }
    });
});
