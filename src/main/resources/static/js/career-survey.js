// 카테고리 설명 매핑 도우미 함수
function getCategoryDescription(name) {
    if (name.includes('인프라')) return '서버, 클라우드, 컨테이너 가상화, 무중단 배포/운영 기술';
    if (name.includes('보안')) return '웹 모의해킹, OWASP 10 취약점 분석, 접근 제어 및 암호화';
    if (name.includes('AI')) return 'LLM 미세조정, RAG 지식베이스, 벡터 DB 가동 핵심 기술';
    if (name.includes('AX')) return 'RPA 자동화 구축, ChatGPT API 연계, 에이전트 시스템화';
    if (name.includes('프론트')) return 'Next.js 프레임워크, 사용자 경험 최적화, 상태관리';
    if (name.includes('백엔드')) return 'Spring Boot, Node.js 서버 아키텍처, 효율적 대용량 DB 설계';
    return '해당 기술 분야를 심도 있게 학습할 수 있는 커리큘럼을 제공합니다.';
}

// 토스트 알림 함수
function showToast(message) {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.innerHTML = `
        <svg class="toast-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 9V14M12 17.01L12.01 16.9989M12 21C16.9706 21 21 16.9706 21 12C21 7.02944 16.9706 3 12 3C7.02944 3 3 7.02944 3 12C3 16.9706 7.02944 21 12 21Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span class="toast-message">${message}</span>
        <button class="toast-close" type="button">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
        </button>
    `;

    container.appendChild(toast);

    const closeBtn = toast.querySelector('.toast-close');
    const dismissToast = () => {
        if (toast.classList.contains('hide')) return;
        toast.classList.add('hide');
        toast.addEventListener('animationend', () => {
            toast.remove();
            if (container.children.length === 0) {
                container.remove();
            }
        });
    };

    closeBtn.addEventListener('click', dismissToast);

    setTimeout(dismissToast, 3000);
}

// 가상 폴백 데이터 (DB 데이터가 비어 있을 경우를 대비)
const fallbackCategories = [
    { skillId: 1000, skillName: '인프라', skillType: 'CATEGORY' },
    { skillId: 2000, skillName: '정보보안', skillType: 'CATEGORY' },
    { skillId: 3000, skillName: 'AI LAB', skillType: 'CATEGORY' },
    { skillId: 4000, skillName: 'AX', skillType: 'CATEGORY' },
    { skillId: 5000, skillName: '프론트엔드', skillType: 'CATEGORY' },
    { skillId: 6000, skillName: '백엔드', skillType: 'CATEGORY' }
];

const fallbackSubSkills = {
    1000: [
        { skillId: 101, skillName: 'Linux' }, { skillId: 102, skillName: 'Docker' },
        { skillId: 103, skillName: 'Kubernetes' }, { skillId: 104, skillName: 'AWS' },
        { skillId: 105, skillName: 'Nginx' }, { skillId: 106, skillName: 'CI/CD' },
        { skillId: 107, skillName: 'Monitoring' }, { skillId: 108, skillName: 'Terraform' },
        { skillId: 109, skillName: 'Ansible' }
    ],
    2000: [
        { skillId: 201, skillName: '웹 모의해킹' }, { skillId: 202, skillName: 'OWASP Top 10' },
        { skillId: 203, skillName: '접근제어' }, { skillId: 204, skillName: '암호화 알고리즘' },
        { skillId: 205, skillName: '취약점 분석' }, { skillId: 206, skillName: 'Network Security' }
    ],
    3000: [
        { skillId: 301, skillName: 'LLM Fine-tuning' }, { skillId: 302, skillName: 'RAG Architecture' },
        { skillId: 303, skillName: 'Vector DB' }, { skillId: 304, skillName: 'Python' },
        { skillId: 305, skillName: 'Deep Learning' }, { skillId: 306, skillName: 'Machine Learning' }
    ],
    4000: [
        { skillId: 401, skillName: 'RPA 구축' }, { skillId: 402, skillName: 'ChatGPT API 연계' },
        { skillId: 403, skillName: 'AI Agent' }, { skillId: 404, skillName: '업무 자동화' },
        { skillId: 405, skillName: '프롬프트 엔지니어링' }
    ],
    5000: [
        { skillId: 501, skillName: 'React' }, { skillId: 502, skillName: 'Vue.js' },
        { skillId: 503, skillName: 'Next.js' }, { skillId: 504, skillName: 'TypeScript' },
        { skillId: 505, skillName: 'JavaScript' }, { skillId: 506, skillName: 'HTML5/CSS3' },
        { skillId: 507, skillName: 'Redux/Recoil' }
    ],
    6000: [
        { skillId: 601, skillName: 'Spring Boot' }, { skillId: 602, skillName: 'Java' },
        { skillId: 603, skillName: 'Node.js' }, { skillId: 604, skillName: 'REST API' },
        { skillId: 605, skillName: 'Oracle DB' }, { skillId: 606, skillName: 'MySQL / PostgreSQL' },
        { skillId: 607, skillName: 'JPA / Hibernate' }, { skillId: 608, skillName: 'MyBatis' }
    ]
};

