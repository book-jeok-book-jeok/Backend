package com.ssafy.bjbj.api.booklog.repository;

import com.ssafy.bjbj.api.bookinfo.entity.BookInfo;
import com.ssafy.bjbj.api.bookinfo.repository.BookInfoRepository;
import com.ssafy.bjbj.api.booklog.dto.request.RequestBooklogDto;
import com.ssafy.bjbj.api.booklog.dto.response.OpenBooklogDto;
import com.ssafy.bjbj.api.booklog.entity.Booklog;
import com.ssafy.bjbj.api.booklog.entity.Like;
import com.ssafy.bjbj.api.member.entity.Member;
import com.ssafy.bjbj.api.member.entity.Role;
import com.ssafy.bjbj.api.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
class BooklogRepositoryTest {

    @Autowired
    private BooklogRepository booklogRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookInfoRepository bookInfoRepository;
    
    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private EntityManager em;

    private Member member1 = null;
    private Member member2 = null;

    private Booklog booklog1 = null;

    private BookInfo bookInfo1 = null;

    @BeforeEach
    public void setUp() {
        String email1 = "member1@bjbj.com";
        member1 = memberRepository.save(Member.builder()
                .email(email1)
                .password("password")
                .name("name")
                .nickname("member1")
                .phoneNumber("010-9999-1111")
                .exp(0)
                .point(100)
                .role(Role.MEMBER)
                .build());

        String email2 = "member2@bjbj.com";
        member2 = memberRepository.save(Member.builder()
                .email(email2)
                .password("password")
                .name("name")
                .nickname("member2")
                .phoneNumber("010-9999-2222")
                .exp(0)
                .point(100)
                .role(Role.MEMBER)
                .build());

        bookInfo1 = bookInfoRepository.findBySeq(2L);

        // 북로그 작성
        booklog1 = booklogRepository.save(Booklog.builder()
                .title("북로그 제목")
                .content(null)
                .summary(null)
                .starRating(null)
                .readDate(null)
                .isOpen(false)
                .views(0)
                .member(member1)
                .bookInfo(bookInfo1)
                .build());
    }

    @DisplayName("북로그 엔티티 등록 테스트")
    @Test
    public void booklog_register_test() {
        // 회원가입
        String email = "email@bjbj.com";
        Member savedMember = memberRepository.save(Member.builder()
                .email(email)
                .password("password")
                .name("name")
                .nickname("nickname")
                .phoneNumber("010-1234-5678")
                .exp(0)
                .point(100)
                .role(Role.MEMBER)
                .build());

        // 책 정보 저장
        /*
        책 정보의 경우 현재 등록 메서드가 없으므로 이미 db에 저장된 775번 책 정보를 사용
         */
        BookInfo savedBookInfo = bookInfoRepository.findBySeq(1L);

        // 북로그 작성
        Booklog booklog = Booklog.builder()
                .title("북로그 제목")
                .content("북로그 내용")
                .summary("북로그 한줄평")
                .starRating(4)
                .readDate(LocalDateTime.parse("2021-12-21" + "T00:00:00"))
                .isOpen(true)
                .views(0)
                .member(savedMember)
                .bookInfo(savedBookInfo)
                .build();

        booklogRepository.save(booklog);
        em.flush();
        em.clear();

        // 작성된 북로그 검증
        Booklog savedBooklog = booklogRepository.findBySeq(booklog.getSeq());
        assertThat(booklog.getMember().getSeq()).isEqualTo(savedBooklog.getMember().getSeq());
        assertThat(booklog.getBookInfo().getSeq()).isEqualTo(savedBooklog.getBookInfo().getSeq());
        assertThat(booklog.getTitle()).isEqualTo(savedBooklog.getTitle());
        assertThat(booklog.getContent()).isEqualTo(savedBooklog.getContent());
        assertThat(booklog.getSummary()).isEqualTo(savedBooklog.getSummary());
        assertThat(booklog.getStarRating()).isEqualTo(savedBooklog.getStarRating());
        assertThat(booklog.getReadDate()).isEqualTo(savedBooklog.getReadDate());
        assertThat(booklog.isOpen()).isEqualTo(savedBooklog.isOpen());
        assertThat(booklog.getViews()).isEqualTo(savedBooklog.getViews());
    }

