document.addEventListener('DOMContentLoaded', () => {
    enableImageUpload(); // DOM이 로드된 후 이미지 업로드 기능 활성화
});

export function enableImageUpload() {
    const imgUpdateButton = document.getElementById('imgUpdate');
    const imgFileInput = document.getElementById('memberImgFile');
    const imgPreview = document.getElementById('memberImgName');
    const memberNum = document.getElementById('memberNum')?.value; // 사번
    if (!imgUpdateButton || !imgFileInput || !imgPreview) {
        console.error("필요한 요소가 누락되었습니다. imgUpdateButton, imgFileInput, imgPreview 중 하나를 찾을 수 없습니다.");
        return;
    }
    // 이미지 업로드 버튼 클릭 이벤트
    imgUpdateButton.addEventListener('click', () => {
        console.log("이미지 교체 버튼 클릭됨");
        imgFileInput.click(); // 파일 선택 창 열기
    });
    // 파일 선택 이벤트
    imgFileInput.addEventListener('change', async (event) => {
        const file = event.target.files[0];
        if (file) {
            // 파일 확장자 및 크기 검증
            const allowedExtensions = ['jpg', 'jpeg', 'png', 'gif'];
            const maxSize = 5 * 1024 * 1024; // 5MB
            const fileExtension = file.name.split('.').pop().toLowerCase();
            if (!allowedExtensions.includes(fileExtension)) {
                alert('지원하지 않는 파일 형식입니다. jpg, jpeg, png, gif만 업로드 가능합니다.');
                return;
            }
            if (file.size > maxSize) {
                alert('파일 크기가 너무 큽니다. 최대 5MB까지 업로드 가능합니다.');
                return;
            }
            // 미리보기 업데이트
            const reader = new FileReader();
            reader.onload = (e) => {
                imgPreview.src = e.target.result; // 선택한 이미지를 미리보기로 보여줌
            };
            reader.readAsDataURL(file);
            // 서버에 이미지 업로드
            const formData = new FormData();
            formData.append('image', file);
            formData.append('memberNum', memberNum);
            try {
                const response = await fetch('/admin/uploadMemberImage', {
                    method: 'POST',
                    body: formData,
                });
                if (!response.ok) {
                    throw new Error('이미지 업로드 중 오류가 발생했습니다.');
                }
                const result = await response.json();
                if (result.success) {
                    alert('이미지가 성공적으로 업로드되었습니다.');
                } else {
                    alert(result.message || '이미지 업로드에 실패했습니다.');
                }
            } catch (err) {
                console.error('이미지 업로드 실패:', err);
                alert('이미지 업로드 중 문제가 발생했습니다.');
            }
        }
    });
}
