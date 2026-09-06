package com.example.file_transfert.dto.file;


import com.example.file_transfert.model.Resources;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUpdateVisibility {
    private Resources.Visibility visibility;
}
