function navigateToSignup(signupUrl) {
        parent.document.getElementById('iframeContainer').style.display = 'none';
        window.parent.location.href = signupUrl;
    }
    function navigateToFindAccount(findAccountUrl) {
      parent.document.getElementById('iframeContainer').style.display = 'none';
      window.parent.location.href = findAccountUrl;
    }
    function navigateToFindPassword(findPasswordUrl) {
      parent.document.getElementById('iframeContainer').style.display = 'none';
      window.parent.location.href = findPasswordUrl;
    }
    // 페이지 로드 시 실행
    document.addEventListener('DOMContentLoaded', function () {
        const memberIdInput = document.querySelector('input[name="memberId"]');
        const saveIdCheckbox = document.querySelector('input[name="saveId"]');

        // 쿠키에서 'uid' 값을 읽어옴
        const cookies = document.cookie.split('; ').reduce((acc, cookie) => {
            const [key, value] = cookie.split('=');
            acc[key] = value;
            return acc;
        }, {});

        // 'uid' 쿠키가 존재하면 아이디 입력 필드에 값 설정
        if (cookies['uid']) {
            memberIdInput.value = cookies['uid'];
            saveIdCheckbox.checked = true; // 체크박스도 활성화
        }
    });