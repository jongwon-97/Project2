package com.kosmo.nexus.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kosmo.nexus.dto.LoginDTO;
import com.kosmo.nexus.dto.SignupDTO;
import com.kosmo.nexus.service.AdminService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@Slf4j
public class AdminRestController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ServletContext servletContext;

    @PostMapping("/admin/addMember")
    public String addUser(@RequestParam("membersData") String membersDataJson,
                          @RequestParam Map<String, MultipartFile> imageFiles,
                          HttpSession ses, Model model) throws JsonProcessingException {

        imageFiles.forEach((key, value) -> log.info("Key: {}, Value: {}", key, value));
        String savedImagePath = null;  // 이미지 경로를 저장할 변수
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<SignupDTO> membersData = objectMapper.readValue(membersDataJson, new TypeReference<List<SignupDTO>>() {});

            Long sesCompanyId = getLoginUserCompanyId(ses, model);
            for (SignupDTO member : membersData) {
                member.setMemberRole("User");
                member.setMemberSns(0);
                member.setMemberStatus("활동");
                member.setCompanyId(sesCompanyId);

                // memberPhone이 null이 아니고 길이가 4 이상일 경우, 마지막 4글자를 비밀번호로 설정
                String memberPhone = member.getMemberPhone();  // 전화번호 가져오기
                if (memberPhone != null && memberPhone.length() >= 4) {
                    // 전화번호의 마지막 4글자를 비밀번호로 설정
                    String memberPw = memberPhone.substring(memberPhone.length() - 4);
                    member.setMemberPw(memberPw);  // 비밀번호 설정
                } else {
                    // 전화번호가 유효하지 않은 경우 기본값 설정 (선택 사항)
                    member.setMemberPw("0000");  // 기본값 "0000"으로 설정
                }
                // imageFiles에서 멤버 ID에 해당하는 파일 찾기
                String key = "imageFiles[" + member.getMemberId() + "]";  // 멤버 ID에 맞는 key 생성
                MultipartFile imageFile = imageFiles != null ? imageFiles.get(key) : null;  // 해당 key로 이미지 찾기

                // 이미지 파일이 존재하면 저장하고 경로를 설정
                if (imageFile != null && !imageFile.isEmpty()) {
                    String imagePath = saveImage(imageFile);
                    member.setMemberImgName(imagePath);  // 이미지 경로를 member 객체에 설정
                    log.info("이미지 경로===={}", member.getMemberImgName());
                }
            }
            log.info("List<SignupDTO>======{}",membersData);

            // 정규성 확인식 필요
            // 1. 데이터 내의 중복 확인(id, 사번)
            // 2. null 확인

            // signupDTO를 가져와서 전송하기
            int result = adminService.insertMemberList(membersData);



            // json 타입은 "(큰 따옴표)로 문자열을 표현함. Java도 문자열을 "(큰따옴표)로 나타내기 때문에, 아래와 같은 형식으로 작성해야함
            return "{\"status\":\"success\", \"message\":\"회원 등록이 완료되었습니다.\", \"loc\":\"/admin/memberList\"}";

        }catch (Exception e) {
            e.printStackTrace();

            // DB 업데이트 오류 나면, 올라갔던 파일을 삭제하는 코드
            if (savedImagePath != null) {
                try {
                    deleteImage(savedImagePath);  // 이미지 삭제
                } catch (IOException ioException) {
                    log.error("이미지 삭제 실패: {}", ioException.getMessage());
                }
            }

            return "Error: " + e.getMessage();
        }
    }

    public String saveImage(MultipartFile imageFile) throws IOException {
        // 스프링의 Resource로 파일 경로 지정
        String foldPath="/member_img";
        Path path = Paths.get(servletContext.getRealPath(foldPath));
        if (!Files.exists(path)) {
            Files.createDirectories(path);  // 경로가 없으면 디렉토리 생성
        }

        // 파일 이름 생성 (UUID + 원본 파일명)
        String originalFilename = imageFile.getOriginalFilename();
        //String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + "_" + originalFilename;

        // 파일 저장
        Path destination = path.resolve(fileName);
        Files.copy(imageFile.getInputStream(), destination);

        // 저장된 파일의 경로를 반환 (이 경로는 DB에 저장할 경로)
        return foldPath+"/" + fileName;
    }

    public void deleteImage(String imagePath) throws IOException {
        // imagePath가 상대 경로로 저장되었으므로, 절대 경로를 만들어야 할 수도 있습니다.
        Path path = Paths.get(servletContext.getRealPath(imagePath));

        // 파일이 존재하면 삭제
        if (Files.exists(path)) {
            Files.delete(path);
            log.info("이미지 파일 삭제됨: {}", imagePath);
        } else {
            log.warn("이미지 파일을 찾을 수 없음: {}", imagePath);
        }
    }

    public Long getLoginUserCompanyId(HttpSession ses, Model model){
        // 세션에서 loginUser 객체 가져오기
        LoginDTO loginUser = (LoginDTO) ses.getAttribute("loginUser");
        if (loginUser == null) {
            message(model, "정상적인 로그인 정보가 아닙니다.", "/logout");
            return null;
        }
        Long companyId = loginUser.getCompanyId();
        if (companyId == null) {     // memberId가 없는 경우
            message(model, "정상적인 로그인 정보가 아닙니다.", "/logout");
            return null;
        }
        return companyId;
    }

    public String message(Model model, String msg, String loc){
        model.addAttribute("msg", msg);
        model.addAttribute("loc", loc);
        return "message";
    }

}
