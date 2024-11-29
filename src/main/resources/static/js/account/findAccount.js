// 폼 전환 로직
function showForm(method) {
  clearForm('companyForm');
  clearForm('emailForm');
  clearForm('phoneForm');

  if (method === 'company') {
    document.getElementById('companyForm').style.display = 'block';
  } else if (method === 'email') {
    document.getElementById('emailForm').style.display = 'block';
  } else if (method === 'phone') {
    document.getElementById('phoneForm').style.display = 'block';
  }
}

// 특정 폼의 입력 필드 초기화
function clearForm(formId) {
  const form = document.getElementById(formId);
  form.style.display = 'none'; // 폼 숨기기
  const inputs = form.querySelectorAll('input'); // 폼 내부의 모든 input 필드 선택
  inputs.forEach(input => {
    input.value = ''; // 입력값 초기화
  });
}

// 휴대전화번호 자동 하이픈 추가
function formatPhone(input) {
  const value = input.value.replace(/[^0-9]/g, ''); // 숫자만 남김
  if (value.length <= 3) {
    input.value = value;
  } else if (value.length <= 7) {
    input.value = value.slice(0, 3) + '-' + value.slice(3);
  } else {
    input.value = value.slice(0, 3) + '-' + value.slice(3, 7) + '-' + value.slice(7, 11);
  }
}

// 사업자등록번호 자동 하이픈 추가
function formatBusinessNumber(input) {
  const value = input.value.replace(/[^0-9]/g, ''); // 숫자만 남김
  if (value.length <= 3) {
    input.value = value;
  } else if (value.length <= 5) {
    input.value = value.slice(0, 3) + '-' + value.slice(3);
  } else {
    input.value = value.slice(0, 3) + '-' + value.slice(3, 5) + '-' + value.slice(5, 10);
  }
}

// 폼 제출 시 입력값 검증
document.getElementById('findAccountForm').addEventListener('submit', function (event) {
  // 기업회원 폼 검증
  if (document.getElementById('companyForm').style.display === 'block') {
    if (!document.getElementById('companyNum').value.trim()) {
      alert('사업자등록번호를 입력해주세요.');
      event.preventDefault();
      return;
    }
    if (!document.getElementById('memberNum').value.trim()) {
      alert('사원번호를 입력해주세요.');
      event.preventDefault();
      return;
    }
  }

  // 개인회원(이메일) 폼 검증
  if (document.getElementById('emailForm').style.display === 'block') {
    if (!document.getElementById('memberNameEmail').value.trim()) {
      alert('이름을 입력해주세요.');
      event.preventDefault();
      return;
    }
    const emailValue = document.getElementById('memberEmail').value.trim();
    if (!emailValue) {
      alert('이메일을 입력해주세요.');
      event.preventDefault();
      return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailValue)) {
      alert('유효한 이메일 형식이 아닙니다.');
      event.preventDefault();
      return;
    }
  }

  // 개인회원(휴대전화번호) 폼 검증
  if (document.getElementById('phoneForm').style.display === 'block') {
    if (!document.getElementById('memberNamePhone').value.trim()) {
      alert('이름을 입력해주세요.');
      event.preventDefault();
      return;
    }
    if (!document.getElementById('memberPhone').value.trim()) {
      alert('휴대전화번호를 입력해주세요.');
      event.preventDefault();
      return;
    }
  }
});
