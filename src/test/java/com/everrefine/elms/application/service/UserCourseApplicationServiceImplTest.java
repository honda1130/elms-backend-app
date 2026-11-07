package com.everrefine.elms.application.service;

import static org.junit.jupiter.api.Assertions.*;

import com.everrefine.elms.application.dto.UserCourseDto;
import com.everrefine.elms.application.exception.ResourceNotFoundException;
import com.everrefine.elms.testsupport.TestDataFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("dev")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Testcontainers
@Transactional
public class UserCourseApplicationServiceImplTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

  @Autowired private TestDataFactory testData;

  @Autowired private UserCourseApplicationService userCourseApplicationService;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Nested
  class コース一覧取得 {
    @Test
    void 進捗付きのコース一覧が返ること() {
      // 1. usersテーブルに1ユーザー作る
      UUID userId = testData.createUser("ul-not-done@example.com", "p", "太郎", "ulnd", "GENERAL");
      // 2. coursesテーブルに2コース作る
      UUID courseId1 = testData.createCourse(new BigDecimal("1001"), "ULテストコース", "コース説明");
      UUID courseId2 = testData.createCourse(new BigDecimal("1002"), "ULテストコース2", "コース説明2");
      UUID lessonGroupId1 =
          testData.createLessonGroup(courseId1, new BigDecimal(1), "ULテストコースのグループ");
      UUID lessonGroupId2 =
          testData.createLessonGroup(courseId2, new BigDecimal(2), "ULテストコース2のグループ");

      // 3. lessonsテーブルレッスンを3個（1個と2個）作る
      UUID completedLessonId1 =
          testData.createLesson(
              lessonGroupId1,
              courseId1,
              new BigDecimal("1"),
              "UL完了レッスン1",
              "説明",
              "https://example.com/video.mp4");

      UUID completedLessonId2 =
          testData.createLesson(
              lessonGroupId2,
              courseId2,
              new BigDecimal("1"),
              "UL完了レッスン2",
              "説明",
              "https://example.com/video.mp4");

      testData.createLesson(
          lessonGroupId2,
          courseId2,
          new BigDecimal("2"),
          "UL未完了レッスン1",
          "説明",
          "https://example.com/video.mp4");
      // 4. userLessonsテーブルに2個（1個と1個）作る
      testData.createUserLesson(userId, completedLessonId1);
      testData.createUserLesson(userId, completedLessonId2);

      List<UserCourseDto> result = userCourseApplicationService.findUserCourses(userId);
      assertNotNull(result);
      UserCourseDto course1 =
          result.stream().filter(dto -> dto.title().equals("ULテストコース")).findFirst().orElseThrow();
      UserCourseDto course2 =
          result.stream().filter(dto -> dto.title().equals("ULテストコース2")).findFirst().orElseThrow();
      assertEquals(courseId1, course1.id());
      assertEquals(new BigDecimal("100.0"), course1.courseProgress());
      assertEquals(courseId2, course2.id());
      assertEquals(new BigDecimal("50.0"), course2.courseProgress());
    }

    @Test
    void レッスンが0件のコースの進捗率が0であること() {
      UUID userId = testData.createUser("ul-no-lesson@example.com", "p", "次郎", "ulnl", "GENERAL");
      UUID courseId = testData.createCourse(new BigDecimal("1003"), "UL空コース", "コース説明");

      List<UserCourseDto> result = userCourseApplicationService.findUserCourses(userId);

      assertEquals(new BigDecimal("0.0"), findCourse(result, courseId).courseProgress());
    }

    @Test
    void 完了したレッスンが0件のコースの進捗率が0であること() {
      UUID userId = testData.createUser("ul-no-done@example.com", "p", "三郎", "ulnd2", "GENERAL");
      UUID courseId = testData.createCourse(new BigDecimal("1004"), "UL未受講コース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal(1), "UL未受講コースのグループ");
      createLesson(lessonGroupId, courseId, "1");
      createLesson(lessonGroupId, courseId, "2");

      List<UserCourseDto> result = userCourseApplicationService.findUserCourses(userId);

      assertEquals(new BigDecimal("0.0"), findCourse(result, courseId).courseProgress());
    }

    @Test
    void 他のユーザーが完了したレッスンは進捗率に含まれないこと() {
      UUID userId = testData.createUser("ul-self@example.com", "p", "五郎", "ulself", "GENERAL");
      UUID otherUserId =
          testData.createUser("ul-other@example.com", "p", "六郎", "ulother", "GENERAL");
      UUID courseId = testData.createCourse(new BigDecimal("1005"), "UL共有コース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal(1), "UL共有コースのグループ");
      UUID lessonId1 = createLesson(lessonGroupId, courseId, "1");
      UUID lessonId2 = createLesson(lessonGroupId, courseId, "2");
      testData.createUserLesson(userId, lessonId1);
      testData.createUserLesson(otherUserId, lessonId1);
      testData.createUserLesson(otherUserId, lessonId2);

      List<UserCourseDto> result = userCourseApplicationService.findUserCourses(userId);

      assertEquals(new BigDecimal("50.0"), findCourse(result, courseId).courseProgress());
    }

    @Test
    void 割り切れない進捗率は小数点第1位で切り捨てられること() {
      UUID userId = testData.createUser("ul-third@example.com", "p", "七郎", "ulthird", "GENERAL");
      UUID courseId = testData.createCourse(new BigDecimal("1006"), "UL3レッスンコース", "コース説明");
      UUID lessonGroupId =
          testData.createLessonGroup(courseId, new BigDecimal(1), "UL3レッスンコースのグループ");
      UUID lessonId = createLesson(lessonGroupId, courseId, "1");
      createLesson(lessonGroupId, courseId, "2");
      createLesson(lessonGroupId, courseId, "3");
      testData.createUserLesson(userId, lessonId);

      List<UserCourseDto> result = userCourseApplicationService.findUserCourses(userId);

      assertEquals(new BigDecimal("33.3"), findCourse(result, courseId).courseProgress());
    }

    @Test
    void コースが0件のとき空リストが返ること() {
      jdbcTemplate.execute("DELETE FROM courses");

      UUID userId =
          testData.createUser("ul-put-mismatch@example.com", "p", "四郎", "ulpm", "GENERAL");
      List<UserCourseDto> result = userCourseApplicationService.findUserCourses(userId);

      assertNotNull(result);
      assertTrue(result.isEmpty());
    }

    @Test
    void 存在しないuserIdを指定した場合ResourceNotFoundExceptionが投げられること() {
      assertThrows(
          ResourceNotFoundException.class,
          () -> userCourseApplicationService.findUserCourses(UUID.randomUUID()));
    }
  }

  private UUID createLesson(UUID lessonGroupId, UUID courseId, String lessonOrder) {
    return testData.createLesson(
        lessonGroupId,
        courseId,
        new BigDecimal(lessonOrder),
        "ULレッスン" + lessonOrder,
        "説明",
        "https://example.com/video.mp4");
  }

  private UserCourseDto findCourse(List<UserCourseDto> result, UUID courseId) {
    return result.stream().filter(dto -> dto.id().equals(courseId)).findFirst().orElseThrow();
  }
}
