package com.everrefine.elms.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.everrefine.elms.application.command.LessonCreateCommand;
import com.everrefine.elms.application.command.LessonImportCommand;
import com.everrefine.elms.application.command.LessonOrderUpdateCommand;
import com.everrefine.elms.application.command.LessonUpdateCommand;
import com.everrefine.elms.application.dto.CourseLessonsDto;
import com.everrefine.elms.application.dto.LessonDto;
import com.everrefine.elms.application.dto.LessonImportResponseDto;
import com.everrefine.elms.application.dto.LessonTagSearchCourseDto;
import com.everrefine.elms.application.dto.LessonTagSearchLessonDto;
import com.everrefine.elms.application.dto.LessonTagSearchLessonGroupDto;
import com.everrefine.elms.application.dto.LessonTagSearchResultDto;
import com.everrefine.elms.application.dto.TagDto;
import com.everrefine.elms.application.exception.BadRequestException;
import com.everrefine.elms.application.exception.ResourceNotFoundException;
import com.everrefine.elms.domain.exception.InvalidValueException;
import com.everrefine.elms.domain.model.lesson.Lesson;
import com.everrefine.elms.domain.repository.LessonRepository;
import com.everrefine.elms.presentation.request.LessonCreateRequest;
import com.everrefine.elms.presentation.request.LessonOrderUpdateRequest;
import com.everrefine.elms.presentation.request.LessonTagRequest;
import com.everrefine.elms.presentation.request.LessonTagSearchRequest;
import com.everrefine.elms.presentation.request.LessonUpdateRequest;
import com.everrefine.elms.testsupport.TestDataFactory;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * {@link LessonApplicationServiceImpl}の統合テストクラス。
 *
 * <p>このテストクラスでは、実際のPostgreSQLデータベースを使用した統合テストを実施します。 Testcontainersを利用してテスト用のDockerコンテナを起動し、Spring
 * Bootのテスト機能と組み合わせることで、 実運用環境に近い状態でのテストを保証します。
 *
 * <p>主な検証項目：
 *
 * <ul>
 *   <li>実運用に近いシナリオの網羅
 *   <li>エッジケースと例外処理の検証
 *   <li>実際のデータベース操作の検証
 *   <li>ビジネスロジックの正確性
 *   <li>トランザクション境界の考慮
 * </ul>
 */
@ActiveProfiles("dev")
@SpringBootTest(webEnvironment = WebEnvironment.NONE) // WebまわりのConfigurationはBean生成を無効にして高速化する。
@Testcontainers // DBはDockerコンテナを使用する。
@Transactional // 各テストメソッド終了時にテストデータをロールバックする。
public class LessonApplicationServiceImplTest {

  /** テストで使うDBを用意する。 */
  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

  /** テスト対象のサービスクラス。 */
  @Autowired private TestDataFactory testData;

  @Autowired private LessonApplicationServiceImpl lessonApplicationService;

  /** レッスンリポジトリ。 */
  @Autowired private LessonRepository lessonRepository;

  /** データ検証で使用するためのJdbcTemplate。 */
  @Autowired private JdbcTemplate jdbcTemplate;

  @Nested
  class レッスン取得 {
    @Test
    void レッスンをIDで取得できること() {
      // Arrange - テストデータを準備（IDは自動生成）
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");
      UUID lessonId =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1"),
              "テストレッスン",
              "テスト説明",
              "https://example.com/video.mp4");

      // Act
      LessonDto result = lessonApplicationService.findLessonById(courseId, lessonGroupId, lessonId);

      // Assert
      assertNotNull(result);
      assertEquals(lessonId, result.id());
      assertEquals(lessonGroupId, result.lessonGroupId());
      assertEquals(courseId, result.courseId());
      assertEquals(new BigDecimal("1.0000"), result.lessonOrder());
      assertEquals("テストレッスン", result.title());
      assertEquals("テスト説明", result.content());
      assertEquals("https://example.com/video.mp4", result.videoUrl());
      assertNotNull(result.createdAt());
      assertNotNull(result.updatedAt());
      assertTrue(result.tags().isEmpty());
    }

    @Test
    void レッスンをIDで取得すると紐づくタグも返ること() {
      UUID courseId = testData.createCourse(new BigDecimal("1"), "タグ取得コース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "タグ取得グループ");
      UUID lessonId =
          testData.createLesson(
              lessonGroupId, courseId, new BigDecimal("1"), "タグ付きレッスン", "説明", null);
      UUID springTagId = testData.createTag("Spring");
      UUID javaTagId = testData.createTag("Java");
      testData.createLessonTag(lessonId, springTagId);
      testData.createLessonTag(lessonId, javaTagId);

      LessonDto result = lessonApplicationService.findLessonById(courseId, lessonGroupId, lessonId);

      assertEquals(
          List.of("Java", "Spring"), result.tags().stream().map(tag -> tag.name()).toList());
      assertEquals(
          List.of(javaTagId, springTagId), result.tags().stream().map(tag -> tag.id()).toList());
    }

    @Test
    void 存在しないレッスンIDでResourceNotFoundExceptionが投げられること() {
      // Act & Assert
      UUID nonExistentId = UUID.randomUUID();
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () ->
                  lessonApplicationService.findLessonById(
                      UUID.randomUUID(), UUID.randomUUID(), nonExistentId));
      assertEquals("Lesson が見つかりませんでした。id = " + nonExistentId, exception.getMessage());
    }

