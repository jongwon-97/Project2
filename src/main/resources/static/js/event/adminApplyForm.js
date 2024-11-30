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


function submitAction(actionType) {
    const form = document.forms['actionForm']; // Form element

    // 선택된 체크박스의 memberId 값을 폼에 추가합니다.
    const selectedMembers = document.querySelectorAll('input[name="selectedMembers"]:checked');
    const selectedCount = selectedMembers.length;

    // 폼에서 기존에 존재하는 hidden input들을 모두 제거
    const hiddenInputs = form.querySelectorAll('input[type="hidden"]');
    hiddenInputs.forEach(input => input.remove());
    const hiddenInput = document.createElement('input');
                hiddenInput.type = 'hidden';
                hiddenInput.name = 'seasonId';  // 서버에서 받을 이름
                hiddenInput.value = seasonId;    // 체크된 항목의 memberId 값을 추가
                form.appendChild(hiddenInput);

    switch (actionType) {
        case 'search':
            const searchValue = document.getElementById('simpleSearch').value.trim();
            const searchOption = document.getElementById('searchOption').value;
            if (searchValue == "") {
                alert("검색어를 입력해주세요.");
                event.preventDefault();  // 폼 제출을 막음
                return;
            }

            form.action = '/admin/event/apply/search';       // search action
            break;

        case 'adSearch':
            // 폼에서 입력된 값 가져오기
            const birthStart = form['birthStart'].value;  // 생년월일 시작일
            const birthEnd = form['birthEnd'].value;      // 생년월일 종료일
            const hireStart = form['hireStart'].value;    // 입사일 시작일
            const hireEnd = form['hireEnd'].value;        // 입사일 종료일
            // 모든 필드가 비어있는지 확인
            if (birthStart === "" && birthEnd === "" && hireStart === "" && hireEnd === "") {
                alert("생년월일과 입사일의 검색 범위를 입력하세요.");
                event.preventDefault();  // 폼 제출을 막음
                return;
            }

            //console.log(birthStart, birthEnd, hireStart, hireEnd);
            form.action = '/admin/event/apply/adSearch';       // search action
            break;

        case 'apply':
            // 선택된 항목의 개수 구하기
            if (selectedCount < 1){
                alert("한 개 이상의 행을 선택하세요")
                return;
            }

            // 선택된 항목을 form에 hidden input으로 추가
            selectedMembers.forEach(checkbox => {
                const hiddenInput = document.createElement('input');
                hiddenInput.type = 'hidden';
                hiddenInput.name = 'memberIds';  // 서버에서 받을 이름
                hiddenInput.value = checkbox.value;    // 체크된 항목의 memberId 값을 추가
                form.appendChild(hiddenInput);
            });
            form.action = '/admin/event/apply/'+seasonId;
            break;
    }
    form.submit(); // Submit the form
}

    function cancelMember(button){
        const form = document.forms['actionForm'];
        // 폼에서 기존에 존재하는 hidden input들을 모두 제거
        const hiddenInputs = form.querySelectorAll('input[type="hidden"]');
        hiddenInputs.forEach(input => input.remove());
        const seasonInput = document.createElement('input');
        seasonInput.type = 'hidden';
        seasonInput.name = 'seasonId';  // 서버에서 받을 이름
        seasonInput.value = seasonId;    // 체크된 항목의 memberId 값을 추가
        form.appendChild(seasonInput);

        let memberId = button.getAttribute('data-member-id');
        const memberInput = document.createElement('input');
        memberInput.type = 'hidden';
        memberInput.name = 'memberId';  // 서버에서 받을 이름
        memberInput.value = memberId;    // 체크된 항목의 memberId 값을 추가
        form.appendChild(memberInput);

        form.action = '/admin/event/apply/cancel';
        form.submit();
    }

// "모든 부서" 체크박스를 클릭하면 모든 부서 체크박스의 상태를 변경하는 함수
function toggleAllDepartments() {
    const isChecked = document.getElementById('departmentsAll').checked;  // "모든 부서" 체크박스 상태
    const departmentCheckboxes = document.querySelectorAll('[name="department"]');  // 개별 부서 체크박스들

    // 모든 부서 체크박스를 "모든 부서" 체크박스의 상태에 맞게 설정
    departmentCheckboxes.forEach(checkbox => {
        checkbox.checked = isChecked;
    });
}

// 개별 부서 체크박스의 상태가 변경될 때마다 "모든 부서" 체크박스의 상태를 업데이트하는 함수
function updateDepartmentsAllState() {
    const departmentCheckboxes = document.querySelectorAll('[name="department"]');  // 개별 부서 체크박스들
    const departmentsAll = document.getElementById('departmentsAll');  // "모든 부서" 체크박스

    // 모든 부서 체크박스가 선택된 상태이면 "모든 부서" 체크박스를 선택, 아니면 해제
    const allChecked = Array.from(departmentCheckboxes).every(checkbox => checkbox.checked);

    // "모든 부서" 체크박스의 상태를 업데이트
    departmentsAll.checked = allChecked;

    // "모든 부서" 체크박스를 클릭하면 모든 부서 체크박스가 선택되거나 해제되도록
    departmentsAll.indeterminate = !allChecked && Array.from(departmentCheckboxes).some(checkbox => checkbox.checked);

    filterMembers();
}

// "모든 직급" 체크박스를 클릭하면 모든 직급 체크박스의 상태를 변경하는 함수
function toggleAllRanks() {
    const isChecked = document.getElementById('ranksAll').checked;  // "모든 직급" 체크박스 상태
    const rankCheckboxes = document.querySelectorAll('[name="rank"]');  // 개별 직급 체크박스들

    // 모든 직급 체크박스를 "모든 직급" 체크박스의 상태에 맞게 설정
    rankCheckboxes.forEach(checkbox => {
        checkbox.checked = isChecked;
    });
}

