package com.everrefine.elms.infrastructure.row;

import com.everrefine.elms.domain.model.lesson.LessonTagSearchCourse;
import com.everrefine.elms.domain.model.lesson.LessonTagSearchLesson;
import com.everrefine.elms.domain.model.lesson.LessonTagSearchLessonGroup;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.annotation.Id;

/** タグ検索でコース・レッスングループ・レッスンをJOINしたセレクト結果の1行。 */
public record LessonTagSearchRow(
    @Id UUID lessonId,
    BigDecimal lessonOrder,
    String lessonTitle,
    UUID lessonGroupId,
    BigDecimal lessonGroupOrder,
    String lessonGroupTitle,
    UUID courseId,
    BigDecimal courseOrder,
    String courseTitle) {

  /**
   * JOIN結果の行一覧を、コース・レッスングループ単位で入れ子にまとめた読み取りモデルへ変換する。
   *
   * <p>行はコース順・レッスングループ順・レッスン順で並んでいることを前提とし、その並び順を保持する。
   *
   * @param rows JOIN結果の行一覧
   * @return コースごとにレッスングループとレッスンをまとめた読み取りモデル一覧
   */
  public static List<LessonTagSearchCourse> toDomainList(List<LessonTagSearchRow> rows) {
    return rows.stream()
        .collect(
            Collectors.groupingBy(
                LessonTagSearchRow::courseId, LinkedHashMap::new, Collectors.toList()))
        .values()
        .stream()
        .map(LessonTagSearchRow::toCourse)
        .toList();
  }

  /**
   * 同一コースに属する行一覧を、1つのコースの読み取りモデルへ変換する。
   *
   * @param courseRows 同一コースの行一覧
   * @return コースの読み取りモデル
   */
  private static LessonTagSearchCourse toCourse(List<LessonTagSearchRow> courseRows) {
    LessonTagSearchRow head = courseRows.getFirst();
    List<LessonTagSearchLessonGroup> lessonGroups =
        courseRows.stream()
            .collect(
                Collectors.groupingBy(
                    LessonTagSearchRow::lessonGroupId, LinkedHashMap::new, Collectors.toList()))
            .values()
            .stream()
            .map(LessonTagSearchRow::toLessonGroup)
            .toList();
    return new LessonTagSearchCourse(
        head.courseId(), head.courseOrder(), head.courseTitle(), lessonGroups);
  }

  /**
   * 同一レッスングループに属する行一覧を、1つのレッスングループの読み取りモデルへ変換する。
   *
   * @param groupRows 同一レッスングループの行一覧
   * @return レッスングループの読み取りモデル
   */
  private static LessonTagSearchLessonGroup toLessonGroup(List<LessonTagSearchRow> groupRows) {
    LessonTagSearchRow head = groupRows.getFirst();
    List<LessonTagSearchLesson> lessons =
        groupRows.stream().map(LessonTagSearchRow::toLesson).toList();
    return new LessonTagSearchLessonGroup(
        head.lessonGroupId(), head.lessonGroupOrder(), head.lessonGroupTitle(), lessons);
  }

  /**
   * この行のレッスン部分を、レッスンの読み取りモデルへ変換する。
   *
   * @return レッスンの読み取りモデル
   */
  private LessonTagSearchLesson toLesson() {
    return new LessonTagSearchLesson(lessonId, lessonOrder, lessonTitle);
  }
}
