import { searchAddress } from '/js/postcode.js';

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('userMyPageForm');
    if (form) {
        initializeForm();
    }
});

function initializeForm() {
    const postcodeButton = document.getElementById('postcodeButton');
    const editButton = document.getElementById('edit');
    const submitButton = document.getElementById('submit');
    // 주소 검색 버튼 이벤트
    postcodeButton?.addEventListener('click', () => {
        searchAddress('memberPostcode', 'memberAddress', 'memberDAddress');
    });
    // 수정 버튼 이벤트 등록
    editButton?.addEventListener('click', enableEdit);
    // 폼 제출 이벤트 등록
    const form = document.getElementById('userMyPageForm');
    form?.addEventListener('submit', validateForm);
}

function enableEdit() {
    const inputs = document.querySelectorAll('#userMyPageForm input, #userMyPageForm select');
    inputs.forEach(input => {
        if (!['memberPostcode', 'companyId'].includes(input.id)) {
            input.removeAttribute('readonly');
            input.removeAttribute('disabled');
            input.style.border = '1px solid #000'; // 수정 가능 테두리
        }
    });
    toggleButtons(true);
}

function toggleButtons(isEditing) {
    const editButton = document.getElementById('edit');
    const submitButton = document.getElementById('submit');
    const postcodeButton = document.getElementById('postcodeButton');
    if (isEditing) {
        editButton.style.visibility = 'hidden';
        submitButton.style.visibility = 'visible';
        postcodeButton.style.display = 'inline-block';
    } else {
        editButton.style.visibility = 'visible';
        submitButton.style.visibility = 'hidden';
        postcodeButton.style.display = 'none';
    }
}

async function validateForm(event) {
    event.preventDefault();
    const requiredFields = [
        { id: 'memberNum', message: '사번은 필수 입력 항목입니다.' },
        { id: 'memberName', message: '이름은 필수 입력 항목입니다.' },
        { id: 'memberId', message: '아이디는 필수 입력 항목입니다.' },
        { id: 'memberDepartment', message: '부서는 필수 입력 항목입니다.' },
        { id: 'memberRank', message: '직급은 필수 입력 항목입니다.' },
        { id: 'memberPhone', message: '유효한 전화번호 형식이 아닙니다.', pattern: /^010-\d{3,4}-\d{4}$/ },
        { id: 'memberEmail', message: '유효한 이메일 형식이 아닙니다.', pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/ },
        { id: 'memberPostcode', message: '우편번호는 필수 입력 항목입니다.' },
        { id: 'memberAddress', message: '주소는 필수 입력 항목입니다.' },
        {
            id: 'memberStatus',
            message: '상태는 "활동", "정지", "휴면" 중 하나를 선택해주세요.',
            options: ['활동', '정지', '휴면']
        }
    ];
    // 입력값 검증
    for (const field of requiredFields) {
        if (!validateField(field)) {
            return false;
        }
    }
    // 생년월일 유효성 검사
    if (!validateBirthDate()) {
        return false;
    }
    // 사번 중복 확인
    if (!(await checkMemberNumDuplicate())) {
        return false;
    }
    // 모든 검증 통과 시 폼 제출
    submitForm();
}

function validateField({ id, message, pattern, options }) {
    const field = document.getElementById(id);
    const value = field?.value?.trim();
    if (!value || (pattern && !pattern.test(value)) || (options && !options.includes(value))) {
        alert(message);
        field?.focus();
        return false;
    }
    return true;
}

function validateBirthDate() {
    const birthField = document.getElementById('memberBirth');
    const birthDateValue = birthField?.value.trim();
    const today = new Date();
    const minBirthDate = new Date('1900-01-01');
    if (!birthDateValue) {
        alert('생년월일은 필수 입력 항목입니다.');
        birthField.focus();
        return false;
    }
    const birthDate = new Date(birthDateValue);
    if (isNaN(birthDate.getTime())) {
        alert('올바른 생년월일을 입력해주세요.');
        birthField.focus();
        return false;
    }
    if (birthDate < minBirthDate) {
        alert('생년월일은 1900년 1월 1일 이후여야 합니다.');
        birthField.focus();
        return false;
    }
    if (birthDate >= today) {
        alert('생년월일은 오늘 이전 날짜여야 합니다.');
        birthField.focus();
        return false;
    }
    return true;
}

async function checkMemberNumDuplicate() {
    const memberNum = document.getElementById('memberNum')?.value?.trim();
    const companyId = document.getElementById('companyId')?.value?.trim();
    if (!companyId) {
        console.error('Company ID가 누락되었습니다.');
        return false;
    }
    try {
        const response = await fetch('/admin/checkMemberNum', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ memberNum, companyId })
        });
        if (!response.ok) {
            throw new Error('네트워크 응답이 정상적이지 않습니다.');
        }
        const data = await response.json();
        if (data.isDuplicate) {
            alert('중복된 사번이 존재합니다. 다른 사번을 입력해주세요.');
            document.getElementById('memberNum')?.focus();
            return false;
        }
        return true;
    } catch (err) {
        console.error('사번 중복 확인 중 오류:', err);
        alert('사번 중복 확인에 실패했습니다. 다시 시도해주세요.');
        return false;
    }
}

function submitForm() {
    const form = document.getElementById('userMyPageForm');
    const formData = new FormData(form);
    fetch(form.action, {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('폼 제출 중 오류가 발생했습니다.');
        }
        alert('성공적으로 변경되었습니다.');
        location.reload();
    })
    .catch(err => {
        console.error('폼 제출 실패:', err);
        alert('폼 제출에 실패했습니다. 다시 시도해주세요.');
    });
}
