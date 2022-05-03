package com.umc.footprint.src.notice;

import com.umc.footprint.config.BaseException;
import com.umc.footprint.src.model.Notice;
import com.umc.footprint.src.notice.model.GetNoticeListRes;
import com.umc.footprint.src.notice.model.GetNoticeRes;
import com.umc.footprint.src.notice.model.NoticeList;
import com.umc.footprint.src.repository.NoticeRepository;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.umc.footprint.config.BaseResponseStatus.INVALID_BADGEIDX;
import static com.umc.footprint.config.BaseResponseStatus.INVALID_NOTICE_IDX;

@Slf4j
@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;

    @Autowired
    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    public GetNoticeListRes getNoticeList(int page, int size) throws BaseException {
        // User에게 page와 size를 받아 Paging 처리된 notice Get
        Page<Notice> findNotice = noticeRepository.findAll(PageRequest.of(page-1,size));

        // find된 Notice가 없을때 exception
        if(findNotice.isEmpty()){
            throw new BaseException(INVALID_NOTICE_IDX);
        }

        // NoticList 형식으로 mapping 진행
        Page<NoticeList> map = findNotice.map(notice -> new NoticeList(notice.getNoticeIdx(), notice.getTitle(), notice.getCreateAt(), notice.getUpdateAt()));

        // 현재 페이지와 전체 페이지를 포함한 GetNoticeListRes 생성
        GetNoticeListRes getNoticeListRes = GetNoticeListRes.builder()
                .pageOn(page)
                .pageTotal(map.getTotalPages())
                .noticeList(map.getContent())
                .build();

        // Page map 안에있는 content만 return
        return getNoticeListRes;
    }

    public Optional<GetNoticeRes> getNotice(int page, int size, int offset) throws BaseException {
        /* index 계산(>=1)
        * page : 현재 page(>=1)
        * size : page의 size(>=1)
        * offset : page 안에서 원하는 공지의 index(>=1)
        * */
        int index = (page-1) * size + offset;

        // index를 사용하여 notice find
        Optional<Notice> noticeByIdx = noticeRepository.findById(index);

        // 찾은 Notice 정보를 GetNoticeRes DTO로 mapping
        Optional<GetNoticeRes> getNoticeRes = noticeByIdx.map(notice -> new GetNoticeRes(notice.getNoticeIdx(), notice.getTitle(), notice.getNotice(),
                notice.getImage(), notice.getCreateAt(), notice.getUpdateAt()));

        System.out.println("noticeByIdx = " + noticeByIdx);
        if(noticeByIdx.equals(Optional.empty())){
            throw new BaseException(INVALID_NOTICE_IDX);
        }

        return getNoticeRes;
    }


}
