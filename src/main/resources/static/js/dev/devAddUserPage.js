const selectAllCheckbox = document.getElementById("selectAll");
let checkboxes = document.querySelectorAll('input[name="checkRow"]');

// CSV 파일을 읽어와서 테이블에 추가하는 함수
function loadCSV() {
    const fileInput = document.getElementById("csvFileInput");
    const file = fileInput.files[0];

    // 파일이 선택되지 않았으면 경고 메시지
    if (!file) {
        alert('파일을 선택하세요');
        return;
    }


    // FileReader로 파일을 읽음
    const reader = new FileReader();
    reader.onload = function(e) {
        const fileContent = e.target.result;

        // EUC-KR 인코딩으로 디코딩
        const decoder = new TextDecoder("euc-kr");  // EUC-KR로 디코딩
        const decodedText = decoder.decode(fileContent);  // 디코딩된 텍스트

        // PapaParse로 CSV 파싱
        Papa.parse(decodedText, {
            complete: function(results) {
                // console.log(results); // 파싱된 CSV 내용 로그로 확인
                const rows = results.data;

                // 첫 번째 행은 열 이름이므로 두 번째 행부터 데이터 입력
                for (let i = 0; i < rows.length; i++) {
                    const row = rows[i];
                    addRowFromCSV(row);  // 새로운 행을 추가
                }
            },
            header: true, // 첫 번째 행을 열 이름으로 사용
            skipEmptyLines: true // 빈 줄은 건너뛰기
        });
    };

    // 파일을 ArrayBuffer로 읽어들임
    reader.readAsArrayBuffer(file);
}

// CSV 파일을 읽어와서 테이블에 추가하는 함수 (이미지 열 제거)
function addRowFromCSV(rowData) {
    const table = document.getElementById("memberTable");
    let firstRow = table.rows[1]; // 첫 번째 데이터 행 (헤더가 아닌 첫 번째 행)
    let newRow;

    // 첫 번째 행의 3번째 셀이 비어있으면 마지막 행 앞에 추가
    if (firstRow && firstRow.cells[2].querySelector('input').value.trim() == "") {
        table.deleteRow(1); // 첫 번째 데이터 행 삭제
        newRow = table.insertRow(1);
    } else {
        newRow = table.insertRow(table.rows.length); // 마지막 행 뒤에 추가
    }


    // CSV 열 이름 -> 실제 input name을 매핑
    const columnMapping = {
        "사번": "memberNum",
        "회원 ID": "memberId",
        "이름": "memberName",
        "직급": "memberRank",
        "부서": "memberDepartment",
        "전화번호": "memberPhone",
        "생년월일": "memberBirth",
        "성별": "memberGender",
        "이메일": "memberEmail",
        "우편번호": "memberPostcode",
        "주소": "memberAddress",
        "상세주소": "memberDAddress",
        "입사일": "memberStartDate"
    };

    // 번호 셀 추가
    const cellIndex = newRow.insertCell(0);
    cellIndex.innerHTML = table.rows.length - 1; // 번호는 행의 순서대로

    // 체크박스 셀 추가
    const cellCheckbox = newRow.insertCell(1);
    const checkbox = document.createElement("input");
    checkbox.type = "checkbox";
    checkbox.name = "checkRow";
    checkbox.onchange = checkAllSelected;
    cellCheckbox.appendChild(checkbox);

    // 각 열에 맞는 데이터 입력
    Object.keys(columnMapping).forEach((col, index) => {
        const cell = newRow.insertCell(index + 2); // 번호와 체크박스를 제외한 셀 위치
        const value = rowData[col] || ""; // 해당 열의 데이터가 없다면 빈 문자열
        const inputName = columnMapping[col]; // 실제 input의 name 속성

        if (col === "성별") {
            // 성별에 대해 select 추가
            const select = document.createElement("select");
            select.name = inputName;
            const optionMale = document.createElement("option");
            optionMale.value = "M";
            optionMale.text = "남성";
            const optionFemale = document.createElement("option");
            optionFemale.value = "F";
            optionFemale.text = "여성";
            select.append(optionMale, optionFemale);
            select.value = value; // CSV에서 읽은 값으로 선택
            cell.appendChild(select);
        } else if (col === "입사일" || col === "생년월일") {
            // 날짜 항목에 대해 input[type="date"] 추가
            const input = document.createElement("input");
            input.type = "date";
            input.name = inputName;
            input.value = value;
            cell.appendChild(input);
        } else {
            // 그 외 텍스트 필드 추가
            const inputElement = document.createElement("input");
            inputElement.type = "text";
            inputElement.name = inputName;
            inputElement.value = value;
            cell.appendChild(inputElement);
        }
    });
    updateRowNumbers();
}

// 행 추가 함수
function addRow() {
    var table = document.getElementById("memberTable");
    var newRow = table.insertRow(table.rows.length); // 마지막 행 뒤에 추가

    var columns = [
        "memberNum", "memberId", "memberName", "memberRank",
        "memberDepartment", "memberPhone", "memberBirth",
        "memberGender", "memberEmail", "memberPostcode",
        "memberAddress", "memberDAddress", "memberStartDate"
    ];

    // 번호 셀 추가
    var cellIndex = newRow.insertCell(0);
    cellIndex.innerHTML = table.rows.length - 1; // 번호는 행의 순서대로

    // 체크박스 셀 추가
    var cellCheckbox = newRow.insertCell(1);
    var checkbox = document.createElement("input");
    checkbox.type = "checkbox";
    checkbox.name = "checkRow";
    checkbox.onchange = checkAllSelected;
    cellCheckbox.appendChild(checkbox);

    columns.forEach(function(col, index) {
        var cell = newRow.insertCell(index + 2); // 번호와 체크박스를 제외한 셀 위치
        var inputElement;

        // 각 항목에 맞는 input 필드 추가
        if (col === "memberBirth" || col === "memberStartDate") {
            inputElement = document.createElement("input");
            inputElement.type = "date";
            inputElement.name = col;
        } else if (col === "memberGender") {
            inputElement = document.createElement("select");
            inputElement.name = col;
            var optionMale = document.createElement("option");
            optionMale.value = "M"; // 남성
            optionMale.text = "남성";
            var optionFemale = document.createElement("option");
            optionFemale.value = "F"; // 여성
            optionFemale.text = "여성";
            inputElement.append(optionMale, optionFemale);
        } else {
            inputElement = document.createElement("input");
            inputElement.type = "text";
            inputElement.name = col;
        }

        cell.appendChild(inputElement);
        updateRowNumbers()
    });
}

