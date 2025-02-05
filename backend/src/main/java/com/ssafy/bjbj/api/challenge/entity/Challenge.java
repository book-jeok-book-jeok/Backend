package com.ssafy.bjbj.api.challenge.entity;

import com.ssafy.bjbj.api.challenge.dto.request.ReqChallengeDto;
import com.ssafy.bjbj.api.challenge.dto.response.ResChallengeDto;
import com.ssafy.bjbj.api.member.entity.Member;
import com.ssafy.bjbj.common.enums.Status;
import com.ssafy.bjbj.common.entity.base.BaseLastModifiedEntity;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.LAZY;

@Getter
@ToString(of = {"seq", "title", "content", "views", "deadline", "startDate", "endDate", "maxMember", "reward", "status", "isDeleted", "isRewarded"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_challenge")
@Entity
public class Challenge extends BaseLastModifiedEntity {

    @Column(name = "challenge_seq", columnDefinition = "BIGINT UNSIGNED")
    @GeneratedValue
    @Id
    private Long seq;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, columnDefinition = "INT UNSIGNED")
    private Integer views;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false, columnDefinition = "INT UNSIGNED")
    private Integer maxMember;

    @Column(nullable = false, columnDefinition = "INT UNSIGNED")
    private Integer reward;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private boolean isDeleted;

    @Column(nullable = false)
    private boolean isRewarded;

    @JoinColumn(name = "member_seq", nullable = false)
    @ManyToOne(fetch = LAZY)
    private Member member;

    @OneToMany(mappedBy = "challenge", cascade = ALL)
    private List<ChallengeAuth> challengeAuths = new ArrayList<>();

    // 챌린지에 참여한 사람들
    @OneToMany(mappedBy = "challenge", cascade = ALL)
    private List<ChallengeMember> challengeMembers = new ArrayList<>();

    @Builder
    public Challenge(String title, String content, LocalDateTime deadline, LocalDateTime startDate, LocalDateTime endDate, Integer maxMember, Integer reward, Member member) {
        this.title = title;
        this.content = content;
        this.views = 0;
        this.deadline = deadline;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxMember = maxMember;
        this.reward = reward;
        this.status = Status.PRE;
        this.isDeleted = false;
        this.isRewarded = false;
        this.member = member;
    }

    public void incrementViews() {
        this.views++;
    }

    public Challenge change(ReqChallengeDto reqChallengeDto) {
        String time = "T00:00:00";

        this.title = reqChallengeDto.getTitle();
        this.content = reqChallengeDto.getContent();
        this.deadline = LocalDateTime.parse(reqChallengeDto.getDeadline() + time);
        this.startDate = LocalDateTime.parse(reqChallengeDto.getStartDate() + time);
        this.endDate = LocalDateTime.parse(reqChallengeDto.getEndDate() + time);
        this.maxMember = reqChallengeDto.getMaxMember();
        this.reward = reqChallengeDto.getReward();

        return this;
    }

    public ResChallengeDto toDto() {
        return toDto(null, null);
    }

    public ResChallengeDto toDto(List<Long> participantSeqs, List<String> participantNicknames) {
        return ResChallengeDto.builder()
                .challengeSeq(this.seq)
                .writerSeq(this.member.getSeq())
                .title(this.title)
                .content(this.content)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .deadline(this.deadline)
                .reward(this.reward)
                .maxMember(this.maxMember)
                .views(this.views)
                .participantSeqs(participantSeqs)
                .participantNicknames(participantNicknames)
                .build();
    }

    public void delete() {
        this.isDeleted = true;
    }

    public void rewarded() {
        this.isRewarded = true;
    }

}