// 개별 직급 체크박스의 상태가 변경될 때마다 "모든 직급" 체크박스의 상태를 업데이트하는 함수
function updateRanksAllState() {
    const rankCheckboxes = document.querySelectorAll('[name="rank"]');  // 개별 직급 체크박스들
    const ranksAll = document.getElementById('ranksAll');  // "모든 직급" 체크박스

    // 모든 직급 체크박스가 선택된 상태이면 "모든 직급" 체크박스를 선택, 아니면 해제
    const allChecked = Array.from(rankCheckboxes).every(checkbox => checkbox.checked);

    // "모든 직급" 체크박스의 상태를 업데이트
    ranksAll.checked = allChecked;

    // "모든 직급" 체크박스를 클릭하면 모든 직급 체크박스가 선택되거나 해제되도록
    ranksAll.indeterminate = !allChecked && Array.from(rankCheckboxes).some(checkbox => checkbox.checked);

    filterMembers();
}

function filterMembers() {
    // 선택된 부서 및 직급 가져오기
    const selectedDepartments = Array.from(document.querySelectorAll('[name="department"]:checked')).map(checkbox => checkbox.value);
    const selectedRanks = Array.from(document.querySelectorAll('[name="rank"]:checked')).map(checkbox => checkbox.value);

    // 테이블의 모든 행을 순회
    const rows = document.querySelectorAll('#memberTableBody tr');

    rows.forEach(row => {
        const department = row.querySelector('[name="memberDepartment"]').value;
        const rank = row.querySelector('[name="memberRank"]').value;

        // 부서와 직급이 선택된 항목에 맞는지 체크
        if ((selectedDepartments.length === 0 || selectedDepartments.includes(department)) &&
            (selectedRanks.length === 0 || selectedRanks.includes(rank))) {
            row.style.display = ''; // 해당하는 경우 표시
        } else {
            row.style.display = 'none'; // 해당하지 않는 경우 숨김
        }
    });
}

// 테이블 정렬 함수
function sortTable(columnIndex) {
    const table = document.querySelector("table");
    const rows = Array.from(table.querySelectorAll("tbody tr"));

    // 각 열에 맞는 데이터 타입을 판단
    const isDateColumn = columnIndex === 6 || columnIndex === 7;  // 생년월일(6)과 입사일(7) 열은 날짜 처리
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

// 간단검색 보이기 숨기기 기능
function toggleSearch() {
    var advancedSearchDiv = document.getElementById("search");
    if (advancedSearchDiv.style.display === "none" || advancedSearchDiv.style.display === "") {
        advancedSearchDiv.style.display = "block";
    } else {
        advancedSearchDiv.style.display = "none";
    }
}

// 상세검색 보이기 숨기기 기능
function toggleAdvancedSearch() {
    var advancedSearchDiv = document.getElementById("adSearch");
    if (advancedSearchDiv.style.display === "none" || advancedSearchDiv.style.display === "") {
        advancedSearchDiv.style.display = "block";
    } else {
        advancedSearchDiv.style.display = "none";
    }
}

// 참가자 명단 보이기 숨기기 기능
function toggleAttentionMember() {
    var advancedSearchDiv = document.getElementById("attentionMember");
    if (advancedSearchDiv.style.display === "none" || advancedSearchDiv.style.display === "") {
        advancedSearchDiv.style.display = "block";
    } else {
        advancedSearchDiv.style.display = "none";
    }
}


function resetBirthDates() {
    document.getElementById('birthStart').value = '';  // 생년월일 시작일 초기화
    document.getElementById('birthEnd').value = '';    // 생년월일 종료일 초기화
}

// 입사일 초기화 함수
function resetHireDates() {
    document.getElementById('hireStart').value = '';  // 입사일 시작일 초기화
    document.getElementById('hireEnd').value = '';    // 입사일 종료일 초기화
}

function filterBirthMonth(){
    const birthMonth = document.getElementById('birthMonth').value;
    const rows = document.querySelectorAll('#memberTableBody tr');

    rows.forEach(row => {
        const memberBirth = row.querySelector('[name="memberBirth"]').value;
    // memberBirth에서 월을 추출 (2021-12-01 → 12)
        const birthMonthFromDate = new Date(memberBirth).getMonth() + 1; // getMonth()는 0부터 시작하므로 1을 더해줘야 실제 월이 됨
        const formattedMonth = birthMonthFromDate.toString().padStart(2, '0'); // 두 자릿수로 포맷팅 (예: 9 → 09)


    if (birthMonth === '전체보기' || birthMonth === formattedMonth) {
                row.style.display = ''; // 전체보기이거나 해당 월인 경우 표시
            } else {
                row.style.display = 'none'; // 해당하지 않는 경우 숨김
            }

    });
}

function filterHireMonth(){
    const hireMonth = document.getElementById('hireMonth').value;
    const rows = document.querySelectorAll('#memberTableBody tr');

    rows.forEach(row => {
        const memberStart = row.querySelector('[name="memberStartDate"]').value;
        const startMonthFromDate = new Date(memberStart).getMonth() + 1; // getMonth()는 0부터 시작하므로 1을 더해줘야 실제 월이 됨
        const formattedMonth = startMonthFromDate.toString().padStart(2, '0'); // 두 자릿수로 포맷팅 (예: 9 → 09)

    if (hireMonth === '전체보기' || hireMonth === formattedMonth) {
                row.style.display = ''; // 전체보기이거나 해당 월인 경우 표시
            } else {
                row.style.display = 'none'; // 해당하지 않는 경우 숨김
            }
    });
}

function allMemberList(){
    window.location.href = `/admin/event/apply/`+seasonId;
}