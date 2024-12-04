// 정규식 패턴
const passwordPattern = /^(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*]).{8,}$/;
const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
// 전화번호 정규식 수정: 휴대폰, 지역번호, 안심번호를 포함한 정규식
const phonePattern = /^(01[0|1|6|7|8|9]-\d{3,4}-\d{4}|02-\d{3,4}-\d{4}|0[3-6][1-9]-\d{3,4}-\d{4}|050-\d{4}-\d{4}|050\d-\d{4}-\d{4})$/;
//const phonePattern = /^01[0|1|6|7|8|9]-\d{3,4}-\d{4}$/;
//const phonePattern = /^\d{10,11}$/;
const birthDatePattern = /^(19|20)\d{2}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01])$/;
// 전화번호 포맷팅 함수
function autoFormatPhoneNumber(phone) {
    const onlyNumbers = phone.replace(/[^0-9]/g, "");

    if (onlyNumbers.startsWith("02")) {
        if (onlyNumbers.length <= 9) {
            return onlyNumbers.replace(/(\d{2})(\d{3})(\d{4})/, "$1-$2-$3");
        } else if (onlyNumbers.length <= 10) {
            return onlyNumbers.replace(/(\d{2})(\d{4})(\d{4})/, "$1-$2-$3");
        }
    } else if (onlyNumbers.startsWith("050")) {
        if (onlyNumbers.length === 11) {
            return onlyNumbers.replace(/(\d{3})(\d{4})(\d{4})/, "$1-$2-$3");
        } else if (onlyNumbers.length === 12) {
            return onlyNumbers.replace(/(\d{4})(\d{4})(\d{4})/, "$1-$2-$3");
        }
    } else if (/^0[3-6][1-9]/.test(onlyNumbers)) {
        if (onlyNumbers.length <= 10) {
            return onlyNumbers.replace(/(\d{3})(\d{3})(\d{4})/, "$1-$2-$3");
        } else if (onlyNumbers.length === 11) {
            return onlyNumbers.replace(/(\d{3})(\d{4})(\d{4})/, "$1-$2-$3");
        }
    } else if (/^01[0|1|6|7|8|9]/.test(onlyNumbers)) {
        if (onlyNumbers.length === 10) {
            return onlyNumbers.replace(/(\d{3})(\d{3})(\d{4})/, "$1-$2-$3");
        } else if (onlyNumbers.length === 11) {
            return onlyNumbers.replace(/(\d{3})(\d{4})(\d{4})/, "$1-$2-$3");
        }
    }
    return onlyNumbers; // 포맷에 맞지 않는 경우 숫자만 반환
}

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

    // 전화번호 입력 시 실시간 하이픈 추가
    inputs.memberPhone.addEventListener('input', (event) => {
        const input = event.target;
        const formatted = autoFormatPhoneNumber(input.value);
        input.value = formatted;
    });

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
}; // <- init 함수 닫기 추가

// 검증 함수들
function validateName() {
    const name = document.getElementById("memberName").value;
    const error = document.getElementById("nameError");
    if (!name.trim()) {
        error.textContent = "이름을 입력하세요.";
        error.style.display = "block";
        return false;
    }
    error.textContent = "";
    error.style.display = "none";
    return true;
}


function validatePassword() {
    const password = document.getElementById("memberPw").value;
    const error = document.getElementById("passwordError");
    if (!passwordPattern.test(password)) {
        error.textContent = "비밀번호는 8자 이상이며, 소문자+숫자+특수문자를 포함해야 합니다.";
        error.style.display = "block";
        return false;
    }
    error.textContent = "";
    error.style.display = "none";
    return true;
}

function validateConfirmPassword() {
    const password = document.getElementById("memberPw").value;
    const confirmPassword = document.getElementById("confirmPw").value;
    const error = document.getElementById("confirmPasswordError");
    if (password !== confirmPassword) {
        error.textContent = "비밀번호가 일치하지 않습니다.";
         error.style.display = "block";
        return false;
    }
    error.textContent = "";
    error.style.display = "none"; // 메시지 숨김
    return true;
}

function validateEmail() {
    const email = document.getElementById("memberEmail").value;
    const error = document.getElementById("emailError");
    if (!emailPattern.test(email)) {
        error.textContent = "유효한 이메일 주소를 입력하세요.";
        error.style.display = "block";
        return false;
    }
    error.textContent = "";
    error.style.display = "none";
    return true;
}

function validatePhone() {
    const phone = document.getElementById("memberPhone").value;
    const error = document.getElementById("phoneError");

    if (!phonePattern.test(phone)) {
        error.textContent = "전화번호 형식이 올바르지 않습니다.";
        error.style.display = "block";
        return false;
    }

    // 서버로 전송하기 전 포맷 확인 (JS에서 서버 규칙과 일치하도록 보정)
    document.getElementById("memberPhone").value = autoFormatPhoneNumber(phone);
    console.log("Formatted Phone Before Submit:", autoFormatPhoneNumber(memberPhone));
    error.textContent = "";
    error.style.display = "none";
    return true;
}

function validateBirthDate() {
    const birthDateInput = document.getElementById("memberBirth");
    const birthDate = birthDateInput.value.trim();
    const error = document.getElementById("birthError");

    if (!birthDate) {
        error.textContent = "생년월일을 입력하세요.";
        error.style.display = "block";
        birthDateInput.value = null; // 빈 값을 null로 설정
        return false;
    }

    if (!birthDatePattern.test(birthDate)) {
        error.textContent = "유효한 생년월일 형식(YYYY-MM-DD)이 아닙니다.";
        error.style.display = "block";
        return false;
    }

    error.textContent = "";
    error.style.display = "none";
    return true;
}

function validatePostcode() {
    const postcode = document.getElementById("memberPostcode").value.trim();
    const error = document.getElementById("postcodeError");
    if (!postcode.trim()) {
        error.textContent = "우편번호를 입력하세요.";
        error.style.display = "block";
        return false;
    }
    error.textContent = "";
    error.style.display = "none";
    return true;
}

function validateGender() {
    const genderMale = document.getElementById("M").checked;
    const genderFemale = document.getElementById("F").checked;
    const error = document.getElementById("genderError");
    if (!genderMale && !genderFemale) {
        error.textContent = "성별을 선택하세요.";
        error.style.display = "block";
        return false;
    }
    error.textContent = "";
    error.style.display = "none";
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
    const isPostcodeValid = validatePostcode();

    if ( !isPasswordValid || !isConfirmPasswordValid || !isEmailValid ||
        !isPhoneValid || !isBirthDateValid || !isGenderValid || !isNameValid || !isPostcodeValid) {
        alert("입력 정보를 다시 확인하세요.");
        return false;
    }
    return true;
}

// DOMContentLoaded 시 초기화 함수 실행
document.addEventListener('DOMContentLoaded', init);