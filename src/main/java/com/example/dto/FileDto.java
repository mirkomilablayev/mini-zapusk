package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FileDto {
    private String fileId;
    private String contentType;

    public FileDto(String contentType){
        this.contentType = contentType;
    }

}
