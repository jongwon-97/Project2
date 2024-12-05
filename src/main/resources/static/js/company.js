// DOM이 완전히 로드된 후 실행
document.addEventListener("DOMContentLoaded", function () {
    // 유효성 플래그
    let isValidBusinessNumber = false;

    // 사업자 등록번호 확인 함수
    window.searchBusiness = function () {
        const companyNum = document.getElementById("companyNum").value.trim();
        const resultSpan = document.getElementById("businessValidationResult");

        // 초기화
        resultSpan.textContent = "";
        if (!companyNum || companyNum.length !== 10 || isNaN(companyNum)) {
            isValidBusinessNumber = false;
            resultSpan.textContent = "유효한 10자리 사업자등록번호를 입력하세요.";
            resultSpan.style.color = "red";
            return;
        }

        // API 요청 데이터
        const data = { b_no: [companyNum] };

        // API 호출
        $.ajax({
            url: "https://api.odcloud.kr/api/nts-businessman/v1/status?serviceKey=o9DpOMbewb3fubRDZNCs1OQS2DUicnW%2FsSvJcEe2f%2BXsXWZ0uXLNij2r7hOCwkqMRvc%2F1qYhohcbsDqGezjBzw%3D%3D",
            type: "POST",
            data: JSON.stringify(data),
            dataType: "JSON",
            contentType: "application/json",
            success: function (response) {
                const businessData = response.data[0];
                if (businessData?.b_stt === "계속사업자") {
                    isValidBusinessNumber = true;
                    resultSpan.textContent = "유효한 사업자 등록번호입니다.";
                    resultSpan.style.color = "green";

                    // 내부 API로 기존 데이터 불러오기
                    $.ajax({
                        url: `/api/company/${companyNum}`,
                        type: "GET",
                        success: function (companyResponse) {
                            document.getElementById("companyName").value = companyResponse.companyName || "";
                            document.getElementById("companyEmail").value = companyResponse.companyEmail || "";
                            document.getElementById("companyPhone").value = companyResponse.companyPhone || "";
                            document.getElementById("companyPostcode").value = companyResponse.companyPostcode || "";
                            document.getElementById("companyAddress").value = companyResponse.companyAddress || "";
                            document.getElementById("companyDAddress").value = companyResponse.companyDAddress || "";

                            // 로고 설정
                            const logoPreview = document.getElementById("logoPreview");
                            const logoUrlField = document.getElementById("companyLogoUrl");
                            if (companyResponse.companyLogoUrl) {
                                logoPreview.src = companyResponse.companyLogoUrl;
                                logoUrlField.value = companyResponse.companyLogoUrl;
                            } else {
                                logoPreview.src = "images/noimage.png";
                            }
                        },
                        error: function () {

                        },
                    });
                } else {
                    isValidBusinessNumber = false;
                    resultSpan.textContent = "유효하지 않은 사업자 등록번호입니다.";
                    resultSpan.style.color = "red";
                }
            },
            error: function () {
                isValidBusinessNumber = false;
                resultSpan.textContent = "사업자 등록번호 검증 중 오류가 발생했습니다.";
                resultSpan.style.color = "red";
            },
        });
    };

    // 입력 필드 실시간 검증
    function validateField(fieldId, errorFieldId, validationFn, errorMessage) {
        const field = document.getElementById(fieldId);
        const errorField = document.getElementById(errorFieldId);
        const value = field.value.trim();

        if (validationFn(value)) {
            errorField.textContent = "";
            return true;
        } else {
            errorField.textContent = errorMessage;
            return false;
        }
    }

    // 필드 실시간 검증 추가
    ["companyName", "companyEmail", "companyPhone"].forEach((id) => {
        const errorId = `error${id.charAt(0).toUpperCase() + id.slice(1)}`;
        const validationFn =
            id === "companyEmail"
                ? (value) =>
                      /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)
                : id === "companyPhone"
                ? (value) =>
                      /^\d{2,3}-\d{3,4}-\d{4}$/.test(value)
                : (value) => value !== "";

        // blur 이벤트로 필드 검증
        document.getElementById(id).addEventListener("blur", function () {
            validateField(id, errorId, validationFn, `${id === "companyName" ? "회사명" : id === "companyEmail" ? "유효한 이메일" : "유효한 전화번호"}을 입력해주세요.`);
        });

        // 입력 중 에러 제거
        document.getElementById(id).addEventListener("input", function () {
            const errorField = document.getElementById(errorId);
            errorField.textContent = ""; // 실시간 입력 시 에러 초기화
        });
    });

    // 폼 제출 시 유효성 검사
    document.querySelector("form").onsubmit = function (event) {
        let hasError = false;

        if (!validateField("companyName", "errorCompanyName", (value) => value !== "", "회사명을 입력해주세요.")) {
            hasError = true;
        }

        if (
            !validateField(
                "companyEmail",
                "errorCompanyEmail",
                (value) =>
                    /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value),
                "유효한 이메일 주소를 입력해주세요."
            )
        ) {
            hasError = true;
        }

        if (
            !validateField(
                "companyPhone",
                "errorCompanyPhone",
                (value) =>
                    /^(\d{2,3}-\d{3,4}-\d{4}|15\d{2}-\d{4})$/.test(value),
                "유효한 전화번호 형식을 입력해주세요."
            )
        ) {
            hasError = true;
        }

        if (!isValidBusinessNumber) {
            const resultSpan = document.getElementById("businessValidationResult");
            resultSpan.textContent = "사업자 등록번호를 다시 확인해주세요.";
            resultSpan.style.color = "red";
            hasError = true;
        }

        if (hasError) {
            alert("입력된 정보를 다시 확인해주세요.");
            event.preventDefault();
        }
    };
});
