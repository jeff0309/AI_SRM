package com.gsrm.importer;

import com.gsrm.domain.entity.SatelliteRequest;
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
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * CsvRequestImporter 單元測試.
 *
 * @author Jeff
 * @since 2026-03-06
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CsvRequestImporter 測試")
class CsvRequestImporterTest {

    @Mock private SatelliteRepository     satelliteRepository;
    @Mock private GroundStationRepository groundStationRepository;
    @Mock private ScheduleSessionRepository sessionRepository;

    private CsvRequestImporter importer;

    @BeforeEach
    void setUp() {
        importer = new CsvRequestImporter(satelliteRepository, groundStationRepository, sessionRepository);
    }

    @Test
    @DisplayName("getSupportedFileType() 回傳 CSV")
    void shouldReturnCsvAsFileType() {
        assertThat(importer.getSupportedFileType()).isEqualTo("CSV");
    }

    @Test
    @DisplayName("supports() 支援 .csv 副檔名")
    void shouldSupportCsvExtension() {
        assertThat(importer.supports("requests.csv")).isTrue();
        assertThat(importer.supports("REQUESTS.CSV")).isTrue();
    }

    @Test
    @DisplayName("getSupportedExtensions() 非空")
    void supportedExtensionsAreNotEmpty() {
        List<String> exts = importer.getSupportedExtensions();
        assertThat(exts).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("只有標頭行時回傳空清單")
    void shouldReturnEmptyListOnHeaderOnly() throws FileImporter.ImportException {
        String csvContent = "SatelliteName,GroundStationName,FrequencyBand,AOS,LOS\n";
        InputStream stream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        List<SatelliteRequest> results = importer.importRequests(stream, 1L);

        assertThat(results).isEmpty();
    }
}
