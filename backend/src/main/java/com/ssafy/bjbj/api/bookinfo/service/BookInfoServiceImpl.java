package com.ssafy.bjbj.api.bookinfo.service;

import com.ssafy.bjbj.api.bookinfo.dto.response.ResponseBookInfoDto;
import com.ssafy.bjbj.api.bookinfo.repository.BookInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
@Service
public class BookInfoServiceImpl implements BookInfoService {

    private final BookInfoRepository bookInfoRepository;

    @Override
    public ResponseBookInfoDto findResponseBookInfoDtoBySeq(Long seq) {
        return bookInfoRepository.findResponseBookInfoDtoBySeq(seq);
    }
}