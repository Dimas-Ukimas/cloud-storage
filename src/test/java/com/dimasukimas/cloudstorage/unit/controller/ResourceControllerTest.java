package com.dimasukimas.cloudstorage.unit.controller;

import com.dimasukimas.cloudstorage.config.security.WithCustomUser;
import com.dimasukimas.cloudstorage.controller.ResourceController;
import com.dimasukimas.cloudstorage.dto.ResourceInfoResponseDto;
import com.dimasukimas.cloudstorage.model.storage.ContentSource;
import com.dimasukimas.cloudstorage.model.storage.ObjectContentSource;
import com.dimasukimas.cloudstorage.model.storage.ZipContentSource;
import com.dimasukimas.cloudstorage.model.storage.ZipEntrySpec;
import com.dimasukimas.cloudstorage.service.ResourceService;
import com.dimasukimas.cloudstorage.service.ResourceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResourceController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ResourceControllerTest {

    @MockitoBean
    private ResourceService resourceService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithCustomUser
    @DisplayName("200 when get resource")
    void getResourceInfo_shouldReturnResourceInfo() throws Exception {
        var resInfo = new ResourceInfoResponseDto("folder1/", "folder2/", null, ResourceType.DIRECTORY);

        when(resourceService.getResourceInfo(1L, "folder2/")).thenReturn(resInfo);

        mockMvc.perform(get("/resource").param("path", "folder2/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value("folder1/"))
                .andExpect(jsonPath("$.name").value("folder2/"))
                .andExpect(jsonPath("$.type").value(ResourceType.DIRECTORY.toString()));
    }

    @Test
    @WithCustomUser
    @DisplayName("201 when upload file")
    void uploadFile_shouldReturnResourceInfo() throws Exception {
        byte[] bytes = "test".getBytes();
        var file = new MockMultipartFile("object", "test.txt", "text/plain", bytes);
        var resInfo = new ResourceInfoResponseDto("folder1/", "test.txt", file.getSize(), ResourceType.FILE);

        when(resourceService.upload(eq(1L), eq("folder1/"), anyList())).thenReturn(List.of(resInfo));

        mockMvc.perform(multipart("/resource")
                        .file(file)
                        .param("path", "folder1/"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].path").value("folder1/"))
                .andExpect(jsonPath("$[0].name").value("test.txt"))
                .andExpect(jsonPath("$[0].size").value(bytes.length))
                .andExpect(jsonPath("$[0].type").value(ResourceType.FILE.toString()));
    }

    @Test
    @WithCustomUser
    @DisplayName("204 when delete file")
    void deleteResource_shouldReturnNoContent() throws Exception {
        doNothing().when(resourceService).delete(anyLong(), anyString());

        mockMvc.perform(delete("/resource").param("path", "folder1/"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    @WithCustomUser
    @DisplayName("200 with content-length when download file")
    void downloadFile_shouldReturnOkWithContentLength() throws Exception {
        Supplier<InputStream> content = () -> new ByteArrayInputStream("test".getBytes());
        ResourceInfoResponseDto metadata = new ResourceInfoResponseDto("folder1/test.txt", "test.txt", 4L, ResourceType.FILE);
        ContentSource contentSource = new ObjectContentSource(content, metadata);

        when(resourceService.download(anyLong(), anyString())).thenReturn(contentSource);

        mockMvc.perform(get("/resource/download").param("path", "folder1/test.txt"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/octet-stream"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.txt\""))
                .andExpect(header().string("Content-Length", String.valueOf(4)));
    }

    @Test
    @WithCustomUser
    @DisplayName("200 without content-length when download directory")
    void downloadDirectory_shouldReturnOkWithoutContentLength() throws Exception {
        Supplier<InputStream> content = () -> new ByteArrayInputStream("test".getBytes());
        ZipEntrySpec zipEntrySpec = new ZipEntrySpec("test.txt", null, ResourceType.FILE, content);
        ContentSource contentSource = new ZipContentSource("folder1", List.of(zipEntrySpec));

        when(resourceService.download(anyLong(), anyString())).thenReturn(contentSource);

        mockMvc.perform(get("/resource/download").param("path", "folder1/"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/octet-stream"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"folder1.zip\""))
                .andExpect(header().doesNotExist("Content-Length"));
    }

    @Test
    @WithCustomUser
    @DisplayName("200 with resource metadata when move")
    void moveResource_shouldReturnOkWithoutMetadata() throws Exception {
        var movedResInfo = new ResourceInfoResponseDto("folder1/folder3/", "test.txt", 7L, ResourceType.FILE);

        when(resourceService.moveOrRename(anyLong(), anyString(), anyString())).thenReturn(movedResInfo);

        mockMvc.perform(get("/resource/move").param("from", "folder1/").param("to", "folder3/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value("folder1/folder3/"))
                .andExpect(jsonPath("$.name").value("test.txt"))
                .andExpect(jsonPath("$.size").value(movedResInfo.size()))
                .andExpect(jsonPath("$.type").value(movedResInfo.type().name()));
    }


}
