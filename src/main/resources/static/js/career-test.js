document.addEventListener('DOMContentLoaded', () => {
    const optionItems = document.querySelectorAll('.option-item');
    
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
        });
    });

    const form = document.getElementById('levelTestForm');
    form.addEventListener('submit', (e) => {
        const hiddenInputs = document.querySelectorAll('input[name="selectedOptionIds"]');
        let allAnswered = true;
        hiddenInputs.forEach((input, index) => {
            if (!input.value) {
                allAnswered = false;
            }
        });

        if (!allAnswered) {
            e.preventDefault();
            alert('아직 풀지 않은 문제가 있습니다. 모든 문제의 답안을 선택해 주세요.');
        }
    });
});
