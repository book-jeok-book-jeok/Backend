package com.ssafy.bjbj.api.readinggroup.service;

import com.ssafy.bjbj.api.member.entity.Member;
import com.ssafy.bjbj.api.member.repository.MemberRepository;
import com.ssafy.bjbj.api.readinggroup.dto.request.ReqReadingGroupDto;
import com.ssafy.bjbj.api.readinggroup.dto.response.*;
import com.ssafy.bjbj.api.readinggroup.entity.ReadingGroup;
import com.ssafy.bjbj.api.readinggroup.entity.ReadingGroupDate;
import com.ssafy.bjbj.api.readinggroup.entity.ReadingGroupMember;
import com.ssafy.bjbj.api.readinggroup.enums.ReadingGroupReview;
import com.ssafy.bjbj.api.readinggroup.enums.ReadingGroupType;
import com.ssafy.bjbj.api.readinggroup.exception.AlreadyReviewedReadingGroupMemberException;
import com.ssafy.bjbj.api.readinggroup.exception.NotAcceptReviewOwnSelfException;
import com.ssafy.bjbj.api.readinggroup.exception.NotFoundReadingGroupException;
import com.ssafy.bjbj.api.readinggroup.exception.NotFoundReadingGroupMemberException;
import com.ssafy.bjbj.api.readinggroup.repository.*;
import com.ssafy.bjbj.common.enums.Day;
import com.ssafy.bjbj.common.exception.NotEqualMemberException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
@Service
public class ReadingGroupServiceImpl implements ReadingGroupService {

    private final ReadingGroupRepository readingGroupRepository;

    private final MemberRepository memberRepository;

    private final ReadingGroupDateRepository readingGroupDateRepository;

    private final ReadingGroupMemberRepository readingGroupMemberRepository;

    private final ReadingGroupBoardRepository readingGroupBoardRepository;

    private final ReadingGroupBoardCommentRepository readingGroupBoardCommentRepository;

    @Transactional
    @Override
    public ReadingGroup register(ReqReadingGroupDto reqReadingGroupDto, Long memberSeq) {
        String time = "T00:00:00";

        Member findMember = memberRepository.findBySeq(memberSeq);

        /**
         * 독서 모임 저장
         */
        ReadingGroup savedReadingGroup = readingGroupRepository.save(ReadingGroup.builder()
                .title(reqReadingGroupDto.getTitle())
                .content(reqReadingGroupDto.getContent())
                .limitLevel(reqReadingGroupDto.getLimitLevel())
                .maxMember(reqReadingGroupDto.getMaxMember())
                .deadline(LocalDateTime.parse(reqReadingGroupDto.getDeadline() + time))
                .startDate(LocalDateTime.parse(reqReadingGroupDto.getStartDate() + time))
                .endDate(LocalDateTime.parse(reqReadingGroupDto.getEndDate() + time))
                .readingGroupType(ReadingGroupType.valueOf(reqReadingGroupDto.getReadingGroupType().toUpperCase()))
                .member(findMember)
                .build());

        /**
         * 독서 모임 날짜 저장
         */
        registerReadingGroupDate(reqReadingGroupDto, savedReadingGroup);

        /**
         * 독서 모임 회원 저장
         */
        readingGroupMemberRepository.save(ReadingGroupMember.builder()
                .readingGroup(savedReadingGroup)
                .member(findMember)
                .build());

        return savedReadingGroup;
    }

    @Transactional
    @Override
    public ResReadingGroupDetailDto getResReadingGroupDetailDto(Long readingGroupSeq) {
        ReadingGroup findReadingGroup = readingGroupRepository.findBySeq(readingGroupSeq)
                .orElseThrow(() -> new NotFoundReadingGroupException("올바르지 않은 요청입니다."));

        /**
         * 조회수 증가
         */
        findReadingGroup.incrementViews();

        /**
         * 참여자 닉네임 세팅
         */
        List<String> participants = findReadingGroup.getReadingGroupMembers()
                .stream().map(readingGroupMember -> readingGroupMember.getMember().getNickname())
                .collect(Collectors.toList());

        List<Long> participantSeqs = findReadingGroup.getReadingGroupMembers()
                .stream().map(readingGroupMember -> readingGroupMember.getMember().getSeq())
                .collect(Collectors.toList());

        /**
         * 요일 정보 세팅
         */
        Set<Day> days = findReadingGroup.getReadingGroupDates()
                .stream().map(readingGroupDate -> Day.valueOf(readingGroupDate.getConferenceDate().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase()))
                .collect(Collectors.toSet());

        return ResReadingGroupDetailDto
                .builder()
                .readingGroupSeq(findReadingGroup.getSeq())
                .writer(findReadingGroup.getMember().getNickname())
                .title(findReadingGroup.getTitle())
                .content(findReadingGroup.getContent())
                .minLevel(findReadingGroup.getLimitLevel())
                .maxNumOfMember(findReadingGroup.getMaxMember())
                .views(findReadingGroup.getViews())
                .status(findReadingGroup.getStatus())
                .deadline(findReadingGroup.getDeadline().toLocalDate())
                .startDate(findReadingGroup.getStartDate().toLocalDate())
                .endDate(findReadingGroup.getEndDate().toLocalDate())
                .readingGroupType(findReadingGroup.getReadingGroupType())
                .createdDate(findReadingGroup.getCreatedDate().toLocalDate())
                .participantSeqs(participantSeqs)
                .participants(participants)
                .days(days)
                .build();
    }

