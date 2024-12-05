// IFrame 보이기
function showLogin(url) {
    const iframeContainer = document.getElementById('iframeContainer');
    const loginIframe = document.getElementById('loginIframe');
    loginIframe.src = url; // IFrame에 URL 설정
    iframeContainer.style.display = 'flex'; // IFrame 컨테이너 보이기
}

// IFrame 닫기: 로그인 여부에 따라 동작
function closeLogin() {
    const iframeContainer = document.getElementById('iframeContainer');
    const loginIframe = document.getElementById('loginIframe');

    // 부모 창에서 성공 여부를 확인
    const isSuccess = loginIframe.src.includes('/home');
    iframeContainer.style.display = 'none';
    loginIframe.src = '';

    // 로그인 성공 여부에 따라 동작 분기
    if (isSuccess) {
        // 로그인 성공 시 홈 화면으로 이동
        window.location.href = '/home';
    } else {
        // 로그인 실패 또는 닫기 버튼 클릭 시 인덱스 페이지로 이동
        iframeContainer.style.display = 'none';
    }
}
