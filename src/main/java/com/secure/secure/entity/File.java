package com.secure.secure.entity;


import lombok.Data;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "files")
@Data
public class File {

    @Id
    private String id;

    private String name;

    private Binary file;
}
