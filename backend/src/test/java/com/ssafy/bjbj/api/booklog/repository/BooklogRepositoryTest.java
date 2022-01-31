package com.ssafy.bjbj.api.booklog.repository;

import com.ssafy.bjbj.api.bookinfo.entity.BookInfo;
import com.ssafy.bjbj.api.bookinfo.repository.BookInfoRepository;
import com.ssafy.bjbj.api.booklog.entity.Booklog;
import com.ssafy.bjbj.api.member.entity.Member;
import com.ssafy.bjbj.api.member.entity.Role;
import com.ssafy.bjbj.api.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;

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
    private EntityManager em;

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
        BookInfo savedBookInfo = bookInfoRepository.findBySeq(775L);

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

        System.out.println("booklog.getCreatedDate() = " + booklog.getCreatedDate());
        System.out.println("booklog.getCreatedBy() = " + booklog.getCreatedBy());
        Booklog savedBooklog = booklogRepository.save(booklog);
        em.flush();
        em.clear();

        // 작성된 북로그 검증
        assertThat(booklog).isEqualTo(savedBooklog);
    }

}