    @DisplayName("북로그 엔티티 수정 테스트")
    @Test
    public void booklog_update_test() {
        // 회원가입
        String email = "email@bjbj.com";
        Member savedMember = memberRepository.save(Member.builder()
                .email(email)
                .password("password")
                .name("name")
                .nickname("nickname")
                .phoneNumber("010-1234-5678")
                .exp(0)
                .point(100)
                .role(Role.MEMBER)
                .build());

        // 책 정보 저장
        /*
        책 정보의 경우 현재 등록 메서드가 없으므로 이미 db에 저장된 775번 책 정보를 사용
         */
        BookInfo savedBookInfo = bookInfoRepository.findBySeq(1L);

        // 북로그 작성
        Booklog booklog = Booklog.builder()
                .title("북로그 제목")
                .content(null)
                .summary(null)
                .starRating(null)
                .readDate(null)
                .isOpen(true)
                .views(0)
                .member(savedMember)
                .bookInfo(savedBookInfo)
                .build();

        booklogRepository.save(booklog);
        em.flush();
        em.clear();

        // 북로그 수정
        Booklog findBooklog = booklogRepository.findBySeq(booklog.getSeq());
        RequestBooklogDto reqBooklogDto = RequestBooklogDto.builder()
                .memberSeq(savedMember.getSeq())
                .bookInfoSeq(1L)
                .title("북로그 제목2")
                .content("북로그 내용2")
                .summary("북로그 한줄평2")
                .starRating(4)
                .readDate("2021-12-21")
                .isOpen(true)
                .build();
        findBooklog.changeBooklog(reqBooklogDto);
        em.flush();
        em.clear();

        // 수정된 북로그 검증
        Booklog savedBooklog = booklogRepository.findBySeq(findBooklog.getSeq());
        assertThat(findBooklog.getMember().getSeq()).isEqualTo(savedBooklog.getMember().getSeq());
        assertThat(findBooklog.getBookInfo().getSeq()).isEqualTo(savedBooklog.getBookInfo().getSeq());
        assertThat(findBooklog.getTitle()).isEqualTo(savedBooklog.getTitle());
        assertThat(findBooklog.getContent()).isEqualTo(savedBooklog.getContent());
        assertThat(findBooklog.getSummary()).isEqualTo(savedBooklog.getSummary());
        assertThat(findBooklog.getStarRating()).isEqualTo(savedBooklog.getStarRating());
        assertThat(findBooklog.getReadDate()).isEqualTo(savedBooklog.getReadDate());
        assertThat(findBooklog.isOpen()).isEqualTo(savedBooklog.isOpen());
        assertThat(findBooklog.getViews()).isEqualTo(savedBooklog.getViews());
    }

    @DisplayName("북로그 엔티티 공개여부 수정 테스트")
    @Test
    public void booklogEntityIsOpenChangeTest() {
        // 수정
        booklog1.changeIsOpen(true);
        em.flush();
        em.clear();

        // 검증
        Booklog changedBooklog = booklogRepository.findBySeq(booklog1.getSeq());
        assertThat(changedBooklog.isOpen()).isTrue();
    }