    @Override
    public ResReadingGroupListPageDto getReadingGroupListPageDto(Pageable pageable) {
        Integer totalCnt = readingGroupRepository.countReadingGroup();
        Integer totalPage = (int) Math.ceil((double) totalCnt / pageable.getPageSize());
        List<ReadingGroupMiniDto> readingGroupMiniDtos = readingGroupRepository.findReadingGroupMiniDtos(pageable);

        return ResReadingGroupListPageDto.builder()
                .totalCnt(totalCnt)
                .totalPage(totalPage)
                .currentPage(pageable.getPageNumber())
                .readingGroupMiniDtos(readingGroupMiniDtos)
                .build();
    }

    @Override
    public boolean isMeetToday(Long readingGroupSeq, Long memberSeq) {
        if (!readingGroupRepository.existsBySeq(readingGroupSeq)) {
            throw new NotFoundReadingGroupException("존재하지 않는 독서모임입니다.");
        }

        if (!readingGroupMemberRepository.existsByReadingGroupSeqAndMemberSeq(readingGroupSeq, memberSeq)) {
            throw new NotFoundReadingGroupMemberException("참여한 독서 모임이 아닙니다.");
        }

        if (readingGroupRepository.findNotEndReadingGroupBySeq(readingGroupSeq) == null) {
            throw new IllegalStateException("이미 종료된 독서모임입니다.");
        }

        return readingGroupDateRepository.existsByReadingGroupSeqAndConferenceDate(readingGroupSeq, LocalDateTime.of(LocalDate.now(), LocalTime.parse("00:00:00")));
    }

    @Transactional
    @Override
    public long changeReadingGroupStatus() {
        int minNumOfMembers = 2;
        return readingGroupRepository.updateStatusPreToIng(minNumOfMembers) + readingGroupRepository.updateStatusPreToFail() + readingGroupRepository.updateStatusIngToEnd();
    }

    @Transactional
    @Override
    public void modify(Long readingGroupSeq, ReqReadingGroupDto reqReadingGroupDto, Long memberSeq) {
        ReadingGroup findReadingGroup = readingGroupRepository.findBySeq(readingGroupSeq)
                .orElseThrow(() -> new NotFoundReadingGroupException("존재하지 않는 독서 모임입니다."));

        if (!findReadingGroup.getMember().getSeq().equals(memberSeq)) {
            throw new NotEqualMemberException("올바르지 않운 요청입니다.");
        }

        // 독서 모임 데이터 수정
        findReadingGroup.change(reqReadingGroupDto);

        // 기존의 독서 모임 날짜 삭제
        readingGroupDateRepository.deleteAllByReadingGroupSeq(readingGroupSeq);

        /**
         * 독서 모임 날짜 저장
         */
        registerReadingGroupDate(reqReadingGroupDto, findReadingGroup);

    }

