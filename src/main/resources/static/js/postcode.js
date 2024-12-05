export function searchAddress(postcodeId, addressId, detailAddressId) {
    new daum.Postcode({
        oncomplete: function (data) {
            // 주소 변수
            let addr = ''; // 도로명/지번 주소
            let extraAddr = ''; // 참고항목

            // 사용자가 선택한 주소 타입에 따라 주소 값 결정
            if (data.userSelectedType === 'R') {
                addr = data.roadAddress; // 도로명 주소
            } else {
                addr = data.jibunAddress; // 지번 주소
            }

            // 참고항목 설정 (도로명 주소일 경우)
            if (data.userSelectedType === 'R') {
                if (data.bname && /[동|로|가]$/g.test(data.bname)) {
                    extraAddr += data.bname; // 법정동 추가
                }
                if (data.buildingName && data.apartment === 'Y') {
                    extraAddr += (extraAddr ? ', ' + data.buildingName : data.buildingName); // 건물명 추가
                }
            }

            // 우편번호와 주소 필드 채우기
            document.getElementById(postcodeId).value = data.zonecode; // 우편번호
            document.getElementById(addressId).value = addr; // 도로명 또는 지번 주소

            // 상세주소 입력란에 포커스
            if (detailAddressId) {
                document.getElementById(detailAddressId).focus();
            }
        }
    }).open(); // 팝업 열기
}

// 전역 객체에 함수 등록
window.searchPostcodeBtn = function () {
    searchAddress('memberPostcode', 'memberAddress', 'memberDAddress');
};
