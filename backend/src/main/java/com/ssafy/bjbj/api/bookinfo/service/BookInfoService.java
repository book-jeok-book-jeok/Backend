package com.ssafy.bjbj.api.bookinfo.service;

import com.ssafy.bjbj.api.bookinfo.dto.response.ResponseBookInfoDto;

public interface BookInfoService {

    ResponseBookInfoDto findResponseBookInfoDtoBySeq(Long seq);

}