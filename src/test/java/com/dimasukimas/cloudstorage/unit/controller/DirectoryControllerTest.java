package com.dimasukimas.cloudstorage.unit.controller;

import com.dimasukimas.cloudstorage.config.security.WithCustomUser;
import com.dimasukimas.cloudstorage.controller.DirectoryController;
import com.dimasukimas.cloudstorage.dto.ResourceInfoDto;
import com.dimasukimas.cloudstorage.exception.handler.GlobalExceptionHandler;
import com.dimasukimas.cloudstorage.service.ResourceManagerService;
import com.dimasukimas.cloudstorage.service.ResourceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DirectoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class DirectoryControllerTest {

    @MockitoBean
    private ResourceManagerService resourceManagerService;

    @Autowired
    private MockMvc mockMvc;

    private final static String DIRECTORY_PATH = "folder1/";

    @Test
    @WithCustomUser
    void directoryCreation_shouldReturnDirectoryInfoWithCreated() throws Exception {
        var dirInfo = new ResourceInfoDto("", DIRECTORY_PATH, null, ResourceType.DIRECTORY);

        when(resourceManagerService.createDirectory(1L, DIRECTORY_PATH)).thenReturn(dirInfo);

        mockMvc.perform(post("/directory").param("path", "folder1/"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.path").value(""))
                .andExpect(jsonPath("$.name").value("folder1/"))
                .andExpect(jsonPath("$.type").value(ResourceType.DIRECTORY.toString()))
                .andExpect(jsonPath("$.size").doesNotExist());
    }

    @Test
    @WithCustomUser
    void getEmptyDirectoryInfo_shouldReturnNoInfoWithOk() throws Exception {
        when(resourceManagerService.getDirectoryContentInfo(1L, DIRECTORY_PATH)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/directory").param("path", DIRECTORY_PATH))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @WithCustomUser
    void getDirectoryInfo_shouldReturnInfoWithOk() throws Exception{
        var resource1 = new ResourceInfoDto("folder1/", "text.txt", 10L, ResourceType.FILE);
        var resource2 = new ResourceInfoDto("folder1/", "folder2/", null, ResourceType.DIRECTORY);

        when(resourceManagerService.getDirectoryContentInfo(1L, DIRECTORY_PATH)).thenReturn(List.of(resource1, resource2));

        mockMvc.perform(get("/directory").param("path", DIRECTORY_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].path").value("folder1/"))
                .andExpect(jsonPath("$[0].name").value("text.txt"))
                .andExpect(jsonPath("$[0].size").value("10"))
                .andExpect(jsonPath("$[0].type").value(ResourceType.FILE.toString()))
                .andExpect(jsonPath("$[1].path").value("folder1/"))
                .andExpect(jsonPath("$[1].name").value("folder2/"))
                .andExpect(jsonPath("$[1].size").doesNotExist())
                .andExpect(jsonPath("$[1].type").value(ResourceType.DIRECTORY.toString()));
    }

}
