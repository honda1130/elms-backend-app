package com.everrefine.elms.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.everrefine.elms.application.command.FeatureFlagUpdateCommand;
import com.everrefine.elms.application.dto.FeatureFlagDto;
import com.everrefine.elms.testsupport.TestDataFactory;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
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

/** {@link FeatureFlagApplicationServiceImpl} の結合テストクラス。 */
@ActiveProfiles("dev")
@SpringBootTest(webEnvironment = WebEnvironment.NONE) // WebまわりのConfigurationはBean生成を無効にして高速化する。
@Testcontainers // DBはDockerコンテナを使用する。
@Transactional // 各テストメソッド終了時にテストデータをロールバックする。
class FeatureFlagApplicationServiceImplTest {

  /** テストで使うDBを用意する。 */
  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17").withReuse(true);

  /** テスト対象のサービスクラス。 */
  @Autowired private FeatureFlagApplicationService featureFlagApplicationService;

  /** テストデータ作成ヘルパー。 */
  @Autowired private TestDataFactory testData;

  /** 検証用のSQL実行に使用する。 */
  @Autowired private JdbcTemplate jdbcTemplate;

  @Nested
  class フィーチャーフラグ取得 {

    @Test
    void 有効なフラグを指定すると有効で返ること() {
      testData.createFeatureFlag("enabled-feature", true);

      FeatureFlagDto featureFlag = featureFlagApplicationService.getFeatureFlag("enabled-feature");

      assertEquals("enabled-feature", featureFlag.featureFlagKey());
      assertTrue(featureFlag.enabled());
    }

    @Test
    void 無効なフラグを指定すると無効で返ること() {
      testData.createFeatureFlag("disabled-feature", false);

      FeatureFlagDto featureFlag = featureFlagApplicationService.getFeatureFlag("disabled-feature");

      assertEquals("disabled-feature", featureFlag.featureFlagKey());
      assertFalse(featureFlag.enabled());
    }

    /**
     * 未登録のキーが例外にならず無効として返ることを検証する。
     *
     * <p>フラグが未作成の状態でも呼び出し側が分岐できるようにするための仕様であり、 リソース未検出として扱うと呼び出し側が例外処理を強いられる。
     */
    @Test
    void 未登録のキーを指定すると無効で返ること() {
      FeatureFlagDto featureFlag = featureFlagApplicationService.getFeatureFlag("unknown-key");

      assertEquals("unknown-key", featureFlag.featureFlagKey());
      assertFalse(featureFlag.enabled());
    }

    /** マイグレーションで投入したウェルカムメール用のフラグが、初期状態では無効であることを検証する。 */
    @Test
    void ウェルカムメールのフラグが初期状態では無効であること() {
      FeatureFlagDto featureFlag = featureFlagApplicationService.getFeatureFlag("welcome-mail");

      assertEquals("welcome-mail", featureFlag.featureFlagKey());
      assertFalse(featureFlag.enabled());
    }
  }

  @Nested
  class フィーチャーフラグ更新 {

    @Test
    void 無効なフラグを有効に更新できること() {
      UUID featureFlagId = testData.createFeatureFlag("target-feature", false);

      FeatureFlagDto featureFlag =
          featureFlagApplicationService.updateFeatureFlag(
              new FeatureFlagUpdateCommand("target-feature", true));

      assertEquals("target-feature", featureFlag.featureFlagKey());
      assertTrue(featureFlag.enabled());
      Boolean enabled =
          jdbcTemplate.queryForObject(
              "SELECT enabled FROM feature_flags WHERE id = ?", Boolean.class, featureFlagId);
      assertEquals(Boolean.TRUE, enabled);
    }

    @Test
    void 有効なフラグを無効に更新できること() {
      UUID featureFlagId = testData.createFeatureFlag("target-feature", true);

      FeatureFlagDto featureFlag =
          featureFlagApplicationService.updateFeatureFlag(
              new FeatureFlagUpdateCommand("target-feature", false));

      assertEquals("target-feature", featureFlag.featureFlagKey());
      assertFalse(featureFlag.enabled());
      Boolean enabled =
          jdbcTemplate.queryForObject(
              "SELECT enabled FROM feature_flags WHERE id = ?", Boolean.class, featureFlagId);
      assertEquals(Boolean.FALSE, enabled);
    }

    /** 既存のキーを指定した場合に、新規登録ではなく同じレコードが更新されることを検証する。 */
    @Test
    void 登録済みのキーを更新するとレコードが増えずに更新日時が更新されること() {
      UUID featureFlagId = testData.createFeatureFlag("target-feature", false);
      LocalDateTime oldUpdatedAt = LocalDateTime.of(2000, 1, 1, 0, 0);
      jdbcTemplate.update(
          "UPDATE feature_flags SET created_at = ?, updated_at = ? WHERE id = ?",
          oldUpdatedAt,
          oldUpdatedAt,
          featureFlagId);

      featureFlagApplicationService.updateFeatureFlag(
          new FeatureFlagUpdateCommand("target-feature", true));

      Map<String, Object> row =
          jdbcTemplate.queryForMap(
              "SELECT id, created_at, updated_at FROM feature_flags WHERE feature_flag_key = ?",
              "target-feature");
      assertEquals(featureFlagId, row.get("id"));
      assertEquals(oldUpdatedAt, ((Timestamp) row.get("created_at")).toLocalDateTime());
      assertTrue(((Timestamp) row.get("updated_at")).toLocalDateTime().isAfter(oldUpdatedAt));
    }

    /**
     * 未登録のキーが例外にならず新規登録されることを検証する。
     *
     * <p>フラグの事前登録なしに機能を切り替えられるようにするための仕様であり、リソース未検出として扱わない。
     */
    @Test
    void 未登録のキーを指定すると新規登録されること() {
      FeatureFlagDto featureFlag =
          featureFlagApplicationService.updateFeatureFlag(
              new FeatureFlagUpdateCommand("new-feature", true));

      assertEquals("new-feature", featureFlag.featureFlagKey());
      assertTrue(featureFlag.enabled());
      Map<String, Object> row =
          jdbcTemplate.queryForMap(
              "SELECT enabled, created_at, updated_at FROM feature_flags WHERE feature_flag_key = ?",
              "new-feature");
      assertEquals(Boolean.TRUE, row.get("enabled"));
      assertNotNull(row.get("created_at"));
      assertNotNull(row.get("updated_at"));
    }

    /** キーの上限（VARCHAR(100)）ちょうどの長さで新規登録できることを検証する。 */
    @Test
    void 上限の100文字のキーで新規登録できること() {
      String maxLengthKey = "a".repeat(100);

      featureFlagApplicationService.updateFeatureFlag(
          new FeatureFlagUpdateCommand(maxLengthKey, true));

      Integer count =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM feature_flags WHERE feature_flag_key = ?",
              Integer.class,
              maxLengthKey);
      assertEquals(1, count);
    }
  }
}