    @DisplayName("최근 일주일 공개 북로그 개수 조회 테스트")
    @Test
    public void recentOpenBooklogCountTest() {
        booklogRepository.deleteAll();

        Integer count = booklogRepository.countByOpenBooklogAndRecentOneWeek();
        assertThat(count).isEqualTo(0);

        booklogRepository.save(Booklog.builder()
                .title("북로그 제목1")
                .content(null)
                .summary(null)
                .starRating(null)
                .readDate(null)
                .isOpen(false)
                .views(0)
                .member(member1)
                .bookInfo(bookInfo1)
                .build());
        em.flush();
        em.clear();
        count = booklogRepository.countByOpenBooklogAndRecentOneWeek();
        assertThat(count).isEqualTo(0);

        em.flush();
        em.clear();

        booklogRepository.save(Booklog.builder()
                .title("북로그 제목2")
                .content(null)
                .summary(null)
                .starRating(null)
                .readDate(null)
                .isOpen(true)
                .views(0)
                .member(member1)
                .bookInfo(bookInfo1)
                .build());
        em.flush();
        em.clear();
        count = booklogRepository.countByOpenBooklogAndRecentOneWeek();
        assertThat(count).isEqualTo(1);
    }

    @DisplayName("최근 일주일 공개 북로그 목록 조회 테스트")
    @Test
    public void RecentOpenBooklogListTest() throws InterruptedException {
        booklogRepository.deleteAll();

        Pageable pageableRecent = PageRequest.of(1, 10, Sort.Direction.ASC, "recent");
        Pageable pageableLike = PageRequest.of(1, 10, Sort.Direction.ASC, "like");
        List<OpenBooklogDto> find1 = booklogRepository.findOpenBooklogDtos(pageableRecent);
        assertThat(find1).isEmpty();

        Booklog savedBooklog1 = booklogRepository.save(Booklog.builder()
                .title("북로그 제목1")
                .content(null)
                .summary(null)
                .starRating(null)
                .readDate(null)
                .isOpen(true)
                .views(0)
                .member(member1)
                .bookInfo(bookInfo1)
                .build());
        Thread.sleep(1000);

        Booklog savedBooklog2 = booklogRepository.save(Booklog.builder()
                .title("북로그 제목2")
                .content(null)
                .summary(null)
                .starRating(null)
                .readDate(null)
                .isOpen(true)
                .views(0)
                .member(member1)
                .bookInfo(bookInfo1)
                .build());
        Thread.sleep(1000);

        Booklog savedBooklog3 = booklogRepository.save(Booklog.builder()
                .title("북로그 제목3")
                .content(null)
                .summary(null)
                .starRating(null)
                .readDate(null)
                .isOpen(true)
                .views(0)
                .member(member1)
                .bookInfo(bookInfo1)
                .build());
        Thread.sleep(1000);

        // booklog1 좋아요 2개
        likeRepository.save(Like.builder()
                .booklog(savedBooklog1)
                .member(member1)
                .build());
        likeRepository.save(Like.builder()
                .booklog(savedBooklog1)
                .member(member2)
                .build());
        // booklog2 좋아요 1개
        likeRepository.save(Like.builder()
                .booklog(savedBooklog2)
                .member(member1)
                .build());
        em.flush();
        em.clear();

        // 최신순 : 3 -> 2 -> 1
        List<OpenBooklogDto> find2 = booklogRepository.findOpenBooklogDtos(pageableRecent);
        assertThat(find2.size()).isEqualTo(3);
        assertThat(find2.get(0).getBooklogSeq()).isEqualTo(savedBooklog3.getSeq());
        assertThat(find2.get(1).getBooklogSeq()).isEqualTo(savedBooklog2.getSeq());
        assertThat(find2.get(2).getBooklogSeq()).isEqualTo(savedBooklog1.getSeq());

        // 좋아요순 : 1 -> 2 -> 3
        List<OpenBooklogDto> find3 = booklogRepository.findOpenBooklogDtos(pageableLike);
        assertThat(find3.size()).isEqualTo(3);
        assertThat(find3.get(0).getBooklogSeq()).isEqualTo(savedBooklog1.getSeq());
        assertThat(find3.get(1).getBooklogSeq()).isEqualTo(savedBooklog2.getSeq());
        assertThat(find3.get(2).getBooklogSeq()).isEqualTo(savedBooklog3.getSeq());
    }

}