package com.example.file_transfert.dto.file;

import com.example.file_transfert.model.Resources;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileResourceMetadata {
    String filename;
    String mimeType;
    String ownerName;
    long size;
    Resources.Visibility visibility;
}
