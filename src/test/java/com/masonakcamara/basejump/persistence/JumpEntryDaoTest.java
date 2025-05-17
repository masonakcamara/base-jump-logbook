package com.masonakcamara.basejump.persistence;

import com.masonakcamara.basejump.model.JumpEntry;
import com.masonakcamara.basejump.model.JumpType;
import com.masonakcamara.basejump.model.SliderPosition;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JumpEntryDaoTest {

    private JumpEntryDao dao;

    @BeforeAll
    void setup() {
        dao = new JumpEntryDao();
    }

    @Test
    @DisplayName("Save and retrieve a JumpEntry")
    void saveAndFindById() {
        var entry = new JumpEntry(
                LocalDateTime.now(),
                "Test Cliff",
                34.1234,
                -117.5678,
                1500.0,
                "ContainerX",
                "MainY",
                "PilotZ",
                SliderPosition.DOWN,
                JumpType.TERMINAL,
                "http://example.com/media.mp4"
        );

        dao.save(entry);
        assertNotNull(entry.getId(), "ID should be generated");

        var fetched = dao.findById(entry.getId());
        assertNotNull(fetched, "Fetched entry should not be null");
        assertEquals("Test Cliff", fetched.getObjectName());
        assertEquals(34.1234, fetched.getLatitude());
        assertEquals(SliderPosition.DOWN, fetched.getSliderPosition());
    }

    @Test
    @DisplayName("Find all returns entries after save")
    void findAll() {
        // ensure at least one entry exists
        var temp = new JumpEntry(
                LocalDateTime.now(),
                "Sample Spot",
                10.0,
                20.0,
                500.0,
                "ContA",
                "MainA",
                "PilotA",
                SliderPosition.UP,
                JumpType.SUBTERMINAL,
                null
        );
        dao.save(temp);

        List<JumpEntry> list = dao.findAll();
        assertNotNull(list, "findAll should not return null");
        assertTrue(list.size() >= 1, "There should be at least one entry in the database");
    }

    @AfterAll
    void teardown() {
        HibernateUtil.shutdown();
    }
}