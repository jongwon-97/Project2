// iframe 열기
function showLogin(url) {
    const iframeContainer = document.getElementById('iframeContainer');
    const loginIframe = document.getElementById('loginIframe');
    loginIframe.src = url;
    iframeContainer.style.display = 'flex';
}

// iframe 닫기
function closeLogin() {
    const iframeContainer = document.getElementById('iframeContainer');
    iframeContainer.style.display = 'none';
    const loginIframe = document.getElementById('loginIframe');
    loginIframe.src = ""; // src를 비워서 리소스 해제
}