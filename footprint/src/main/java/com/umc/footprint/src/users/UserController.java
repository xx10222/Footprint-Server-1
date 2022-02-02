package com.umc.footprint.src.users;

import com.umc.footprint.src.users.model.GetUserTodayRes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.umc.footprint.src.users.model.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import com.umc.footprint.config.BaseException;
import com.umc.footprint.config.BaseResponse;

import com.umc.footprint.config.BaseResponseStatus;
import com.umc.footprint.config.BaseResponseStatus.*;


@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private final UserProvider userProvider;
    @Autowired
    private final UserService userService;

    public UserController(UserProvider userProvider, UserService userService) {
        this.userProvider = userProvider;
        this.userService = userService;
    }

    /**
     * 유저 오늘 산책관련 정보 조회 API
     * [GET] /users/:userIdx/today
     */
    // Path-variable
    @ResponseBody
    @GetMapping("/{useridx}/today")
    public BaseResponse<List<GetUserTodayRes>> getToday(@PathVariable("useridx") int userIdx){
        try{
            List<GetUserTodayRes> userTodayRes = userProvider.getUserToday(userIdx);

            return new BaseResponse<>(userTodayRes);
        } catch (BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 유저 날짜별 산책관련 정보 조회 API
     * [GET] /users/:userIdx/:date
     */
    // Path-variable
    @ResponseBody
    @GetMapping("/{useridx}/{date}")
    public BaseResponse<List<GetUserDateRes>> getDateWalk(@PathVariable("useridx") int userIdx,@PathVariable("date") String date){

        // Validation 1. 날짜 형식 검사
        if(!date.matches("^\\d{4}\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])$")){
            return new BaseResponse<>(new BaseException(BaseResponseStatus.INVALID_DATE).getStatus());
        }

        // Provider 연결
        try{
            List<GetUserDateRes> userDateRes = userProvider.getUserDate(userIdx,date);

            return new BaseResponse<>(userDateRes);
        } catch (BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 유저 정보 조회 API
     * [GET] /users/:userIdx
     */
    // Path-variable
    @ResponseBody
    @GetMapping("/{userIdx}") // (GET) 127.0.0.1:3000/users/:userIdx
    public BaseResponse<GetUserRes> getUser(@PathVariable("userIdx") int userIdx) {
        try {
            GetUserRes getUserRes = userProvider.getUser(userIdx);
            return new BaseResponse<>(getUserRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }

    }

    /**
     * 유저 닉네임 변경 API
     * [PATCH] /users/:userIdx/nickname
     */
    @ResponseBody
    @PatchMapping("/{userIdx}/nickname")
    public BaseResponse<String> modifyNickname(@PathVariable("userIdx") int userIdx, @RequestBody User user) {
        try {
            PatchNicknameReq patchNicknameReq = new PatchNicknameReq(userIdx, user.getNickname());
            if (user.getNickname().length() > 8) { // 닉네임 8자 초과
                throw new BaseException(BaseResponseStatus.MAX_NICKNAME_LENGTH);
            }
            userService.modifyNickname(patchNicknameReq);

            String result = "닉네임이 수정되었습니다.";
            
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /*
     * 유저 "이번달" 목표 조회 API
     * [GET] /users/:userIdx/goals
     */
    // Path-variable
    @ResponseBody
    @GetMapping("/{userIdx}/goals") // (GET) 127.0.0.1:3000/users/:userIdx/goals
    public BaseResponse<GetUserGoalRes> getUserGoal(@PathVariable("userIdx") int userIdx) {
        try {
            GetUserGoalRes getUserGoalRes = userProvider.getUserGoal(userIdx);
            return new BaseResponse<>(getUserGoalRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }

    }

    /**
     * 유저 "다음달" 목표 조회 API
     * [GET] /users/:userIdx/goals/next
     */
    // Path-variable
    @ResponseBody
    @GetMapping("/{userIdx}/goals/next") // (GET) 127.0.0.1:3000/users/:userIdx/goals/next
    public BaseResponse<GetUserGoalRes> getUserGoalNext(@PathVariable("userIdx") int userIdx) {
        try {
            GetUserGoalRes getUserGoalRes = userProvider.getUserGoalNext(userIdx);
            return new BaseResponse<>(getUserGoalRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }

    }


    @ResponseBody
    @GetMapping("/{userIdx}/tmonth") // (GET) 127.0.0.1:3000/users/{userIdx}/tmonth
    public BaseResponse<GetMonthInfoRes> getMonthInfo(@PathVariable("userIdx") int userIdx) {
        // TO-DO-LIST
        // jwt 확인?
        // user테이블에 해당 userIdx가 존재하는지
        // GoalDay 테이블에 해당 userIdx가 존재하는지

        try {
            LocalDate now = LocalDate.now();
            int nowYear = now.getYear();
            int nowMonth = now.getMonthValue();

            GetMonthInfoRes getMonthInfoRes = userProvider.getMonthInfoRes(userIdx, nowYear, nowMonth);
            return new BaseResponse<>(getMonthInfoRes);
          } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
      
    }
  

    /**
     * 목표 수정 API
     * [PATCH] /users/:useridx/goals
     */
    // Path-variable
    @ResponseBody
    @PatchMapping("/{useridx}/goals") // [PATCH] /users/:useridx/goals
    public BaseResponse<String> modifyGoal(@PathVariable("useridx") int userIdx, @RequestBody PatchUserGoalReq patchUserGoalReq){
      // Validaion 1. userIdx 가 0 이하일 경우 exception
        if(userIdx <= 0)
            return new BaseResponse<>(new BaseException(BaseResponseStatus.INVALID_USERIDX).getStatus());

        // Validaion 2. dayIdx 길이 확인
        if(patchUserGoalReq.getDayIdx().size() == 0) // 요일 0개 선택
            return new BaseResponse<>(new BaseException(BaseResponseStatus.MIN_DAYIDX).getStatus());
        if(patchUserGoalReq.getDayIdx().size() > 7)  // 요일 7개 초과 선택
            return new BaseResponse<>(new BaseException(BaseResponseStatus.MAX_DAYIDX).getStatus());

        // Validaion 3. dayIdx 숫자 범위 확인
        for (Integer dayIdx : patchUserGoalReq.getDayIdx()){
            if (dayIdx > 7 || dayIdx < 1)
                return new BaseResponse<>(new BaseException(BaseResponseStatus.INVALID_DAYIDX).getStatus());
        }

        // Validaion 4. dayIdx 중복된 숫자 확인
        Set<Integer> setDayIDx = new HashSet<>(patchUserGoalReq.getDayIdx());
        if(patchUserGoalReq.getDayIdx().size() != setDayIDx.size()) // dayIdx 크기를 set으로 변형시킨 dayIdx 크기와 비교. 크기가 다르면 중복된 값 존재
            return new BaseResponse<>(new BaseException(BaseResponseStatus.OVERLAP_DAYIDX).getStatus());

        // Validaion 5. walkGoalTime 범위 확인
        if(patchUserGoalReq.getWalkGoalTime() < 10) // 최소 산책 목표 시간 미만
            return new BaseResponse<>(new BaseException(BaseResponseStatus.MIN_WALK_GOAL_TIME).getStatus());
        if(patchUserGoalReq.getWalkGoalTime() > 240) // 최대 산책 목표 시간 초과
            return new BaseResponse<>(new BaseException(BaseResponseStatus.MAX_WALK_GOAL_TIME).getStatus());

        // Validaion 6. walkTimeSlot 범위 확인
        if(patchUserGoalReq.getWalkTimeSlot() > 7 || patchUserGoalReq.getWalkTimeSlot() < 1)
            return new BaseResponse<>(new BaseException(BaseResponseStatus.INVALID_WALK_TIME_SLOT).getStatus());
       
       try {
            userService.modifyGoal(userIdx, patchUserGoalReq);

            String result ="목표가 수정되었습니다.";

            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }


    @ResponseBody
    @GetMapping("/{userIdx}/months/footprints") // (GET) 127.0.0.1:3000/users/{userIdx}/months/footprints?year=2021&month=2
    public BaseResponse<List<GetFootprintCount>> getMonthFootprints(@PathVariable("userIdx") int userIdx,@RequestParam(required = true) int year, @RequestParam(required = true) int month) throws BaseException {
        List<GetFootprintCount> getFootprintCounts = userProvider.getMonthFootprints(userIdx, year, month);
        return new BaseResponse<>(getFootprintCounts);
    }


    @ResponseBody
    @GetMapping("/{userIdx}/badges") // (GET) 127.0.0.1:3000/users/{userIdx}/badges
    public BaseResponse<GetUserBadges> getUsersBadges(@PathVariable("userIdx") int userIdx) throws BaseException {
            GetUserBadges getUserBadges = userProvider.getUserBadges(userIdx);
            return new BaseResponse<>(getUserBadges);
    }

    @ResponseBody
    @PatchMapping("/{userIdx}/badges/title/{badgeIdx}")
    public BaseResponse<BadgeInfo> patchRepBadge(@PathVariable("userIdx") int userIdx, @PathVariable("badgeIdx") int badgeIdx) throws BaseException {
        BadgeInfo patchRepBadgeInfo = userService.patchRepBadge(userIdx, badgeIdx);
        return new BaseResponse<>(patchRepBadgeInfo);
    }


    /**
     * 유저 세부 정보 조회 API
     * [GET] /users/:userIdx/infos
     */
    // Path-variable
    @ResponseBody
    @GetMapping("/{userIdx}/infos") // (GET) 127.0.0.1:3000/users/:userIdx/infos
    public BaseResponse<GetUserInfoRes> getUserInfo(@PathVariable("userIdx") int userIdx) {
        try {
            GetUserInfoRes getUserInfoRes = userProvider.getUserInfo(userIdx);
            return new BaseResponse<>(getUserInfoRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }

    }

   /**
     * 초기 정보 등록 API
     * [POST] /users/:useridx/infos
     */
    // Path-variable
    @ResponseBody
    @PostMapping("/{useridx}/infos") // [POST] /users/:useridx/goals
    public BaseResponse<String> postGoal(@PathVariable("useridx") int userIdx, @RequestBody PatchUserInfoReq patchUserInfoReq){

        // Validation 0. 날짜 형식 검사
        if(!patchUserInfoReq.getBirth().matches("^\\d{4}\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])$")){
            return new BaseResponse<>(new BaseException(BaseResponseStatus.INVALID_DATE).getStatus());
        }

        // Validaion 1. userIdx 가 0 이하일 경우 exception
        if(userIdx <= 0)
            return new BaseResponse<>(new BaseException(BaseResponseStatus.INVALID_USERIDX).getStatus());

        // Validaion 2. dayIdx 길이 확인
        if(patchUserInfoReq.getDayIdx().size() == 0) // 요일 0개 선택
            return new BaseResponse<>(new BaseException(BaseResponseStatus.MIN_DAYIDX).getStatus());
        if(patchUserInfoReq.getDayIdx().size() > 7)  // 요일 7개 초과 선택
            return new BaseResponse<>(new BaseException(BaseResponseStatus.MAX_DAYIDX).getStatus());

        // Validaion 3. dayIdx 숫자 범위 확인
        for (Integer dayIdx : patchUserInfoReq.getDayIdx()){
            if (dayIdx > 7 || dayIdx < 1)
                return new BaseResponse<>(new BaseException(BaseResponseStatus.INVALID_DAYIDX).getStatus());
        }

        // Validaion 4. dayIdx 중복된 숫자 확인
        Set<Integer> setDayIDx = new HashSet<>(patchUserInfoReq.getDayIdx());
        if(patchUserInfoReq.getDayIdx().size() != setDayIDx.size()) // dayIdx 크기를 set으로 변형시킨 dayIdx 크기와 비교. 크기가 다르면 중복된 값 존재
            return new BaseResponse<>(new BaseException(BaseResponseStatus.OVERLAP_DAYIDX).getStatus());

        // Validaion 5. walkGoalTime 범위 확인
        if(patchUserInfoReq.getWalkGoalTime() < 10) // 최소 산책 목표 시간 미만
            return new BaseResponse<>(new BaseException(BaseResponseStatus.MIN_WALK_GOAL_TIME).getStatus());
        if(patchUserInfoReq.getWalkGoalTime() > 240) // 최대 산책 목표 시간 초과
            return new BaseResponse<>(new BaseException(BaseResponseStatus.MAX_WALK_GOAL_TIME).getStatus());

        // Validaion 6. walkTimeSlot 범위 확인
        if(patchUserInfoReq.getWalkTimeSlot() > 7 || patchUserInfoReq.getWalkTimeSlot() < 1)
            return new BaseResponse<>(new BaseException(BaseResponseStatus.INVALID_WALK_TIME_SLOT).getStatus());


        try {
            int result = userService.postUserInfo(userIdx, patchUserInfoReq);

            String resultMsg = "정보 저장에 성공하였습니다.";
            if(result == 0)
                resultMsg = "정보 저장에 실패하였습니다.";

            return new BaseResponse<>(resultMsg);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 태그 검색 API
     * [GET] /users/:useridx/tags?tag=""
     */
    // Query String
    @ResponseBody
    @GetMapping("/{userIdx}/tags")
    public BaseResponse<List<GetTagRes>> getTags(@PathVariable("userIdx") int userIdx, @RequestParam(required = false) String tag) {
        try {
            if (tag == null) { // Query String(검색어)를 입력하지 않았을 경우
                return new BaseResponse<>(new BaseException(BaseResponseStatus.NEED_TAG_INFO).getStatus());
            }
            List<GetTagRes> tagResult = userProvider.getTagResult(userIdx, tag);
            return new BaseResponse<>(tagResult);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }

    }

}