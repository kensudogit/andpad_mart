package jp.andpad.api.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jp.andpad.api.AbstractIntegrationTest;

import jp.andpad.api.demo.DemoCatalog;
import jp.andpad.api.demo.DemoCatalog.CatalogVideo;

class DemoCatalogTest extends AbstractIntegrationTest {

    @Test
    void catalogHasVideosAndPaths() {
        assertThat(DemoCatalog.videos()).hasSize(10);
        assertThat(DemoCatalog.paths()).hasSize(6);
        CatalogVideo v = DemoCatalog.videos().getFirst();
        assertThat(v.thumbnailUrl()).contains("youtube.com");
    }

}
