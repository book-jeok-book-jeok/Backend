package com.ssafy.bjbj.api.readinggroup.dto.request;

import lombok.*;

@ToString(of = {"readingGroupSeq", "title", "content"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ReqReadingGroupBoardDto {

    private Long readingGroupSeq;

    private String title;

    private String content;
}
