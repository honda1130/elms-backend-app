package com.everrefine.elms.application.service;

import com.everrefine.elms.application.dto.UserCourseDto;
import com.everrefine.elms.application.exception.ResourceNotFoundException;
import com.everrefine.elms.domain.model.PagerForRequest;
import com.everrefine.elms.domain.model.course.Course;
import com.everrefine.elms.domain.model.user.CourseLessonCount;
import com.everrefine.elms.domain.model.user.ProgressRate;
import com.everrefine.elms.domain.model.user.User;
import com.everrefine.elms.domain.repository.CourseRepository;
import com.everrefine.elms.domain.repository.UserLessonRepository;
import com.everrefine.elms.domain.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** ユーザーコースアプリケーションサービスの実装。 */
@Service
@AllArgsConstructor
public class UserCourseApplicationServiceImpl implements UserCourseApplicationService {

  private final UserRepository userRepository;
  private final CourseRepository courseRepository;
  private final UserLessonRepository userLessonRepository;

  private static final int FIRST_PAGE_NUM = 1;

  /**
   * 引数に指定されたユーザーIDに紐づくユーザーのコース一覧を取得する。
   *
   * @param userId ユーザーID
   * @return 引数に指定されたユーザーIDに紐づくユーザーのコース一覧
   */
  @Override
  @Transactional(readOnly = true)
  public List<UserCourseDto> findUserCourses(UUID userId) {
    // ユーザーがいるかの判定
    userRepository
        .findUserById(userId)
        .orElseThrow(() -> new ResourceNotFoundException(User.class, String.valueOf(userId)));
    // 全件数を数えて、1ページに全部載せる
    int totalCourses = courseRepository.countCourses();
    if (totalCourses == 0) {
      return List.of();
    }

    PagerForRequest pager = new PagerForRequest(FIRST_PAGE_NUM, totalCourses);
    List<Course> courses = courseRepository.findCourses(pager);
    // コースごとにクエリを発行しないよう、全コース分の件数をまとめて取得する
    Map<UUID, CourseLessonCount> lessonCounts = userLessonRepository.countLessonsPerCourse(userId);

    return courses.stream()
        .map(
            course -> {
              // レッスンが1件もないコースは集約結果に含まれない
              CourseLessonCount lessonCount =
                  lessonCounts.getOrDefault(course.id(), CourseLessonCount.NO_LESSONS);
              BigDecimal progress =
                  ProgressRate.of(lessonCount.completedLessonCount(), lessonCount.allLessonCount())
                      .toBigDecimal();
              return UserCourseDto.from(course, progress);
            })
        .toList();
  }
}
