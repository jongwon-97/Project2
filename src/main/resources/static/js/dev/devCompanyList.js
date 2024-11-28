const selectAllCheckbox = document.getElementById("selectAll");
const checkboxes = document.getElementsByName("selectedMembers");
let sortDirection = {};

document.querySelector('form').addEventListener('keydown', function(e) {
    if (e.key === 'Enter') {
        e.preventDefault(); // Enter 키로 인한 기본 동작(폼 제출)을 막음
    }
});

function toggleSelectAll() {
    checkboxes.forEach(checkbox => {
        checkbox.checked = selectAllCheckbox.checked;
    });
}


function checkAllSelected(){
    const allChecked = Array.from(checkboxes).every(checkbox => checkbox.checked);

    // selectAll 체크박스의 상태를 업데이트
    selectAllCheckbox.checked = allChecked;

    // 하나라도 체크되지 않았다면 selectAll 체크박스 해제
    if (!allChecked) {
        selectAllCheckbox.indeterminate = Array.from(checkboxes).some(checkbox => checkbox.checked);
    } else {
        selectAllCheckbox.indeterminate = false;  // 모든 체크박스가 체크된 경우
    }
}



// 테이블 정렬 함수
function sortTable(columnIndex) {
    const table = document.querySelector("table");
    const rows = Array.from(table.querySelectorAll("tbody tr"));

    // 각 열에 맞는 데이터 타입을 판단
    const isDateColumn = columnIndex === 8;  // 회사 가입일(8) 열은 날짜 처리
    const isTextColumn = !isDateColumn;  // 나머지 열들은 텍스트(문자열) 처리

    // 정렬 방향을 저장
    if (!sortDirection[columnIndex]) {
        sortDirection[columnIndex] = 1; // 기본적으로 오름차순
    } else {
        sortDirection[columnIndex] *= -1; // 반대로 변경 (오름차순 <-> 내림차순)
    }

    rows.sort((a, b) => {
        const inputA = a.cells[columnIndex].querySelector('input'); // 해당 셀의 input 요소
        const inputB = b.cells[columnIndex].querySelector('input');

        let valA = inputA.value.trim();
        let valB = inputB.value.trim();

        if (isDateColumn) {
            // 날짜 형식 처리 (생년월일, 입사일)
            valA = new Date(valA);
            valB = new Date(valB);
        }else if(isTextColumn) {
            // 한글과 영어를 동일하게 정렬하려면 normalize()를 사용하거나,
            // localeCompare에서 'ko' 로케일과 'base'로 민감도를 설정해준다
            valA = valA.normalize("NFD").replace(/[\u0300-\u036f]/g, "");  // 유니코드 변환 (선택적)
            valB = valB.normalize("NFD").replace(/[\u0300-\u036f]/g, "");  // 유니코드 변환 (선택적)

            // localeCompare 사용하여 비교 (정렬 방향을 고려)
            const compareResult = valA.localeCompare(valB, 'ko', { sensitivity: 'base' });
            return compareResult * sortDirection[columnIndex];
        }

        // 문자열 또는 숫자 정렬
        if (valA > valB) return sortDirection[columnIndex];
        if (valA < valB) return -sortDirection[columnIndex];
        return 0;
    });
    //console.log(rows);
    //console.log(sortDirection[columnIndex]);
    // 정렬된 행을 테이블에 다시 추가
    const tbody = table.querySelector("tbody");
    rows.forEach(row => tbody.appendChild(row));
}

function goToCompanyPage(button) {
    // 클릭된 버튼에서 data-id 값을 가져옴 (data-id에 memberId가 있음)
    const companyId = button.getAttribute('data-id');

    // memberId를 쿼리 파라미터로 URL에 추가하여 이동
    window.location.href = `/dev/companyPage?id=${companyId}`;
}
