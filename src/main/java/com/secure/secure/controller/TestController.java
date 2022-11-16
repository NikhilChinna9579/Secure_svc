package com.secure.secure.controller;

import com.mongodb.MongoSocketReadTimeoutException;
import com.secure.secure.entity.File;
import com.secure.secure.repository.FileRepository;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private FileRepository fileRepository;

    @GetMapping("/hw")
    public String hello(){
        return "hello world!";
    }

    @PostMapping("/save")
    public Object save(@RequestParam String name, MultipartFile file) throws IOException {
        File filedb = new File();
        filedb.setName(name);
        if(file.isEmpty()){
            filedb.setFile(new Binary(BsonBinarySubType.BINARY, file.getBytes()));
        }

        return fileRepository.save(filedb);
    }

    @GetMapping("/all")
    public Object all(){
        return fileRepository.findAll();
    }

}
