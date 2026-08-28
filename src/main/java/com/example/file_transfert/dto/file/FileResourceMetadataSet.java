package com.example.file_transfert.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileResourceMetadataSet {
    int page;
    int prev;
    int next;
    int totalItems;
    int totalPages;
    int limit = 20;
    List<FileResourceMetadata> files;
}
