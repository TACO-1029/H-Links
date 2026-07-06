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

    // Restore selected answers visually and in hidden inputs
    Object.entries(savedAnswers).forEach(([qId, optId]) => {
        const optionItem = document.querySelector(`.option-item[data-q-id="${qId}"][data-opt-id="${optId}"]`);
        if (optionItem) {
            optionItem.classList.add('active');
            const input = document.getElementById('selected-option-input-' + qId);
            if (input) {
                input.value = optId;
            }
        }
    });

    // Handle option click
    optionItems.forEach(item => {
        item.addEventListener('click', () => {
            const qId = item.getAttribute('data-q-id');
            const optId = item.getAttribute('data-opt-id');

            // Find other options of the same question and remove active class
            const sisterOptions = document.querySelectorAll(`.option-item[data-q-id="${qId}"]`);
            sisterOptions.forEach(opt => opt.classList.remove('active'));

            // Add active class to clicked option
            item.classList.add('active');

            // Update the hidden input value
            const input = document.getElementById('selected-option-input-' + qId);
            if (input) {
                input.value = optId;
            }

            // Save to localStorage
            savedAnswers[qId] = optId;
            localStorage.setItem(storageKey, JSON.stringify(savedAnswers));
        });
    });

    // Handle form submission
    form.addEventListener('submit', (e) => {
        const hiddenInputs = document.querySelectorAll('input[name="selectedOptionIds"]');
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
        }
    });
});
