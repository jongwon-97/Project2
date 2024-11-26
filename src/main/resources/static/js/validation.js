// 정규식 패턴
const passwordPattern = /^(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*]).{8,}$/;
const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const phonePattern = /^01[0|1|6|7|8|9]-\d{3,4}-\d{4}$/;

// 초기화 함수
const init = () => {
    const form = document.querySelector('form');
    const inputs = {
        memberPw: document.getElementById("memberPw"),
        confirmPw: document.getElementById("confirmPw"),
        memberEmail: document.getElementById("memberEmail"),
        memberPhone: document.getElementById("memberPhone"),
        memberBirth: document.getElementById("memberBirth"),
        memberGender: document.querySelectorAll('input[name="memberGender"]'),
        memberName: document.getElementById("memberName")
    };

    // 비밀번호 입력 시 실시간 검증
    inputs.memberPw.addEventListener('input', validatePassword);
    inputs.confirmPw.addEventListener('input', validateConfirmPassword);

    // 이메일 입력 시 실시간 검증
    inputs.memberEmail.addEventListener('input', validateEmail);

    // 전화번호 입력 시 실시간 검증
    inputs.memberPhone.addEventListener('input', validatePhone);

    // 생년월일 입력 시 검증
    inputs.memberBirth.addEventListener('blur', validateBirthDate);
    // 이름 입력 시 실시간 검증 추가
    inputs.memberName.addEventListener('input', validateName);

    // 폼 제출 시 전체 검증
    form.addEventListener('submit', (event) => {
        if (!validateForm()) {
            event.preventDefault(); // 폼 제출 방지
        }
    });
};
function validateName() {
    const name = document.getElementById("memberName").value;
    const error = document.getElementById("nameError");
    if (!name.trim()) {
        error.textContent = "이름을 입력하세요.";
        return false;
    }
    error.textContent = "";
    return true;
}

// 검증 함수들
function validatePassword() {
    const password = document.getElementById("memberPw").value;
    const error = document.getElementById("passwordError");
    if (!passwordPattern.test(password)) {
        error.textContent = "비밀번호는 8자 이상이며, 소문자+숫자+특수문자를 포함해야 합니다.";
        return false;
    }
    error.textContent = "";
    return true;
}

function validateConfirmPassword() {
    const password = document.getElementById("memberPw").value;
    const confirmPassword = document.getElementById("confirmPw").value;
    const error = document.getElementById("confirmPasswordError");
    if (password !== confirmPassword) {
        error.textContent = "비밀번호가 일치하지 않습니다.";
        return false;
    }
    error.textContent = "";
    return true;
}

function validateEmail() {
    const email = document.getElementById("memberEmail").value;
    const error = document.getElementById("emailError");
    if (!emailPattern.test(email)) {
        error.textContent = "유효한 이메일 주소를 입력하세요.";
        return false;
    }
    error.textContent = "";
    return true;
}

function validatePhone() {
    const phone = document.getElementById("memberPhone").value;
    const error = document.getElementById("phoneError");
    if (!phonePattern.test(phone)) {
        error.textContent = "전화번호 형식이 올바르지 않습니다. 예: 010-1234-5678";
        return false;
    }
    error.textContent = "";
    return true;
}

function validateBirthDate() {
    const birthDate = document.getElementById("memberBirth").value;
    const error = document.getElementById("birthError");
    if (!birthDate) {
        error.textContent = "생년월일을 입력하세요.";
        return false;
    }
    error.textContent = "";
    return true;
}

function validateGender() {
    const genderMale = document.getElementById("M").checked;
    const genderFemale = document.getElementById("F").checked;
    const error = document.getElementById("genderError");
    if (!genderMale && !genderFemale) {
        error.textContent = "성별을 선택하세요.";
        return false;
    }
    error.textContent = "";
    return true;
}

// 전체 폼 검증
function validateForm() {
    const isPasswordValid = validatePassword();
    const isConfirmPasswordValid = validateConfirmPassword();
    const isEmailValid = validateEmail();
    const isPhoneValid = validatePhone();
    const isBirthDateValid = validateBirthDate();
    const isGenderValid = validateGender();
    const isNameValid = validateName();

    if (!isPasswordValid || !isConfirmPasswordValid || !isEmailValid ||
        !isPhoneValid || !isBirthDateValid || !isGenderValid || !isNameValid) {
        alert("입력 정보를 다시 확인하세요.");
        return false;
    }
    return true;
}

// DOMContentLoaded 시 초기화 함수 실행
document.addEventListener('DOMContentLoaded', init);