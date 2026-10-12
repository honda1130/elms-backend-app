package com.everrefine.elms.application.service;

import com.everrefine.elms.application.command.NewsCreateCommand;
import com.everrefine.elms.application.command.NewsSearchCommand;
import com.everrefine.elms.application.command.NewsUpdateCommand;
import com.everrefine.elms.application.dto.NewsDto;
import com.everrefine.elms.application.dto.NewsPageDto;
import com.everrefine.elms.application.exception.ResourceNotFoundException;
import com.everrefine.elms.application.util.CsvUtils;
import com.everrefine.elms.domain.model.news.News;
import com.everrefine.elms.domain.repository.NewsRepository;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** お知らせアプリケーションサービスの実装。 */
@Service
@AllArgsConstructor
public class NewsApplicationServiceImpl implements NewsApplicationService {

  private final NewsRepository newsRepository;

  @Override
  @Transactional(readOnly = true)
  public NewsDto findNewsById(UUID newsId) {
    News news =
        newsRepository
            .findNewsById(newsId)
            .orElseThrow(() -> new ResourceNotFoundException(News.class, String.valueOf(newsId)));
    return NewsDto.from(news);
  }

  @Override
  @Transactional(readOnly = true)
  public NewsPageDto findNews(NewsSearchCommand newsSearchCommand) {
    var newsSearchCondition = newsSearchCommand.toSearchCondition();
    List<UUID> newsIds = newsRepository.findNewsIdsBySearchConditions(newsSearchCondition);
    List<News> news = newsRepository.findNewsByIds(newsIds);
    List<NewsDto> newsDtos = news.stream().map(NewsDto::from).toList();
    int totalSize = newsRepository.countNews(newsSearchCondition);
    return NewsPageDto.from(
        newsDtos,
        newsSearchCondition.pagerForRequest().pageNum(),
        newsSearchCondition.pagerForRequest().pageSize(),
        totalSize);
  }

  @Override
  @Transactional
  public void createNews(NewsCreateCommand newsCreateCommand) {
    newsRepository.createNews(newsCreateCommand.toNews());
  }

  @Override
  @Transactional
  public void updateNews(NewsUpdateCommand newsUpdateCommand) {
    News currentNews =
        newsRepository
            .findNewsById(newsUpdateCommand.id())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        News.class, String.valueOf(newsUpdateCommand.id())));
    newsRepository.updateNews(newsUpdateCommand.toNews(currentNews));
  }

  @Override
  @Transactional
  public void deleteNewsById(UUID newsId) {
    newsRepository.findNewsById(newsId).ifPresent(news -> newsRepository.deleteNewsById(newsId));
  }

  @Override
  @Transactional(readOnly = true)
  public Resource exportNewsCsv() {
    String[] header = {"お知らせID", "タイトル", "本文", "作成日時", "更新日時"};
    List<News> newsList = newsRepository.findAllNews();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (PrintWriter file =
        new PrintWriter(new BufferedWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8)))) {
      // Excelで開いたときの文字化けを防ぐ
      file.print('\uFEFF');
      file.println(String.join(",", header));

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
      for (News news : newsList) {
        String[] row = {
          CsvUtils.escape(String.valueOf(news.id())),
          CsvUtils.escape(news.title().value()),
          CsvUtils.escape(news.content().value()),
          CsvUtils.escape(news.createdAt().format(formatter)),
          CsvUtils.escape(news.updatedAt().format(formatter))
        };
        file.println(String.join(",", row));
      }
    } catch (Exception e) {
      throw new RuntimeException("CSVファイルの作成に失敗しました", e);
    }
    return new ByteArrayResource(baos.toByteArray());
  }
}
