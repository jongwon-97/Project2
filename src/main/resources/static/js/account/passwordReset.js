// 기존 정규식
const passwordPattern = /^(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*]).{8,}$/;

// 입력 필드와 메시지 요소 참조
const newPasswordField = document.getElementById('newPassword');
const confirmPasswordField = document.getElementById('confirmPassword');
const passwordError = document.getElementById('passwordError');
const passwordValid = document.getElementById('passwordValid');
const confirmError = document.getElementById('confirmError');

// 실시간 비밀번호 검증
newPasswordField.addEventListener('input', () => {
  const password = newPasswordField.value;

  // 정규식 검증
  if (passwordPattern.test(password)) {
    passwordError.style.display = 'none';
    passwordValid.style.display = 'block';
  } else {
    passwordError.style.display = 'block';
    passwordValid.style.display = 'none';
  }

  // 확인창과 일치 여부 확인
  checkPasswordMatch();
});

// 실시간 비밀번호 확인 검증
confirmPasswordField.addEventListener('input', () => {
  checkPasswordMatch();
});

// 비밀번호 확인 일치 여부 확인 함수
function checkPasswordMatch() {
  const password = newPasswordField.value;
  const confirmPassword = confirmPasswordField.value;

  if (confirmPassword && password !== confirmPassword) {
    confirmError.style.display = 'block';
  } else {
    confirmError.style.display = 'none';
  }
}

// 폼 제출 시 최종 검증
document.getElementById('passwordResetForm').addEventListener('submit', (event) => {
  const password = newPasswordField.value;
  const confirmPassword = confirmPasswordField.value;

  if (!passwordPattern.test(password)) {
    event.preventDefault();
    alert('비밀번호 조건을 만족하지 않습니다. 다시 확인해주세요.');
  } else if (password !== confirmPassword) {
    event.preventDefault();
    alert('비밀번호와 확인 비밀번호가 일치하지 않습니다.');
  }
});
