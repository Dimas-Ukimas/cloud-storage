package com.dimasukimas.cloudstorage.swagger.storage;

import com.dimasukimas.cloudstorage.dto.ResourceInfoResponseDto;
import com.dimasukimas.cloudstorage.exception.handler.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@WithPathParameter
@Operation(
        summary = "Upload files or directories to storage",
        description = """
                Uploads one or more files (as well as nested directories) to the specified user directory.
                
                        • **Query parameter** `path` is the path to the directory where the resources will be uploaded.
                          - If the path ends with `/`, it is considered a directory.
                          - The root directory is set to the empty value `path'.
                
                        • **The request body** (`multipart/form-data`) contains file data from the HTML input.
                          If the file name includes a subdirectory (for example, `upload_folder/test.txt `),
                          then, when uploading inside `storage_folder/`, this structure will be created, and the file will be on the path `storage_folder/upload_folder/test.txt `.
                
                        It supports downloading multiple files, as well as recursive subfolders in a single request.
                """
)
@io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        description = "Multipart/form-data format. The `object` parameter contains one or more files to download.."
)
@ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "The files have been uploaded successfully. The list of downloaded resources is returned.",
                content = @Content(
                        mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = ResourceInfoResponseDto.class)),
                        examples = @ExampleObject(value = """
                                    [
                                      {
                                        "path": "folder1/folder2/",
                                        "name": "file.txt",
                                        "size": 123,
                                        "type": "FILE"
                                      }
                                    ]
                                """)
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request body (for example, missing part 'files' or incorrect multipart).",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
                responseCode = "409",
                description = "Resource is already exists.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )

})
public @interface UploadResourceDocs {
}