// 모든 체크박스 선택/해제
function toggleSelectAll() {
    let isChecked = document.getElementById("selectAll").checked;
    checkboxes.forEach(checkbox => {
        checkbox.checked = isChecked;
    });
}


// 선택된 행 삭제 함수
function deleteSelectedRows() {
    var table = document.getElementById("memberTable");
    var rows = table.getElementsByTagName("tr");
    for (var i = rows.length - 1; i > 0; i--) { // 첫 번째 행은 헤더이므로 제외
        var row = rows[i];
        var checkbox = row.querySelector('input[type="checkbox"]');
        if (checkbox && checkbox.checked) {
            table.deleteRow(i); // 체크된 행 삭제
        }
    }
    // 삭제 후 번호 재조정
    updateRowNumbers();
}

// 행 번호 업데이트 함수
function updateRowNumbers() {
    var table = document.getElementById("memberTable");
    var rows = table.getElementsByTagName("tr");
    for (var i = 1; i < rows.length; i++) {
        var row = rows[i];
        row.cells[0].innerText = i; // 번호 재조정
    }
    checkboxes = document.querySelectorAll('input[name="checkRow"]');
    checkAllSelected();
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

function goToCompanyPage(companyId){
    window.location.href = `/dev/companyPage?id=${companyId}`;
}


const addMember = async () => {
    let formData = new FormData();
    const table = document.getElementById("memberTable");
    const rows = table.getElementsByTagName("tr");

    let membersData = []; // 데이터를 담을 배열

    // 첫 번째 행은 헤더이므로 1부터 시작
    for (let i = 1; i < rows.length; i++) {
        const row = rows[i];
        const checkBox = row.querySelector('input[type="checkbox"]');

        // 체크된 행만 보내기
        if (checkBox && checkBox.checked) {
            let member = {};
            const inputs = row.getElementsByTagName("input");
            const selects = row.getElementsByTagName("select");

            // 각 행의 셀에 있는 데이터 읽기
            for (let j = 0; j < inputs.length; j++) {
                const input = inputs[j];
                const name = input.name;
                // 'checkRow'는 불필요하므로, 이 값을 제외하고 처리
                if (name !== 'checkRow') {  // 'checkRow' 제외
                    const value = input.value;
                    member[name] = value;
                }
            }

            // select 요소의 값도 처리
            for (let j = 0; j < selects.length; j++) {
                const select = selects[j];
                const name = select.name;
                const value = select.value;
                member[name] = value;
            }

            // 선택된 행의 데이터가 있으면 배열에 추가
            membersData.push(member);
        }
    }

    // 데이터가 비어있지 않으면 서버로 전송
    if (membersData.length > 0) {
        formData.append('membersData', JSON.stringify(membersData)); // 배열을 JSON 문자열로 추가

        // 이미지 파일을 formData에 추가
        const imageInput = document.getElementById("imageFileInput");
        const imageFiles = imageInput.files;

        // 여러 이미지 파일을 처리
        for (let i = 0; i < imageFiles.length; i++) {
            const imageFile = imageFiles[i];

            // 각 이미지 파일에 대해 memberId와 일치하는 데이터 찾기
            let matchedMember = null;

            for (let j = 0; j < membersData.length; j++) {
                const memberId = membersData[j].memberId;

                // memberId와 파일명이 일치하는 경우 찾기
                if (imageFile.name.startsWith(memberId)) {
                    matchedMember = membersData[j];  // 일치하는 멤버 찾기
                    break;  // 첫 번째 일치하는 멤버만 찾으면 됨
                }
            }

            // 일치하는 멤버가 있으면 파일을 formData에 추가
            if (matchedMember) {
                //console.log("Appending image file", matchedMember.memberId, imageFile); // 디버깅
                formData.append(`imageFiles[${matchedMember.memberId}]`, imageFile); // key를 'imageFiles[memberId]'로 설정
            } else {
                //console.log("Skipping image file due to no matching memberId", imageFile); // 디버깅
            }
        }

        // 서버로 데이터 전송
        try {
            formData.append('companyId', companyId);
            const response = await fetch("/dev/addMember", {
                method: 'POST',
                body: formData
            });

            // 응답 상태 코드가 200(정상)인지 확인
            if (response.ok) {
                const data = await response.json();

                // 서버에서 성공적인 응답을 받은 경우
                if (data.status == "success") {
                    alert(data.message);
                    deleteSelectedRows();
                } else {
                    alert(data.message);
                }
            } else {
                // 서버가 에러 상태 코드를 반환한 경우
                const errorText = await response.text();
                alert(`서버 오류: ${errorText}`);
            }
        } catch (error) {
            alert("Error: " + error);
        }
    } else {
        alert("선택된 행이 없습니다.");
    }
};