package dev.kwlew;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionSupportTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "1.18.2-R0.1-SNAPSHOT",
            "1.19-R0.1-SNAPSHOT",
            "1.19.4-R0.1-SNAPSHOT",
            "1.20.6-R0.1-SNAPSHOT",
            "1.21.11-R0.1-SNAPSHOT",
            "26.1.2-R0.1-SNAPSHOT",
            "26.3-R0.1-SNAPSHOT",
    })
    void supportsEverythingFrom1_18_2(String bukkitVersion) {
        assertTrue(VersionSupport.supports(bukkitVersion));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1.18-R0.1-SNAPSHOT",
            "1.18.1-R0.1-SNAPSHOT",
            "1.17.1-R0.1-SNAPSHOT",
            "1.16.5-R0.1-SNAPSHOT",
            "1.8.8-R0.1-SNAPSHOT",
    })
    void rejectsVersionsOlderThan1_18_2(String bukkitVersion) {
        assertFalse(VersionSupport.supports(bukkitVersion));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "unknown", "R0.1-SNAPSHOT", "1"})
    void rejectsUnparseableVersions(String bukkitVersion) {
        assertFalse(VersionSupport.supports(bukkitVersion));
    }

    @Test
    void brigadierStartsAt1_20_6() {
        assertFalse(VersionSupport.supportsBrigadier("1.18.2-R0.1-SNAPSHOT"));
        assertFalse(VersionSupport.supportsBrigadier("1.20.4-R0.1-SNAPSHOT"));
        assertTrue(VersionSupport.supportsBrigadier("1.20.6-R0.1-SNAPSHOT"));
        assertTrue(VersionSupport.supportsBrigadier("1.21.1-R0.1-SNAPSHOT"));
        assertTrue(VersionSupport.supportsBrigadier("26.3-R0.1-SNAPSHOT"));
    }
}