document.addEventListener('DOMContentLoaded', () => {
    const categoryContainer = document.getElementById('category-container');
    const q2Section = document.getElementById('section-q2');
    const displayCategoryName = document.getElementById('category-name-display');
    const skillsContainer = document.getElementById('skills-container');
    const selectedCategoryInput = document.getElementById('selectedCategory');
    const surveyForm = document.getElementById('careerSurveyForm');
    const jsErrorAlert = document.getElementById('js-error-alert');
    const q3Section = document.getElementById('section-q3');
    const difficultySkillsContainer = document.getElementById('difficulty-skills-container');

    // 1. Q1 카테고리 스킬 분류 동적 생성
    let categories = dbSkills.filter(skill => skill.skillType === 'CATEGORY' || !skill.parentSkillId);
    if (categories.length === 0) {
        categories = fallbackCategories;
    }

    categoryContainer.innerHTML = '';
    categories.forEach(cat => {
        const card = document.createElement('div');
        card.className = 'category-card';
        if (typeof deptCategoryIds !== 'undefined' && deptCategoryIds.map(Number).includes(Number(cat.skillId))) {
            card.classList.add('highlight-dept');
        }
        card.setAttribute('data-category-id', cat.skillId);
        card.setAttribute('data-category-name', cat.skillName);

        card.innerHTML = `
            <div class="category-card-header">
                <h3>${cat.skillName}</h3>
                <span class="check-icon">✓</span>
            </div>
            <p>${getCategoryDescription(cat.skillName)}</p>
        `;

        // 클릭 이벤트
        card.addEventListener('click', () => {
            const categoryCards = document.querySelectorAll('.category-card');
            categoryCards.forEach(c => c.classList.remove('active'));
            card.classList.add('active');

            selectedCategoryInput.value = cat.skillId;
            localStorage.setItem('career_selectedCategory', cat.skillId);
            displayCategoryName.textContent = cat.skillName;

            // 새로운 분야를 누르면 Q2 스킬들을 다시 선택해야 하므로 Q3를 숨기고 에러를 가립니다.
            q3Section.style.display = 'none';
            difficultySkillsContainer.innerHTML = '';
            jsErrorAlert.style.display = 'none';

            renderSubSkills(cat.skillId, cat.skillName);

            q2Section.style.display = 'block';
            q2Section.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        });

        categoryContainer.appendChild(card);
    });

    // Q3 노출 조건 연산 및 에러 제거 헬퍼
    function checkQ3Visibility() {
        const checkedSkills = document.querySelectorAll('input[name="skillIds"]:checked');
        if (checkedSkills.length > 0) {
            q3Section.style.display = 'block';
            // Q2 스킬 미선택 에러가 켜져 있으면 가림
            if (jsErrorAlert.style.display === 'block' && jsErrorAlert.textContent.includes('Q2.')) {
                jsErrorAlert.style.display = 'none';
            }
            renderDifficultySelectors(checkedSkills);
        } else {
            q3Section.style.display = 'none';
            difficultySkillsContainer.innerHTML = '';
        }
    }

    // 2. Q2 세부 기술 스택 렌더링
    function renderSubSkills(categoryId, categoryName, preselectedSkillIds = []) {
        skillsContainer.innerHTML = '';
        
        // parentSkillId가 카테고리의 skillId와 매칭되는 스킬들 조회
        let filteredSkills = dbSkills.filter(skill => Number(skill.parentSkillId) === Number(categoryId));

        // 매칭되는 스킬이 없을 시 폴백 데이터 사용
        if (filteredSkills.length === 0) {
            // 이름을 기준으로 폴백 매핑
            let fallbackKey = 1000;
            if (categoryName.includes('보안')) fallbackKey = 2000;
            else if (categoryName.includes('AI')) fallbackKey = 3000;
            else if (categoryName.includes('AX')) fallbackKey = 4000;
            else if (categoryName.includes('프론트')) fallbackKey = 5000;
            else if (categoryName.includes('백엔드')) fallbackKey = 6000;
            filteredSkills = fallbackSubSkills[fallbackKey] || [];
        }

        filteredSkills.forEach(skill => {
            const label = document.createElement('label');
            label.className = 'skill-chip';
            if (typeof deptSkills !== 'undefined' && deptSkills.map(Number).includes(Number(skill.skillId))) {
                label.classList.add('highlight-dept');
            }
            
            const checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.name = 'skillIds';
            checkbox.value = skill.skillId;
            checkbox.style.display = 'none';

            if (preselectedSkillIds.includes(String(skill.skillId)) || preselectedSkillIds.includes(Number(skill.skillId))) {
                checkbox.checked = true;
                label.classList.add('active');
            }

            const span = document.createElement('span');
            span.textContent = skill.skillName;

            label.addEventListener('click', (e) => {
                setTimeout(() => {
                    const checkedSkills = document.querySelectorAll('input[name="skillIds"]:checked');
                    if (checkbox.checked && checkedSkills.length > 3) {
                        checkbox.checked = false;
                        label.classList.remove('active');
                        showToast("집중 학습할 기술은 최대 3개까지만 선택 가능합니다.");
                        return;
                    }
                    if (checkbox.checked) {
                        label.classList.add('active');
                    } else {
                        label.classList.remove('active');
                    }
                    saveSelectedSkills();
                    checkQ3Visibility();
                }, 10);
            });

            label.appendChild(checkbox);
            label.appendChild(span);
            skillsContainer.appendChild(label);
        });

        // 초기 로딩 복원 시 Q3 노출 여부 판단
        checkQ3Visibility();
    }

    function saveSelectedSkills() {
        const checkedSkills = document.querySelectorAll('input[name="skillIds"]:checked');
        const skillIds = Array.from(checkedSkills).map(cb => cb.value);
        localStorage.setItem('career_skillIds', JSON.stringify(skillIds));
    }

    // Q3 난이도 동적 생성 및 매핑
    function renderDifficultySelectors(checkedSkills) {
        const savedDifficultiesJSON = localStorage.getItem('career_difficulties');
        let savedDifficulties = {};
        if (savedDifficultiesJSON) {
            try {
                savedDifficulties = JSON.parse(savedDifficultiesJSON);
            } catch (e) {
                console.error(e);
            }
        }

        const previousValues = {};
        difficultySkillsContainer.querySelectorAll('.difficulty-skill-row').forEach(row => {
            const skillId = row.getAttribute('data-skill-id');
            const activeCard = row.querySelector('.difficulty-card.active');
            if (activeCard) {
                previousValues[skillId] = activeCard.getAttribute('data-difficulty');
            }
        });

        difficultySkillsContainer.innerHTML = '';
        checkedSkills.forEach(cb => {
            const skillId = cb.value;
            const label = cb.closest('label');
            const skillName = label ? label.querySelector('span').textContent : '기술';
            
            const defaultValue = previousValues[skillId] || savedDifficulties[skillId] || '중';

            const row = document.createElement('div');
            row.className = 'difficulty-skill-row';
            row.setAttribute('data-skill-id', skillId);
            row.style.marginBottom = '24px';

            row.innerHTML = `
                <div class="difficulty-skill-header" style="margin-bottom: 8px;">
                    <h4 style="font-size: 15px; font-weight: bold; color: var(--color-text-primary);">${skillName} 난이도 선택</h4>
                </div>
                <div class="difficulty-grid small">
                    <div class="difficulty-card difficulty-card--high ${defaultValue === '상' ? 'active' : ''}" data-difficulty="상">
                        <span class="difficulty-val">상</span>
                        <span class="difficulty-desc">실무자 / 마스터 과정</span>
                    </div>
                    <div class="difficulty-card difficulty-card--medium ${defaultValue === '중' ? 'active' : ''}" data-difficulty="중">
                        <span class="difficulty-val">중</span>
                        <span class="difficulty-desc">준비생 / 현업 적용용</span>
                    </div>
                    <div class="difficulty-card difficulty-card--low ${defaultValue === '하' ? 'active' : ''}" data-difficulty="하">
                        <span class="difficulty-val">하</span>
                        <span class="difficulty-desc">비전공자 / 전직자 기초</span>
                    </div>
                </div>
                <input type="hidden" name="difficulties" value="${defaultValue}">
            `;

            const cards = row.querySelectorAll('.difficulty-card');
            const input = row.querySelector('input[name="difficulties"]');
            cards.forEach(card => {
                card.addEventListener('click', () => {
                    cards.forEach(c => c.classList.remove('active'));
                    card.classList.add('active');
                    input.value = card.getAttribute('data-difficulty');
                    saveSelectedDifficulties();
                });
            });

            difficultySkillsContainer.appendChild(row);
        });
        saveSelectedDifficulties();
    }

    function saveSelectedDifficulties() {
        const difficultiesMap = {};
        document.querySelectorAll('.difficulty-skill-row').forEach(row => {
            const skillId = row.getAttribute('data-skill-id');
            const val = row.querySelector('input[name="difficulties"]').value;
            difficultiesMap[skillId] = val;
        });
        localStorage.setItem('career_difficulties', JSON.stringify(difficultiesMap));
    }

    // localStorage 복원
    function restoreSavedState() {
        const savedCategory = localStorage.getItem('career_selectedCategory');
        const savedSkillsJSON = localStorage.getItem('career_skillIds');

        if (savedCategory) {
            const targetCard = document.querySelector(`.category-card[data-category-id="${savedCategory}"]`);
            if (targetCard) {
                const catName = targetCard.getAttribute('data-category-name');
                const categoryCards = document.querySelectorAll('.category-card');
                categoryCards.forEach(c => c.classList.remove('active'));
                targetCard.classList.add('active');
                selectedCategoryInput.value = savedCategory;
                displayCategoryName.textContent = catName;
                
                let preselectedSkillIds = [];
                if (savedSkillsJSON) {
                    try {
                        preselectedSkillIds = JSON.parse(savedSkillsJSON);
                    } catch (e) {
                        console.error(e);
                    }
                }
                renderSubSkills(savedCategory, catName, preselectedSkillIds);
                q2Section.style.display = 'block';
            }
        }
    }

    restoreSavedState();

    // 폼 제출 검증
    surveyForm.addEventListener('submit', (e) => {
        const checkedSkills = document.querySelectorAll('input[name="skillIds"]:checked');
        if (!selectedCategoryInput.value) {
            e.preventDefault();
            jsErrorAlert.textContent = "Q1. 핵심 기술 분야를 먼저 선택해 주세요.";
            jsErrorAlert.style.display = 'block';
            return;
        }
        if (checkedSkills.length === 0) {
            e.preventDefault();
            jsErrorAlert.textContent = "Q2. 집중 학습할 세부 기술들을 최소 1개 이상 선택해 주세요.";
            jsErrorAlert.style.display = 'block';
            return;
        }
        jsErrorAlert.style.display = 'none';

        localStorage.removeItem('career_selectedCategory');
        localStorage.removeItem('career_skillIds');
        localStorage.removeItem('career_difficulties');
    });
});
