package com.gsrm.importer;

import com.gsrm.repository.GroundStationRepository;
import com.gsrm.repository.SatelliteRepository;
import com.gsrm.repository.ScheduleSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlRequestImporter 單元測試.
 *
 * @author Jeff
 * @since 2026-03-06
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("XmlRequestImporter 測試")
class XmlRequestImporterTest {

    @Mock private SatelliteRepository     satelliteRepository;
    @Mock private GroundStationRepository groundStationRepository;
    @Mock private ScheduleSessionRepository sessionRepository;

    private XmlRequestImporter importer;

    @BeforeEach
    void setUp() {
        importer = new XmlRequestImporter(satelliteRepository, groundStationRepository, sessionRepository);
    }

    @Test
    @DisplayName("getSupportedFileType() 回傳 XML")
    void shouldReturnXmlAsFileType() {
        assertThat(importer.getSupportedFileType()).isEqualTo("XML");
    }

    @Test
    @DisplayName("supports() 支援 .xml 副檔名")
    void shouldSupportXmlExtension() {
        assertThat(importer.supports("requests.xml")).isTrue();
        assertThat(importer.supports("REQUESTS.XML")).isTrue();
    }

    @Test
    @DisplayName("supports() 不支援 .csv 副檔名")
    void shouldNotSupportCsvExtension() {
        assertThat(importer.supports("requests.csv")).isFalse();
    }

    @Test
    @DisplayName("空串流拋出 ImportException")
    void shouldThrowOnEmptyInput() {
        InputStream emptyStream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> importer.importRequests(emptyStream, 1L))
                .isInstanceOf(FileImporter.ImportException.class);
    }

    @Test
    @DisplayName("無效 XML 格式拋出 ImportException")
    void shouldThrowOnInvalidXml() {
        String invalidXml = "NOT VALID XML <<<<";
        InputStream stream = new ByteArrayInputStream(invalidXml.getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> importer.importRequests(stream, 1L))
                .isInstanceOf(FileImporter.ImportException.class);
    }
}