    private void registerReadingGroupDate(ReqReadingGroupDto reqReadingGroupDto, ReadingGroup readingGroup) {
        LocalDate startDate = LocalDate.parse(reqReadingGroupDto.getStartDate());
        LocalDate endDate = LocalDate.parse(reqReadingGroupDto.getEndDate());
        List<String> days = reqReadingGroupDto.getDays();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1L)) {
            String baseDay = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US);
            for (String day : days) {
                if (day.equalsIgnoreCase(baseDay)) {
                    readingGroupDateRepository.save(ReadingGroupDate.builder()
                            .conferenceDate(LocalDateTime.of(date, LocalTime.parse("00:00:00")))
                            .readingGroup(readingGroup)
                            .build());
                    break;
                }
            }
        }
    }

    @Transactional
    @Override
    public void removeOne(Long readingGroupSeq, Long memberSeq) {
        ReadingGroup findReadingGroup = readingGroupRepository.findBySeq(readingGroupSeq)
                .orElseThrow(() -> new NotFoundReadingGroupException("존재하지 않는 독서 모임입니다."));

        if (!findReadingGroup.getMember().getSeq().equals(memberSeq)) {
            throw new NotEqualMemberException("올바르지 않은 요청입니다.");
        }

        /**
         * 독서 모임 삭제
         */
        findReadingGroup.delete();

        /**
         * 독서 모임 회원 삭제
         */
        readingGroupMemberRepository.deleteAllByReadingGroupSeq(readingGroupSeq);

        /**
         * 독서 모임 날짜 삭제
         */
        readingGroupDateRepository.deleteAllByReadingGroupSeq(readingGroupSeq);

        /**
         * 독서 모임 게시글 삭제
         * isDeleted -> true
         */
        readingGroupBoardRepository.deleteAllByReadingGroupSeq(readingGroupSeq);

        /**
         * 독서 모임 게시글 댓글 삭제
         */
        readingGroupBoardCommentRepository.deleteAllByReadingGroupSeq(readingGroupSeq);
    }

    @Override
    public ResMyReadingGroupPageDto getResMyReadingGroupPageDto(Pageable pageable, Long memberSeq) {
        Integer totalCnt = readingGroupRepository.countMyReadingGroupByMemberSeq(memberSeq);
        Integer totalPage = (int) Math.ceil((double) totalCnt / pageable.getPageSize());
        List<MyReadingGroupDtoWithDays> myReadingGroupDtoWithDays = new ArrayList<>();

        List<MyReadingGroupDto> findMyReadingGroupDtos = readingGroupRepository.findMyReadingGroupDtosByMemberSeq(pageable, memberSeq);

        for(MyReadingGroupDto findMyReadingGroupDto : findMyReadingGroupDtos) {
            Set<Day> days = findMyReadingGroupDto.getReadingGroupDates()
                    .stream().map(LocalDateTime -> Day.valueOf(LocalDateTime.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase()))
                    .collect(Collectors.toSet());

            myReadingGroupDtoWithDays.add(new MyReadingGroupDtoWithDays().builder()
                    .readingGroupSeq(findMyReadingGroupDto.getReadingGroupSeq())
                    .title(findMyReadingGroupDto.getTitle())
                    .status(findMyReadingGroupDto.getStatus())
                    .readingGroupType(findMyReadingGroupDto.getReadingGroupType())
                    .isReviewed(findMyReadingGroupDto.isReviewed())
                    .days(days)
                    .build());
        }

        return ResMyReadingGroupPageDto.builder()
                .totalCnt(totalCnt)
                .totalPage(totalPage)
                .currentPage(pageable.getPageNumber())
                .myReadingGroupDtos(myReadingGroupDtoWithDays)
                .build();
    }

    @Transactional
    @Override
    public void reviewReadingGroupMember(Long readingGroupSeq, Map<String, ReadingGroupReview> reviews, Long memberSeq) {
        // 1. is Reviewed true로 변경
        ReadingGroupMember readingGroupMember = readingGroupMemberRepository.findByReadingGroupSeqAndMemberSeq(readingGroupSeq, memberSeq)
                .orElseThrow(() -> new NotFoundReadingGroupMemberException("올바르지 않은 요청입니다."));
        if (readingGroupMember.isReviewed()) {
            throw new AlreadyReviewedReadingGroupMemberException("이미 해당 독서 모임에 대한 리뷰를 완료한 사용자입니다.");
        }
        readingGroupMember.review();

        // 2. list의 길이만큼 돌면서 list 내의 회원 경험치 증감
        for (String reviewedMemberSeqString : reviews.keySet()) {
            // reviewedMemberSeq가 readingGroup에 없으면 에러
            readingGroupMemberRepository.findByReadingGroupSeqAndMemberSeq(readingGroupSeq, memberSeq)
                    .orElseThrow(() -> new NotFoundReadingGroupMemberException("올바르지 않은 요청입니다."));

            Long reviewedMemberSeq = Long.valueOf(reviewedMemberSeqString);
            if (Objects.equals(memberSeq, reviewedMemberSeq)) {
                throw new NotAcceptReviewOwnSelfException("올바르지 않은 요청입니다.");
            }

            Member reviewedMember = memberRepository.findBySeq(reviewedMemberSeq);

            if (reviews.get(reviewedMemberSeqString).equals(ReadingGroupReview.GOOD)) {
                reviewedMember.incrementExp(1);
            } else {
                reviewedMember.decrementExp(1);
            }
        }

        // 3. memberSeq의 경험치 +20 (완주)
        Member reviewingMember = memberRepository.findBySeq(memberSeq);
        reviewingMember.incrementExp(20);
    }

}
