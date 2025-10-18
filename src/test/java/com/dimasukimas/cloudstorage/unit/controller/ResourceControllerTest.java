package com.dimasukimas.cloudstorage.unit.controller;

import com.dimasukimas.cloudstorage.config.security.WithCustomUser;
import com.dimasukimas.cloudstorage.controller.ResourceController;
import com.dimasukimas.cloudstorage.dto.ResourceInfoDto;
import com.dimasukimas.cloudstorage.repository.ContentSource;
import com.dimasukimas.cloudstorage.repository.ObjectContentSource;
import com.dimasukimas.cloudstorage.repository.ZipContentSource;
import com.dimasukimas.cloudstorage.repository.ZipEntrySpec;
import com.dimasukimas.cloudstorage.service.ResourceManagerService;
import com.dimasukimas.cloudstorage.service.ResourceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResourceController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ResourceControllerTest {

    @MockitoBean
    private ResourceManagerService resourceManagerService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithCustomUser
    void getResourceInfo_shouldReturnResourceInfo() throws Exception {
        var resInfo = new ResourceInfoDto("folder1/", "folder2/", null, ResourceType.DIRECTORY);

        when(resourceManagerService.getResourceInfo(1L, "folder2/")).thenReturn(resInfo);

        mockMvc.perform(get("/resource").param("path", "folder2/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value("folder1/"))
                .andExpect(jsonPath("$.name").value("folder2/"))
                .andExpect(jsonPath("$.type").value(ResourceType.DIRECTORY.toString()));
    }

    @Test
    @WithCustomUser
    void uploadFile_shouldReturnResourceInfo() throws Exception {
        byte[] bytes = "test".getBytes();
        var file = new MockMultipartFile("file", "test.txt", "text/plain", bytes);
        var resInfo = new ResourceInfoDto("folder1/", "test.txt", file.getSize(), ResourceType.FILE);

        when(resourceManagerService.upload(1L, "folder1/", file)).thenReturn(resInfo);

        mockMvc.perform(multipart("/resource")
                        .file(file)
                        .queryParam("path", "folder1/")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.path").value("folder1/"))
                .andExpect(jsonPath("$.name").value("test.txt"))
                .andExpect(jsonPath("$.size").value(file.getSize()))
                .andExpect(jsonPath("$.type").value(ResourceType.FILE.toString()));
    }

    @Test
    @WithCustomUser
    @DisplayName("204 when delete file")
    void deleteResource_shouldReturnNoContent() throws Exception {
        doNothing().when(resourceManagerService).delete(anyLong(), anyString());

        mockMvc.perform(delete("/resource").param("path", "folder1/"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    @WithCustomUser
    @DisplayName("200 with content-length when download file")
    void downloadFile_shouldReturnOkWithContentLength() throws Exception {
        Supplier<InputStream> content = () -> new ByteArrayInputStream("test".getBytes());
        ResourceInfoDto metadata = new ResourceInfoDto("folder1/test.txt", "test.txt", 4L, ResourceType.FILE);
        ContentSource contentSource = new ObjectContentSource(content, metadata);

        when(resourceManagerService.download(anyLong(), anyString())).thenReturn(contentSource);

        mockMvc.perform(get("/resource/download").param("path", "folder1/test.txt"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/octet-stream"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.txt\"; filename*=UTF-8''test.txt"))
                .andExpect(header().string("Content-Length", String.valueOf(4)));
    }

    @Test
    @WithCustomUser
    @DisplayName("200 without content-length when download directory")
    void downloadDirectory_shouldReturnOkWithoutContentLength() throws Exception {
        Supplier<InputStream> content = () -> new ByteArrayInputStream("test".getBytes());
        ZipEntrySpec zipEntrySpec = new ZipEntrySpec("test.txt", null, ResourceType.FILE, content);
        ContentSource contentSource = new ZipContentSource("folder1", List.of(zipEntrySpec));

        when(resourceManagerService.download(anyLong(), anyString())).thenReturn(contentSource);

        mockMvc.perform(get("/resource/download").param("path", "folder1/"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/octet-stream"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"folder1.zip\"; filename*=UTF-8''folder1.zip"))
                .andExpect(header().doesNotExist("Content-Length"));
    }


}
