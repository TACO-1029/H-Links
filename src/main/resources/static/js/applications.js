/**
 * applications.js - 내 신청 내역 제어 클로저 모듈
 */
(function () {
    'use strict';

    // 전역 네임스페이스 또는 필요한 엘리먼트 캐싱
    let activeCancelCourseId = null;

    // 모달 DOM 참조
    const detailModal = document.getElementById('detailModal');
    const cancelConfirmModal = document.getElementById('cancelConfirmModal');

    // 모달 제어 함수
    function openModal(modalElement) {
        if (!modalElement) return;
        modalElement.classList.add('is-open');
        document.body.style.overflow = 'hidden';
    }

    function closeModal(modalElement) {
        if (!modalElement) return;
        modalElement.classList.remove('is-open');
        document.body.style.overflow = '';
    }

    // 상세 정보 모달 열기
    window.openDetailModal = function (element) {
        const title = element.getAttribute('data-title');
        const type = element.getAttribute('data-type');
        const status = element.getAttribute('data-status');
        const location = element.getAttribute('data-location');
        const start = element.getAttribute('data-start');
        const end = element.getAttribute('data-end');
        const reject = element.getAttribute('data-reject');

        // 엘리먼트 바인딩
        document.getElementById('mdlTitle').innerText = title || '-';
        document.getElementById('mdlType').innerText = type === 'OFFLINE' ? '오프라인 특강' : '온라인 과정';

        let statusText = '';
        switch (status) {
            case 'WAITING':
                statusText = '승인 대기 중';
                break;
            case 'APPROVED':
                statusText = '최종 승인 완료';
                break;
            case 'REJECTED':
                statusText = '심사 반려됨';
                break;
            case 'CANCELED':
                statusText = '신청 취소 완료';
                break;
            default:
                statusText = status;
        }
        document.getElementById('mdlStatus').innerText = statusText;

        // 오프라인 전용 패널
        const offlinePanel = document.getElementById('mdlOfflinePanel');
        if (type === 'OFFLINE') {
            offlinePanel.style.display = 'block';
            document.getElementById('mdlLocation').innerText = location || '장소 미정';
            document.getElementById('mdlSchedule').innerText = `${start || '-'} ~ ${end || '-'}`;
        } else {
            offlinePanel.style.display = 'none';
        }

        // 반려 사유 패널
        const rejectPanel = document.getElementById('mdlRejectPanel');
        if (status === 'REJECTED') {
            rejectPanel.style.display = 'block';
            document.getElementById('mdlRejectReason').innerText = reject || '등록된 상세 사유가 존재하지 않습니다.';
        } else {
            rejectPanel.style.display = 'none';
        }

        openModal(detailModal);
    };

    window.closeDetailModal = function () {
        closeModal(detailModal);
    };

    // 커스텀 컨펌 모달 열기 (취소하기)
    window.openCancelConfirmModal = function (courseId, courseTitle) {
        activeCancelCourseId = courseId;
        document.getElementById('cancelTargetTitle').innerText = courseTitle;
        openModal(cancelConfirmModal);
    };

    window.closeCancelConfirmModal = function () {
        closeModal(cancelConfirmModal);
        activeCancelCourseId = null;
    };

    // 취소 API 통신 처리
    window.confirmCancelApplication = function (csrfToken, csrfHeader) {
        if (!activeCancelCourseId) return;

        fetch(`/mypage/applications/${activeCancelCourseId}`, {
            method: 'DELETE',
            headers: {
                'X-Requested-With': 'XMLHttpRequest',
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            }
        })
        .then(response => {
            if (!response.ok) throw new Error('서버 통신 오류 장애');
            return response.json();
        })
        .then(res => {
            closeModal(cancelConfirmModal);
            if (res.success) {
                alert('강의 신청 취소 처리가 완료되었습니다.');
                location.reload();
            } else {
                alert(`취소 처리 실패: ${res.message}`);
            }
        })
        .catch(error => {
            console.error(error);
            closeModal(cancelConfirmModal);
            alert('일시적인 시스템 오류가 발생했습니다. 담당자에게 문의하세요.');
        });
    };

    // 드롭다운 필터 변경 핸들러
    window.handleStatusFilterChange = function (selectElement) {
        const selectedStatus = selectElement.value;
        const cards = document.querySelectorAll('.application-card');

        cards.forEach(card => {
            if (!selectedStatus || card.getAttribute('data-status-filter') === selectedStatus) {
                card.style.display = 'flex';
            } else {
                card.style.display = 'none';
            }
        });

        // 필터링 결과 0건인 경우에 대비하여 동적 빈 화면 표시 여부 조절
        const visibleCards = Array.from(cards).filter(card => card.style.display !== 'none');
        const emptyElement = document.getElementById('filteredEmptyMessage');

        if (visibleCards.length === 0 && cards.length > 0) {
            if (!emptyElement) {
                const listContainer = document.querySelector('.applications-list');
                const emptyDiv = document.createElement('div');
                emptyDiv.id = 'filteredEmptyMessage';
                emptyDiv.className = 'applications-empty';
                emptyDiv.style.marginTop = '16px';
                emptyDiv.innerHTML = `
                    <div class="applications-empty__icon">
                        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M12 9V14M12 17.01L12.01 16.9989M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                    </div>
                    <p class="applications-empty__text">조건에 부합하는 신청 내역이 없습니다.</p>
                `;
                listContainer.appendChild(emptyDiv);
            } else {
                emptyElement.style.display = 'flex';
            }
        } else {
            if (emptyElement) {
                emptyElement.style.display = 'none';
            }
        }
    };

    // 모달 바깥 영역 클릭 시 닫기 바인딩
    window.addEventListener('click', function (e) {
        if (e.target.id === 'detailModal') {
            closeModal(detailModal);
        }
        if (e.target.id === 'cancelConfirmModal') {
            closeModal(cancelConfirmModal);
        }
    });

})();
