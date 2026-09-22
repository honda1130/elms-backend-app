package com.everrefine.elms.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.everrefine.elms.application.dto.FeatureFlagDto;
import com.everrefine.elms.testsupport.TestDataFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
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
}
