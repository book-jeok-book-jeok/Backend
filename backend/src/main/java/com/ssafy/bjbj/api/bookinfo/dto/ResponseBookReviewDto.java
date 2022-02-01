package com.ssafy.bjbj.api.bookinfo.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;
import com.ssafy.bjbj.api.bookinfo.entity.BookInfo;
import com.ssafy.bjbj.api.member.entity.Member;
import lombok.*;

import java.time.LocalDateTime;

@ToString(of = {"startRating", "summary", "createdDate"})
@NoArgsConstructor
@Getter
public class ResponseBookReviewDto {

    @JsonIgnore
    private Long seq;

    private Long bookInfoSeq;

    private Long memberSeq;

    private String bookTitle;

    private String bookAuthor;

    private String memberNickname;

    private Integer starRating;

    private String summary;

    private LocalDateTime createdDate;

    @QueryProjection
    @Builder
    public ResponseBookReviewDto(Long seq, Long bookInfoSeq, Long memberSeq, String bookTitle, String bookAuthor, String memberNickname, Integer starRating, String summary, LocalDateTime createdDate) {
        this.seq = seq;
        this.bookInfoSeq = bookInfoSeq;
        this.memberSeq = memberSeq;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.memberNickname = memberNickname;
        this.starRating = starRating;
        this.summary = summary;
        this.createdDate = createdDate;
    }
}