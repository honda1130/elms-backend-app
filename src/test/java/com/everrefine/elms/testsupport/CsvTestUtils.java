package com.everrefine.elms.testsupport;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.io.Resource;

/** CSV出力のテストで共通利用するヘルパー。 */
public final class CsvTestUtils {

  private CsvTestUtils() {}

  /**
   * CSVを読み込み、全行を返す。先頭行にBOMが付いている場合は取り除く。
   *
   * @param csv 読み込むCSVリソース
   * @return 全行（ヘッダー行を含む）
   */
  public static List<String[]> readCsvRows(Resource csv)
      throws IOException, CsvValidationException {
    List<String[]> rows = new ArrayList<>();
    try (CSVReader csvReader =
        new CSVReader(new InputStreamReader(csv.getInputStream(), StandardCharsets.UTF_8))) {
      String[] row;
      while ((row = csvReader.readNext()) != null) {
        rows.add(row);
      }
    }
    if (!rows.isEmpty() && rows.get(0)[0].startsWith("\uFEFF")) {
      rows.get(0)[0] = rows.get(0)[0].substring(1);
    }
    return rows;
  }
}
