package com.dimasukimas.cloud_storage.service;

import com.dimasukimas.cloud_storage.dto.DirectoryInfoDto;
import com.dimasukimas.cloud_storage.dto.FileInfoDto;
import org.springframework.stereotype.Service;

@Service
public class FileManagerService {

   public DirectoryInfoDto createDirectory(Long userId, String directoryName){

      return new DirectoryInfoDto("1","1","1");
   }

}
