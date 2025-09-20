package com.dimasukimas.cloudstorage.unit.controller;

import com.dimasukimas.cloudstorage.config.security.WithCustomUser;
import com.dimasukimas.cloudstorage.controller.ResourceController;
import com.dimasukimas.cloudstorage.dto.ResourceInfoDto;
import com.dimasukimas.cloudstorage.service.ResourceManagerService;
import com.dimasukimas.cloudstorage.service.ResourceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResourceController.class)
public class ResourceControllerTest {

    @MockitoBean
    private ResourceManagerService resourceManagerService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithCustomUser
    void getResourceInfo_shouldReturnResourceInfo() throws Exception{

        var resInfo = new ResourceInfoDto("folder1/", "folder2/",null, ResourceType.DIRECTORY);

        when(resourceManagerService.getResourceInfo(1L, "folder2/" )).thenReturn(resInfo);

        mockMvc.perform(get("/resource").param("path", "folder2/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value("folder1/"))
                .andExpect(jsonPath("$.name").value("folder2/"))
                .andExpect(jsonPath("$.type").value(ResourceType.DIRECTORY.toString()));
    }

}
