package com.everrefine.elms.application.service;

import com.everrefine.elms.application.command.LessonGroupCreateCommand;
import com.everrefine.elms.application.command.LessonGroupUpdateCommand;
import com.everrefine.elms.application.dto.LessonDto;
import com.everrefine.elms.application.dto.LessonGroupDto;
import com.everrefine.elms.application.exception.ResourceNotFoundException;
import com.everrefine.elms.domain.model.course.Course;
import com.everrefine.elms.domain.model.lesson.Lesson;
import com.everrefine.elms.domain.model.lesson.LessonGroup;
import com.everrefine.elms.domain.model.tag.Tag;
import com.everrefine.elms.domain.repository.CourseRepository;
import com.everrefine.elms.domain.repository.LessonGroupRepository;
import com.everrefine.elms.domain.repository.LessonRepository;
import com.everrefine.elms.domain.repository.LessonTagRepository;
import com.everrefine.elms.domain.service.LessonGroupDomainService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** レッスングループアプリケーションサービスの実装。 */
@Service
@AllArgsConstructor
public class LessonGroupApplicationServiceImpl implements LessonGroupApplicationService {

  private final LessonGroupRepository lessonGroupRepository;
  private final LessonRepository lessonRepository;
  private final LessonTagRepository lessonTagRepository;
  private final CourseRepository courseRepository;
  private final LessonGroupDomainService lessonGroupDomainService;

  @Override
  @Transactional
  public LessonGroupDto createLessonGroup(LessonGroupCreateCommand lessonGroupCreateCommand) {
    throwExceptionIfCourseNotExists(lessonGroupCreateCommand.courseId());

    BigDecimal lessonGroupOrder =
        lessonGroupDomainService.issueLessonGroupOrder(lessonGroupCreateCommand.courseId());
    LessonGroup createdLessonGroup =
        lessonGroupRepository.createLessonGroup(
            lessonGroupCreateCommand.toLessonGroup(lessonGroupOrder));
    return LessonGroupDto.from(createdLessonGroup);
  }

  /**
   * コースが存在しない場合に例外をスローする。
   *
   * <p>検証しないまま登録すると外部キー制約違反となり、クライアント起因の誤りが500として返ってしまう。
   *
   * @param courseId コースID
   */
  private void throwExceptionIfCourseNotExists(UUID courseId) {
    courseRepository
        .findCourseById(courseId)
        .orElseThrow(() -> new ResourceNotFoundException(Course.class, String.valueOf(courseId)));
  }

  @Override
  @Transactional
  public LessonGroupDto updateLessonGroup(LessonGroupUpdateCommand lessonGroupUpdateCommand) {
    LessonGroup lessonGroup =
        lessonGroupRepository
            .findLessonGroupById(lessonGroupUpdateCommand.id())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        LessonGroup.class, String.valueOf(lessonGroupUpdateCommand.id())));
    LessonGroup persistedLessonGroup =
        lessonGroupRepository.updateLessonGroup(
            lessonGroupUpdateCommand.toLessonGroup(lessonGroup));

    List<Lesson> lessons = lessonRepository.findLessonsByLessonGroupId(persistedLessonGroup.id());
    List<UUID> lessonIds = lessons.stream().map(Lesson::id).toList();
    Map<UUID, List<Tag>> tagsByLessonId = lessonTagRepository.findTagsByLessonIdIn(lessonIds);
    List<LessonDto> lessonDtos =
        lessons.stream()
            .map(
                lesson ->
                    LessonDto.from(lesson, tagsByLessonId.getOrDefault(lesson.id(), List.of())))
            .toList();

    return LessonGroupDto.from(persistedLessonGroup, lessonDtos);
  }

  @Override
  @Transactional
  public void deleteLessonGroupById(UUID lessonGroupId) {
    // レッスングループが存在しなくてもエラーにはしない。
    lessonGroupRepository
        .findLessonGroupById(lessonGroupId)
        .ifPresent(LessonGroup -> lessonGroupRepository.deleteLessonGroupById(lessonGroupId));
  }
}