    @Test
    void パスのコースまたはグループがレッスンと一致しない場合ResourceNotFoundExceptionが投げられること() {
      UUID courseId = testData.createCourse(new BigDecimal("1"), "整合コース", "説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "整合グループ");
      UUID lessonId =
          testData.createLesson(lessonGroupId, courseId, new BigDecimal("1"), "L1", "d", null);
      UUID otherCourseId = testData.createCourse(new BigDecimal("2"), "別コース", "説明");

      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () ->
                  lessonApplicationService.findLessonById(otherCourseId, lessonGroupId, lessonId));
      assertEquals("Lesson が見つかりませんでした。id = " + lessonId, exception.getMessage());
    }
  }

  @Nested
  class コース別レッスン一覧取得 {
    @Test
    void コース別レッスン一覧を取得できること() {
      // Arrange - テストデータを準備（IDは自動生成）
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");

      // Act
      CourseLessonsDto result = lessonApplicationService.findLessonsGroupedByLessonGroup(courseId);

      // Assert
      assertNotNull(result);
      assertNotNull(result.lessonGroups());
    }

    @Test
    void コース別レッスン一覧で各レッスンのタグも返ること() {
      UUID courseId = testData.createCourse(new BigDecimal("1"), "一覧タグ取得コース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "一覧タグ取得グループ");
      UUID taggedLessonId =
          testData.createLesson(
              lessonGroupId, courseId, new BigDecimal("1"), "一覧タグ付きレッスン", "説明", null);
      UUID taglessLessonId =
          testData.createLesson(
              lessonGroupId, courseId, new BigDecimal("2"), "一覧タグなしレッスン", "説明", null);
      UUID javaTagId = testData.createTag("Java");
      testData.createLessonTag(taggedLessonId, javaTagId);

      CourseLessonsDto result = lessonApplicationService.findLessonsGroupedByLessonGroup(courseId);

      LessonDto taggedLesson =
          result.lessonGroups().getFirst().lessons().stream()
              .filter(lesson -> lesson.id().equals(taggedLessonId))
              .findFirst()
              .orElseThrow();
      LessonDto taglessLesson =
          result.lessonGroups().getFirst().lessons().stream()
              .filter(lesson -> lesson.id().equals(taglessLessonId))
              .findFirst()
              .orElseThrow();

      assertEquals(1, taggedLesson.tags().size());
      assertEquals(javaTagId, taggedLesson.tags().getFirst().id());
      assertEquals("Java", taggedLesson.tags().getFirst().name());
      assertTrue(taglessLesson.tags().isEmpty());
    }
  }

  @Nested
  class レッスン作成 {
    @Test
    void レッスンを作成できること() {
      // Arrange - 関連データを準備（IDは自動生成）
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");

      LessonCreateRequest request =
          new LessonCreateRequest("新規レッスン", "新規説明", "https://example.com/new-video.mp4");
      LessonCreateCommand command = request.toCommand(courseId, lessonGroupId);

      // Act
      LessonDto result = lessonApplicationService.createLesson(command);

      // Assert
      assertNotNull(result);
      assertEquals("新規レッスン", result.title());
      assertEquals("新規説明", result.content());
      assertEquals("https://example.com/new-video.mp4", result.videoUrl());

      // DBにデータが保存されていることを確認
      Integer count =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM lessons WHERE title = ?", Integer.class, "新規レッスン");
      assertEquals(1, count);
      String savedContent =
          jdbcTemplate.queryForObject(
              "SELECT content FROM lessons WHERE title = ?", String.class, "新規レッスン");
      assertEquals("新規説明", savedContent);
    }

    @Test
    void null許容フィールドを指定せずにレッスンを作成できること() {
      // Arrange - 関連データを準備（IDは自動生成）
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");

      LessonCreateRequest request = new LessonCreateRequest("最小構成レッスン", null, null);
      LessonCreateCommand command = request.toCommand(courseId, lessonGroupId);

      // Act
      LessonDto result = lessonApplicationService.createLesson(command);

      // Assert
      assertNotNull(result);
      assertEquals("最小構成レッスン", result.title());
      assertNull(result.content());
      assertNull(result.videoUrl());
    }

    @Test
    void 複数レッスンを一括作成できること() {
      // Arrange
      UUID courseId = testData.createCourse(new BigDecimal("1"), "一括登録コース", "コース説明");
      UUID lessonGroupId1 =
          testData.createLessonGroup(courseId, new BigDecimal("1024"), "一括登録グループ1");
      UUID lessonGroupId2 =
          testData.createLessonGroup(courseId, new BigDecimal("2048"), "一括登録グループ2");

      List<Lesson> lessons =
          List.of(
              Lesson.create(
                  lessonGroupId1,
                  courseId,
                  new BigDecimal("1024"),
                  "一括登録レッスン1",
                  "説明1",
                  "https://example.com/batch1.mp4"),
              Lesson.create(
                  lessonGroupId1, courseId, new BigDecimal("2048"), "一括登録レッスン2", null, null),
              Lesson.create(
                  lessonGroupId2, courseId, new BigDecimal("1024"), "一括登録レッスン3", "説明3", null));

      // Act
      lessonRepository.createLessons(lessons);
      lessonRepository.createLessons(List.of());

      // Assert
      List<Map<String, Object>> actualLessons =
          jdbcTemplate.queryForList(
              """
              SELECT l.lesson_group_id, l.course_id, l.lesson_order, l.title, l.content, l.video_url
              FROM lessons l
              JOIN lesson_groups g ON g.id = l.lesson_group_id
              WHERE l.course_id = ?
              ORDER BY g.lesson_group_order ASC, l.lesson_order ASC
              """,
              courseId);

      assertEquals(3, actualLessons.size());
      assertEquals(lessonGroupId1, actualLessons.get(0).get("lesson_group_id"));
      assertEquals(courseId, actualLessons.get(0).get("course_id"));
      assertEquals(new BigDecimal("1024.0000"), actualLessons.get(0).get("lesson_order"));
      assertEquals("一括登録レッスン1", actualLessons.get(0).get("title"));
      assertEquals("説明1", actualLessons.get(0).get("content"));
      assertEquals("https://example.com/batch1.mp4", actualLessons.get(0).get("video_url"));

      assertEquals(lessonGroupId1, actualLessons.get(1).get("lesson_group_id"));
      assertEquals(new BigDecimal("2048.0000"), actualLessons.get(1).get("lesson_order"));
      assertEquals("一括登録レッスン2", actualLessons.get(1).get("title"));
      assertNull(actualLessons.get(1).get("content"));
      assertNull(actualLessons.get(1).get("video_url"));

      assertEquals(lessonGroupId2, actualLessons.get(2).get("lesson_group_id"));
      assertEquals(new BigDecimal("1024.0000"), actualLessons.get(2).get("lesson_order"));
      assertEquals("一括登録レッスン3", actualLessons.get(2).get("title"));
      assertEquals("説明3", actualLessons.get(2).get("content"));
      assertNull(actualLessons.get(2).get("video_url"));
    }
  }

  @Nested
  class レッスンCSV取込 {
    @Test
    void 既存レッスン構成を置き換えられること() throws Exception {
      // Arrange - 既存のレッスン構成を準備
      UUID courseId = testData.createCourse(new BigDecimal("1"), "CSV取込コース", "コース説明");
      UUID oldLessonGroupId =
          testData.createLessonGroup(courseId, new BigDecimal("1024"), "削除対象グループ");
      testData.createLesson(
          oldLessonGroupId,
          courseId,
          new BigDecimal("1024"),
          "削除対象レッスン",
          "削除対象説明",
          "https://example.com/old.mp4");

      String csv =
          String.join(
              "\n",
              "レッスングループタイトル,レッスンタイトル,レッスン説明,レッスンの動画URL",
              "Basic,Lesson 1,説明1,https://example.com/1.mp4",
              "Advanced,Lesson 2,,",
              "Basic,Lesson 3,,https://example.com/3.mp4");
      MockMultipartFile file =
          new MockMultipartFile(
              "file", "lessons.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

      // Act
      LessonImportResponseDto result =
          lessonApplicationService.importLessonsCsv(
              LessonImportCommand.from(courseId, file.getOriginalFilename(), file.getBytes()));

      // Assert - 取込件数
      assertEquals(2, result.importedLessonGroupCount());
      assertEquals(3, result.importedLessonCount());

      // Assert - 既存構成は削除されている
      Integer oldLessonCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM lessons WHERE title = ?", Integer.class, "削除対象レッスン");
      assertEquals(0, oldLessonCount);
      Integer oldLessonGroupCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM lesson_groups WHERE title = ?", Integer.class, "削除対象グループ");
      assertEquals(0, oldLessonGroupCount);

      // Assert - レッスングループは初出順で1024間隔になる
      List<Map<String, Object>> lessonGroups =
          jdbcTemplate.queryForList(
              """
              SELECT id, title, lesson_group_order
              FROM lesson_groups
              WHERE course_id = ?
              ORDER BY lesson_group_order ASC
              """,
              courseId);
      assertEquals(2, lessonGroups.size());
      assertEquals("Basic", lessonGroups.get(0).get("title"));
      assertEquals(new BigDecimal("1024.0000"), lessonGroups.get(0).get("lesson_group_order"));
      assertEquals("Advanced", lessonGroups.get(1).get("title"));
      assertEquals(new BigDecimal("2048.0000"), lessonGroups.get(1).get("lesson_group_order"));

      // Assert - 同じグループタイトルの行は同一グループにまとまり、レッスン順はグループ内のCSV順になる
      UUID basicLessonGroupId =
          jdbcTemplate.queryForObject(
              "SELECT id FROM lesson_groups WHERE course_id = ? AND title = ?",
              UUID.class,
              courseId,
              "Basic");
      List<Map<String, Object>> basicLessons =
          jdbcTemplate.queryForList(
              """
              SELECT title, lesson_order, content, video_url
              FROM lessons
              WHERE course_id = ? AND lesson_group_id = ?
              ORDER BY lesson_order ASC
              """,
              courseId,
              basicLessonGroupId);
      assertEquals(2, basicLessons.size());
      assertEquals("Lesson 1", basicLessons.get(0).get("title"));
      assertEquals(new BigDecimal("1024.0000"), basicLessons.get(0).get("lesson_order"));
      assertEquals("説明1", basicLessons.get(0).get("content"));
      assertEquals("https://example.com/1.mp4", basicLessons.get(0).get("video_url"));
      assertEquals("Lesson 3", basicLessons.get(1).get("title"));
      assertEquals(new BigDecimal("2048.0000"), basicLessons.get(1).get("lesson_order"));
      assertNull(basicLessons.get(1).get("content"));
      assertEquals("https://example.com/3.mp4", basicLessons.get(1).get("video_url"));

      UUID advancedLessonGroupId =
          jdbcTemplate.queryForObject(
              "SELECT id FROM lesson_groups WHERE course_id = ? AND title = ?",
              UUID.class,
              courseId,
              "Advanced");
      Map<String, Object> advancedLesson =
          jdbcTemplate.queryForMap(
              """
              SELECT title, lesson_order, content, video_url
              FROM lessons
              WHERE course_id = ? AND lesson_group_id = ?
              """,
              courseId,
              advancedLessonGroupId);
      assertEquals("Lesson 2", advancedLesson.get("title"));
      assertEquals(new BigDecimal("1024.0000"), advancedLesson.get("lesson_order"));
      assertNull(advancedLesson.get("content"));
      assertNull(advancedLesson.get("video_url"));
    }

    @Test
    void 存在しないコースを指定した場合ResourceNotFoundExceptionが投げられ既存構成が削除されないこと() throws Exception {
      // Arrange
      UUID courseId = testData.createCourse(new BigDecimal("1"), "CSV取込失敗コース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1024"), "保持対象グループ");
      testData.createLesson(
          lessonGroupId,
          courseId,
          new BigDecimal("1024"),
          "保持対象レッスン",
          "保持対象説明",
          "https://example.com/keep.mp4");

      String csv =
          String.join(
              "\n",
              "レッスングループタイトル,レッスンタイトル,レッスン説明,レッスンの動画URL",
              "Basic,Lesson 1,説明1,https://example.com/1.mp4");
      MockMultipartFile file =
          new MockMultipartFile(
              "file", "lessons.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
      LessonImportCommand command =
          LessonImportCommand.from(UUID.randomUUID(), file.getOriginalFilename(), file.getBytes());

      // Act & Assert
      assertThrows(
          ResourceNotFoundException.class,
          () -> lessonApplicationService.importLessonsCsv(command));

      Integer lessonGroupCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM lesson_groups WHERE course_id = ?", Integer.class, courseId);
      assertEquals(1, lessonGroupCount);
      Integer lessonCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM lessons WHERE course_id = ?", Integer.class, courseId);
      assertEquals(1, lessonCount);
    }
  }

  @Nested
  class レッスン更新 {
    @Test
    void レッスンを更新できること() {
      // Arrange - 既存レッスンを準備（IDは自動生成）
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");
      UUID lessonId =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1"),
              "元のタイトル",
              "元の説明",
              "https://example.com/old-video.mp4");

      LessonUpdateRequest request =
          new LessonUpdateRequest(
              "更新後タイトル", "更新後説明", "https://example.com/updated-video.mp4", List.of());
      LessonUpdateCommand command = request.toCommand(lessonId);

      // Act
      LessonDto result = lessonApplicationService.updateLesson(command);

      // Assert
      assertNotNull(result);
      assertEquals(lessonId, result.id());
      assertEquals("更新後タイトル", result.title());
      assertEquals("更新後説明", result.content());
      assertEquals("https://example.com/updated-video.mp4", result.videoUrl());

      // DBが更新されていることを確認
      String updatedTitle =
          jdbcTemplate.queryForObject(
              "SELECT title FROM lessons WHERE id = ?", String.class, lessonId);
      assertEquals("更新後タイトル", updatedTitle);
      String updatedContent =
          jdbcTemplate.queryForObject(
              "SELECT content FROM lessons WHERE id = ?", String.class, lessonId);
      assertEquals("更新後説明", updatedContent);
    }

    @Test
    void null許容フィールドをnullで更新できること() {
      // Arrange - 既存レッスンを準備（IDは自動生成）
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");
      UUID lessonId =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1"),
              "元のタイトル",
              "元の説明",
              "https://example.com/old-video.mp4");

      // nullを渡すと元の値が保持される仕様
      LessonUpdateRequest request = new LessonUpdateRequest("タイトルのみ更新", null, null, List.of());
      LessonUpdateCommand command = request.toCommand(lessonId);

      // Act
      LessonDto result = lessonApplicationService.updateLesson(command);

      // Assert - nullを渡した場合は元の値が保持される
      assertNotNull(result);
      assertEquals("タイトルのみ更新", result.title());
      assertEquals("元の説明", result.content()); // 元の値が保持される
      assertEquals("https://example.com/old-video.mp4", result.videoUrl()); // 元の値が保持される
    }

    @Test
    void 存在しないレッスンを更新するとResourceNotFoundExceptionが投げられること() {
      // Arrange
      LessonUpdateRequest request =
          new LessonUpdateRequest("存在しないレッスン", "説明", "https://example.com/video.mp4", List.of());
      UUID nonExistentId = UUID.randomUUID();
      LessonUpdateCommand command = request.toCommand(nonExistentId);

      // Act & Assert
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () -> lessonApplicationService.updateLesson(command));
      assertEquals("Lesson が見つかりませんでした。id = " + nonExistentId, exception.getMessage());
    }

    @Test
    void 複数タグを指定してレッスンを更新できること() {
      // Arrange - 既存レッスンを準備（IDは自動生成）
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");
      UUID lessonId =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1"),
              "元のタイトル",
              "元の説明",
              "https://example.com/old-video.mp4");

      LessonUpdateRequest request =
          new LessonUpdateRequest(
              "更新後タイトル",
              "更新後説明",
              "https://example.com/updated-video.mp4",
              List.of(new LessonTagRequest("Java"), new LessonTagRequest("Spring")));
      LessonUpdateCommand command = request.toCommand(lessonId);

      // Act
      LessonDto result = lessonApplicationService.updateLesson(command);

      // Assert
      assertNotNull(result);
      assertEquals(2, result.tags().size());
      assertEquals(
          List.of("Java", "Spring"),
          result.tags().stream().map(tag -> tag.name()).sorted().toList());

      List<String> tagNames =
          jdbcTemplate.queryForList(
              """
                  SELECT t.name
                  FROM tags t
                  INNER JOIN lesson_tags lt
                    ON t.id = lt.tag_id
                  WHERE lt.lesson_id = ?
                  ORDER BY t.name
                  """,
              String.class,
              lessonId);
      assertEquals(List.of("Java", "Spring"), tagNames);
    }

    @Test
    void 前後に空白があるタグ名はtrimして同じタグとして扱うこと() {
      // Arrange - 既存レッスンと既存タグを準備
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");
      UUID lessonId =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1"),
              "元のタイトル",
              "元の説明",
              "https://example.com/old-video.mp4");
      UUID tagId = UUID.randomUUID();
      jdbcTemplate.update("INSERT INTO tags (id, name) VALUES (?, ?)", tagId, "Java");

      LessonUpdateRequest request =
          new LessonUpdateRequest(
              "更新後タイトル", "更新後説明", null, List.of(new LessonTagRequest(" Java ")));
      LessonUpdateCommand command = request.toCommand(lessonId);

      // Act
      LessonDto result = lessonApplicationService.updateLesson(command);

      // Assert
      assertEquals(
          List.of("Java"), result.tags().stream().map(tag -> tag.name()).sorted().toList());

      Integer tagCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM tags WHERE name = ?", Integer.class, "Java");
      assertEquals(1, tagCount);

      List<String> tagNames =
          jdbcTemplate.queryForList(
              """
                  SELECT t.name
                  FROM tags t
                  INNER JOIN lesson_tags lt
                    ON t.id = lt.tag_id
                  WHERE lt.lesson_id = ?
                  ORDER BY t.name
                  """,
              String.class,
              lessonId);
      assertEquals(List.of("Java"), tagNames);

      Integer lessonTagCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM lesson_tags WHERE lesson_id = ?", Integer.class, lessonId);
      assertEquals(1, lessonTagCount);
    }

    @Test
    void trim後のタグ名が重複する場合はBadRequestExceptionが投げられること() {
      // Arrange - 既存レッスンを準備
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");
      UUID lessonId =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1"),
              "元のタイトル",
              "元の説明",
              "https://example.com/old-video.mp4");

      LessonUpdateRequest request =
          new LessonUpdateRequest(
              "更新後タイトル",
              "更新後説明",
              null,
              List.of(new LessonTagRequest("Java"), new LessonTagRequest(" Java ")));
      LessonUpdateCommand command = request.toCommand(lessonId);

      // Act & Assert
      BadRequestException exception =
          assertThrows(
              BadRequestException.class, () -> lessonApplicationService.updateLesson(command));
      assertEquals("タグ名は重複しないように入力してください", exception.getMessage());
    }

    @Test
    void 既存タグ紐付けをリクエストされたタグで洗い替えできること() {
      // Arrange - 既存タグが紐づいたレッスンを準備
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");
      UUID lessonId =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1"),
              "元のタイトル",
              "元の説明",
              "https://example.com/old-video.mp4");
      UUID oldTagId = UUID.randomUUID();
      jdbcTemplate.update("INSERT INTO tags (id, name) VALUES (?, ?)", oldTagId, "古いタグ");
      jdbcTemplate.update(
          "INSERT INTO lesson_tags (lesson_id, tag_id) VALUES (?, ?)", lessonId, oldTagId);

      LessonUpdateRequest request =
          new LessonUpdateRequest("更新後タイトル", "更新後説明", null, List.of(new LessonTagRequest("Java")));
      LessonUpdateCommand command = request.toCommand(lessonId);

      // Act
      LessonDto result = lessonApplicationService.updateLesson(command);

      // Assert
      assertEquals(
          List.of("Java"), result.tags().stream().map(tag -> tag.name()).sorted().toList());

      List<String> tagNames =
          jdbcTemplate.queryForList(
              """
                  SELECT t.name
                  FROM tags t
                  INNER JOIN lesson_tags lt
                    ON t.id = lt.tag_id
                  WHERE lt.lesson_id = ?
                  ORDER BY t.name
                  """,
              String.class,
              lessonId);
      assertEquals(List.of("Java"), tagNames);
    }

    @Test
    void 空配列で更新すると既存タグ紐付けを全削除できること() {
      // Arrange - 既存タグが紐づいたレッスンを準備
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");
      UUID lessonId =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1"),
              "元のタイトル",
              "元の説明",
              "https://example.com/old-video.mp4");
      UUID tagId = UUID.randomUUID();
      jdbcTemplate.update("INSERT INTO tags (id, name) VALUES (?, ?)", tagId, "Java");
      jdbcTemplate.update(
          "INSERT INTO lesson_tags (lesson_id, tag_id) VALUES (?, ?)", lessonId, tagId);

      LessonUpdateRequest request = new LessonUpdateRequest("更新後タイトル", "更新後説明", null, List.of());
      LessonUpdateCommand command = request.toCommand(lessonId);

      // Act
      LessonDto result = lessonApplicationService.updateLesson(command);

      // Assert
      assertTrue(result.tags().isEmpty());

      Integer lessonTagCount =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM lesson_tags WHERE lesson_id = ?", Integer.class, lessonId);
      assertEquals(0, lessonTagCount);
    }
  }

  @Nested
  class レッスン削除 {
    @Test
    void 存在するレッスンを削除できること() {
      // Arrange - 削除対象のレッスンを準備（IDは自動生成）
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");
      UUID lessonId =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1"),
              "削除対象レッスン",
              "説明",
              "https://example.com/video.mp4");

      // Act
      lessonApplicationService.deleteLessonById(lessonId);

      // Assert - レッスンが削除されていることを確認
      Integer count =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM lessons WHERE id = ?", Integer.class, lessonId);
      assertEquals(0, count);
    }

    @Test
    void 存在しないレッスンを削除してもエラーにならないこと() {
      // Act - 存在しないIDで削除を実行
      lessonApplicationService.deleteLessonById(UUID.randomUUID());

      // Assert - 例外が発生しないことを確認（このテストが成功すればOK）
    }

    @Test
    void 複数回の削除操作が安全に実行できること() {
      // Arrange - 削除対象のレッスンを準備（IDは自動生成）
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");
      UUID lessonId =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1"),
              "複数回削除テスト",
              "説明",
              "https://example.com/video.mp4");

      // Act
      lessonApplicationService.deleteLessonById(lessonId);
      lessonApplicationService.deleteLessonById(lessonId); // 2回目

      // Assert - 2回目の削除もエラーにならないことを確認
      Integer count =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM lessons WHERE id = ?", Integer.class, lessonId);
      assertEquals(0, count);
    }
  }

  @Nested
  class レッスン並び替え {
    @Test
    void 指定した2つのレッスンの間に移動できること() {
      // Arrange - テストデータを準備
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");

      // 3つのレッスンを作成（order: 1000, 2000, 3000）
      UUID lesson1Id =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1000"),
              "レッスン1",
              "説明1",
              "https://example.com/video1.mp4");

      UUID lesson2Id =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("2000"),
              "レッスン2",
              "説明2",
              "https://example.com/video2.mp4");

      UUID lesson3Id =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("3000"),
              "レッスン3",
              "説明3",
              "https://example.com/video3.mp4");

      // Act - レッスン3をレッスン1と2の間に移動
      LessonOrderUpdateRequest request = new LessonOrderUpdateRequest(lesson1Id, lesson2Id);
      LessonOrderUpdateCommand command = request.toCommand(lesson3Id);
      LessonDto result = lessonApplicationService.updateLessonOrder(command);

      // Assert - 新しい順序は (1000 + 2000) / 2 = 1500
      assertNotNull(result);
      assertEquals(lesson3Id, result.id());
      assertEquals(new BigDecimal("1500.0000"), result.lessonOrder());

      // DBが更新されていることを確認
      BigDecimal updatedOrder =
          jdbcTemplate.queryForObject(
              "SELECT lesson_order FROM lessons WHERE id = ?", BigDecimal.class, lesson3Id);
      assertEquals(new BigDecimal("1500.0000"), updatedOrder);
    }

    @Test
    void 先頭に移動できること() {
      // Arrange - テストデータを準備
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");

      // 2つのレッスンを作成
      UUID lesson1Id =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1000"),
              "レッスン1",
              "説明1",
              "https://example.com/video1.mp4");
      UUID lesson2Id =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("2000"),
              "レッスン2",
              "説明2",
              "https://example.com/video2.mp4");

      // Act - レッスン2を先頭に移動（precedingLessonIdをnullに）
      LessonOrderUpdateRequest request = new LessonOrderUpdateRequest(null, lesson1Id);
      LessonOrderUpdateCommand command = request.toCommand(lesson2Id);
      LessonDto result = lessonApplicationService.updateLessonOrder(command);

      // Assert - 新しい順序は 1000 / 2 = 500
      assertNotNull(result);
      assertEquals(lesson2Id, result.id());
      assertEquals(new BigDecimal("500.0000"), result.lessonOrder());

      // DBが更新されていることを確認
      BigDecimal updatedOrder =
          jdbcTemplate.queryForObject(
              "SELECT lesson_order FROM lessons WHERE id = ?", BigDecimal.class, lesson2Id);
      assertEquals(new BigDecimal("500.0000"), updatedOrder);
    }

    @Test
    void 末尾に移動できること() {
      // Arrange - テストデータを準備
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");

      // 2つのレッスンを作成
      UUID lesson1Id =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1000"),
              "レッスン1",
              "説明1",
              "https://example.com/video1.mp4");
      UUID lesson2Id =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("2000"),
              "レッスン2",
              "説明2",
              "https://example.com/video2.mp4");

      // Act - レッスン1を末尾に移動（followingLessonIdをnullに）
      LessonOrderUpdateRequest request = new LessonOrderUpdateRequest(lesson2Id, null);
      LessonOrderUpdateCommand command = request.toCommand(lesson1Id);
      LessonDto result = lessonApplicationService.updateLessonOrder(command);

      // Assert - 新しい順序は 2000 + 1024 = 3024
      assertNotNull(result);
      assertEquals(lesson1Id, result.id());
      assertEquals(new BigDecimal("3024.0000"), result.lessonOrder());

      // DBが更新されていることを確認
      BigDecimal updatedOrder =
          jdbcTemplate.queryForObject(
              "SELECT lesson_order FROM lessons WHERE id = ?", BigDecimal.class, lesson1Id);
      assertEquals(new BigDecimal("3024.0000"), updatedOrder);
    }

    @Test
    void 前後のレッスンIDがどちらもnullの場合BadRequestExceptionが投げられること() {
      UUID lessonId = UUID.randomUUID();
      LessonOrderUpdateRequest request = new LessonOrderUpdateRequest(null, null);

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> lessonApplicationService.updateLessonOrder(request.toCommand(lessonId)));

      assertEquals("前後のレッスンIDをどちらか一方は指定してください", exception.getMessage());
    }

    @Test
    void 存在しないレッスンIDで並び替えするとResourceNotFoundExceptionが投げられること() {
      // Arrange - 存在するレッスンを1つ準備
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");
      UUID lesson1Id =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1000"),
              "レッスン1",
              "説明1",
              "https://example.com/video1.mp4");

      // Act & Assert - 存在しないレッスンIDで並び替え
      LessonOrderUpdateRequest request = new LessonOrderUpdateRequest(lesson1Id, null);
      UUID nonExistentId = UUID.randomUUID();
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () -> lessonApplicationService.updateLessonOrder(request.toCommand(nonExistentId)));
      assertEquals("Lesson が見つかりませんでした。id = " + nonExistentId, exception.getMessage());
    }

    @Test
    void 存在しないprecedingLessonIdでResourceNotFoundExceptionが投げられること() {
      // Arrange - テストデータを準備
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");
      UUID lesson1Id =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1000"),
              "レッスン1",
              "説明1",
              "https://example.com/video1.mp4");

      // Act & Assert - 存在しないprecedingLessonId
      UUID nonExistentId = UUID.randomUUID();
      LessonOrderUpdateRequest request = new LessonOrderUpdateRequest(nonExistentId, null);
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () -> lessonApplicationService.updateLessonOrder(request.toCommand(lesson1Id)));
      assertEquals("Lesson が見つかりませんでした。id = " + nonExistentId, exception.getMessage());
    }

    @Test
    void 存在しないfollowingLessonIdでResourceNotFoundExceptionが投げられること() {
      // Arrange - テストデータを準備
      UUID courseId = testData.createCourse(new BigDecimal("1"), "テストコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "テストグループ");
      UUID lesson1Id =
          testData.createLesson(
              lessonGroupId,
              courseId,
              new BigDecimal("1000"),
              "レッスン1",
              "説明1",
              "https://example.com/video1.mp4");

      // Act & Assert - 存在しないfollowingLessonId
      UUID nonExistentId = UUID.randomUUID();
      LessonOrderUpdateRequest request = new LessonOrderUpdateRequest(null, nonExistentId);
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () -> lessonApplicationService.updateLessonOrder(request.toCommand(lesson1Id)));
      assertEquals("Lesson が見つかりませんでした。id = " + nonExistentId, exception.getMessage());
    }
  }

  @Nested
  class タグに紐づくレッスン検索 {

    @Test
    void 指定したタグに紐づくレッスンだけが返ること() {
      // Arrange
      UUID courseId = testData.createCourse(new BigDecimal("1"), "タグ検索コース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "タグ検索グループ");
      UUID targetLessonId =
          testData.createLesson(
              lessonGroupId, courseId, new BigDecimal("1"), "タグ検索対象レッスン", "説明", null);
      testData.createLesson(
          lessonGroupId, courseId, new BigDecimal("2"), "タグ検索タグなしレッスン", "説明", null);
      UUID otherTaggedLessonId =
          testData.createLesson(
              lessonGroupId, courseId, new BigDecimal("3"), "タグ検索別タグレッスン", "説明", null);
      testData.createLessonTag(targetLessonId, testData.createTag("タグ検索Java"));
      testData.createLessonTag(otherTaggedLessonId, testData.createTag("タグ検索Python"));

      // Act
      LessonTagSearchResultDto result = searchByTag("タグ検索Java", 1, 10);

      // Assert
      assertEquals("タグ検索Java", result.tag());
      assertEquals(1, result.totalSize());
      assertEquals(1, result.courses().size());
      assertEquals(1, result.courses().getFirst().lessonGroups().size());
      assertEquals(
          List.of(targetLessonId),
          lessonIdsOf(result.courses().getFirst().lessonGroups().getFirst()));
    }

    @Test
    void コースとレッスングループとレッスンが表示順の昇順で返ること() {
      // Arrange - 表示順とは逆の順番で登録し、並び替えが効いていることを確認する
      UUID secondCourseId = testData.createCourse(new BigDecimal("2"), "並び順コースB", "コース説明");
      UUID firstCourseId = testData.createCourse(new BigDecimal("1"), "並び順コースA", "コース説明");
      UUID secondGroupId =
          testData.createLessonGroup(firstCourseId, new BigDecimal("2"), "並び順グループA2");
      UUID firstGroupId =
          testData.createLessonGroup(firstCourseId, new BigDecimal("1"), "並び順グループA1");
      UUID otherCourseGroupId =
          testData.createLessonGroup(secondCourseId, new BigDecimal("1"), "並び順グループB1");
      UUID secondLessonId =
          testData.createLesson(
              firstGroupId, firstCourseId, new BigDecimal("2"), "並び順レッスンA1-2", "説明", null);
      UUID firstLessonId =
          testData.createLesson(
              firstGroupId, firstCourseId, new BigDecimal("1"), "並び順レッスンA1-1", "説明", null);
      UUID thirdLessonId =
          testData.createLesson(
              secondGroupId, firstCourseId, new BigDecimal("1"), "並び順レッスンA2-1", "説明", null);
      UUID fourthLessonId =
          testData.createLesson(
              otherCourseGroupId, secondCourseId, new BigDecimal("1"), "並び順レッスンB1-1", "説明", null);
      UUID tagId = testData.createTag("並び順タグ");
      testData.createLessonTag(firstLessonId, tagId);
      testData.createLessonTag(secondLessonId, tagId);
      testData.createLessonTag(thirdLessonId, tagId);
      testData.createLessonTag(fourthLessonId, tagId);

      // Act
      LessonTagSearchResultDto result = searchByTag("並び順タグ", 1, 10);

      // Assert
      assertEquals(
          List.of(firstCourseId, secondCourseId),
          result.courses().stream().map(LessonTagSearchCourseDto::courseId).toList());

      LessonTagSearchCourseDto firstCourse = result.courses().getFirst();
      assertEquals(
          List.of(firstGroupId, secondGroupId),
          firstCourse.lessonGroups().stream()
              .map(LessonTagSearchLessonGroupDto::lessonGroupId)
              .toList());
      assertEquals(
          List.of(firstLessonId, secondLessonId),
          lessonIdsOf(firstCourse.lessonGroups().getFirst()));
    }

    @Test
    void 一致するレッスンを持たないコースとレッスングループが結果に含まれないこと() {
      // Arrange
      UUID targetCourseId = testData.createCourse(new BigDecimal("1"), "絞り込みコースA", "コース説明");
      UUID otherCourseId = testData.createCourse(new BigDecimal("2"), "絞り込みコースB", "コース説明");
      UUID targetGroupId =
          testData.createLessonGroup(targetCourseId, new BigDecimal("1"), "絞り込みグループA1");
      UUID emptyGroupId =
          testData.createLessonGroup(targetCourseId, new BigDecimal("2"), "絞り込みグループA2");
      UUID otherCourseGroupId =
          testData.createLessonGroup(otherCourseId, new BigDecimal("1"), "絞り込みグループB1");
      UUID targetLessonId =
          testData.createLesson(
              targetGroupId, targetCourseId, new BigDecimal("1"), "絞り込み対象レッスン", "説明", null);
      testData.createLesson(
          emptyGroupId, targetCourseId, new BigDecimal("1"), "絞り込み対象外レッスンA2", "説明", null);
      testData.createLesson(
          otherCourseGroupId, otherCourseId, new BigDecimal("1"), "絞り込み対象外レッスンB1", "説明", null);
      testData.createLessonTag(targetLessonId, testData.createTag("絞り込みタグ"));

      // Act
      LessonTagSearchResultDto result = searchByTag("絞り込みタグ", 1, 10);

      // Assert
      assertEquals(
          List.of(targetCourseId),
          result.courses().stream().map(LessonTagSearchCourseDto::courseId).toList());
      assertEquals(
          List.of(targetGroupId),
          result.courses().getFirst().lessonGroups().stream()
              .map(LessonTagSearchLessonGroupDto::lessonGroupId)
              .toList());
    }

    @Test
    void レッスンに紐づくタグが検索条件以外のタグも含めて返ること() {
      // Arrange
      UUID courseId = testData.createCourse(new BigDecimal("1"), "複数タグコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "複数タググループ");
      UUID lessonId =
          testData.createLesson(
              lessonGroupId, courseId, new BigDecimal("1"), "複数タグレッスン", "説明", null);
      UUID javaTagId = testData.createTag("複数タグJava");
      UUID springTagId = testData.createTag("複数タグSpring");
      testData.createLessonTag(lessonId, javaTagId);
      testData.createLessonTag(lessonId, springTagId);

      // Act
      LessonTagSearchResultDto result = searchByTag("複数タグJava", 1, 10);

      // Assert
      List<UUID> tagIds =
          result.courses().getFirst().lessonGroups().getFirst().lessons().getFirst().tags().stream()
              .map(TagDto::id)
              .toList();
      assertEquals(2, tagIds.size());
      assertTrue(tagIds.contains(javaTagId));
      assertTrue(tagIds.contains(springTagId));
    }

    @Test
    void 前後に空白を含むタグ名でも検索できること() {
      // Arrange
      UUID courseId = testData.createCourse(new BigDecimal("1"), "空白タグコース", "コース説明");
      UUID lessonGroupId = testData.createLessonGroup(courseId, new BigDecimal("1"), "空白タググループ");
      UUID lessonId =
          testData.createLesson(
              lessonGroupId, courseId, new BigDecimal("1"), "空白タグレッスン", "説明", null);
      testData.createLessonTag(lessonId, testData.createTag("空白タグJava"));

      // Act
      LessonTagSearchResultDto result = searchByTag("  空白タグJava  ", 1, 10);

      // Assert - 検索に使用したタグ名も前後の空白を取り除いた値が返る
      assertEquals("空白タグJava", result.tag());
      assertEquals(1, result.totalSize());
      assertEquals(
          List.of(lessonId), lessonIdsOf(result.courses().getFirst().lessonGroups().getFirst()));
    }

    @Test
    void 該当するレッスンがない場合に空のコース一覧と総件数0が返ること() {
      // Act
      LessonTagSearchResultDto result = searchByTag("存在しないタグ", 1, 10);

      // Assert
      assertEquals("存在しないタグ", result.tag());
      assertTrue(result.courses().isEmpty());
      assertEquals(0, result.totalSize());
      assertEquals(1, result.pageNum());
      assertEquals(10, result.pageSize());
    }

    @Test
    void 指定したページ番号と件数でレッスンがページングされること() {
      // Arrange
      List<UUID> lessonIds = createTaggedLessons("ページングタグ", 3);

      // Act
      LessonTagSearchResultDto firstPage = searchByTag("ページングタグ", 1, 2);
      LessonTagSearchResultDto secondPage = searchByTag("ページングタグ", 2, 2);

      // Assert
      assertEquals(1, firstPage.pageNum());
      assertEquals(2, firstPage.pageSize());
      assertEquals(
          List.of(lessonIds.get(0), lessonIds.get(1)),
          lessonIdsOf(firstPage.courses().getFirst().lessonGroups().getFirst()));
      assertEquals(2, secondPage.pageNum());
      assertEquals(
          List.of(lessonIds.get(2)),
          lessonIdsOf(secondPage.courses().getFirst().lessonGroups().getFirst()));
    }

    @Test
    void 総件数がページサイズによらず該当レッスンの全件数になること() {
      // Arrange
      createTaggedLessons("総件数タグ", 3);

      // Act
      LessonTagSearchResultDto result = searchByTag("総件数タグ", 1, 2);

      // Assert
      assertEquals(3, result.totalSize());
      assertEquals(2, lessonIdsOf(result.courses().getFirst().lessonGroups().getFirst()).size());
    }

    @Test
    void ページ番号が1未満の場合にInvalidValueExceptionが投げられること() {
      InvalidValueException exception =
          assertThrows(InvalidValueException.class, () -> searchByTag("タグ", 0, 10));

      assertEquals("ページ番号は1以上を指定してください", exception.getMessage());
    }

    @Test
    void ページサイズが1未満の場合にInvalidValueExceptionが投げられること() {
      InvalidValueException exception =
          assertThrows(InvalidValueException.class, () -> searchByTag("タグ", 1, 0));

      assertEquals("ページサイズは1以上を指定してください", exception.getMessage());
    }

    /**
     * 指定したタグ名・ページ情報でタグ検索を実行する。
     *
     * @param tag タグ名
     * @param pageNum ページ番号
     * @param pageSize 1ページ当たりの件数
     * @return タグ検索結果
     */
    private LessonTagSearchResultDto searchByTag(String tag, int pageNum, int pageSize) {
      return lessonApplicationService.searchLessonsByTag(
          new LessonTagSearchRequest(tag, pageNum, pageSize).toCommand());
    }

    /**
     * 1つのレッスングループ配下に、同じタグを付けたレッスンを表示順の昇順で作成する。
     *
     * @param tagName 付与するタグ名
     * @param lessonCount 作成するレッスン数
     * @return 作成したレッスンIDを表示順の昇順で並べたリスト
     */
    private List<UUID> createTaggedLessons(String tagName, int lessonCount) {
      UUID courseId = testData.createCourse(new BigDecimal("1"), tagName + "コース", "コース説明");
      UUID lessonGroupId =
          testData.createLessonGroup(courseId, new BigDecimal("1"), tagName + "グループ");
      UUID tagId = testData.createTag(tagName);
      List<UUID> lessonIds = new ArrayList<>();
      for (int lessonNumber = 1; lessonNumber <= lessonCount; lessonNumber++) {
        UUID lessonId =
            testData.createLesson(
                lessonGroupId,
                courseId,
                new BigDecimal(lessonNumber),
                tagName + "レッスン" + lessonNumber,
                "説明",
                null);
        testData.createLessonTag(lessonId, tagId);
        lessonIds.add(lessonId);
      }
      return lessonIds;
    }

    /**
     * レッスングループ配下のレッスンIDを、返却された順のまま取り出す。
     *
     * @param lessonGroup タグ検索結果のレッスングループ
     * @return レッスンIDのリスト
     */
    private List<UUID> lessonIdsOf(LessonTagSearchLessonGroupDto lessonGroup) {
      return lessonGroup.lessons().stream().map(LessonTagSearchLessonDto::lessonId).toList();
    }
  }
}
