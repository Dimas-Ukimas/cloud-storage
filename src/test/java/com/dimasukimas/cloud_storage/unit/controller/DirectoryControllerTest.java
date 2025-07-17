package com.dimasukimas.cloud_storage.unit.controller;


import com.dimasukimas.cloud_storage.config.TestSecurityConfig;
import com.dimasukimas.cloud_storage.controller.DirectoryController;
import com.dimasukimas.cloud_storage.dto.DirectoryInfoDto;
import com.dimasukimas.cloud_storage.exception.handler.GlobalExceptionHandler;
import com.dimasukimas.cloud_storage.service.FileManagerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.data.redis.connection.ReactiveStreamCommands.AddStreamRecord.body;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DirectoryController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
public class DirectoryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    public FileManagerService fileManagerService;

    private String fileInfoJson = """
            {
               "path": ""
               "name": "folder1/",
               "size": ""
               "type": "DIRECTORY"
             }
            """;

    @Test
    void whenCreateValidDirectory_shouldReturnDirectoryInfo() throws Exception {

        DirectoryInfoDto directoryInfo = new DirectoryInfoDto("", "folder1/", "DIRECTORY");
        when(fileManagerService.createDirectory(1L, "folder1/")).thenReturn(directoryInfo);


        mockMvc.perform(post("/directory")
                        .param("path", "folder1/"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.path").value(""),
                        jsonPath("$.name").value("folder1"),
                        jsonPath("$.type").value("DIRECTORY"));
    }

}
