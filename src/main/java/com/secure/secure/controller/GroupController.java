package com.secure.secure.controller;

import com.secure.secure.dto.Input;
import com.secure.secure.dto.Output;
import com.secure.secure.entity.Group;
import com.secure.secure.repository.GroupRepository;
import com.secure.secure.repository.UserRepository;
import com.secure.secure.util.CustomException;
import com.secure.secure.util.ResponseMapper;
import com.secure.secure.util.ResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/group")
public class GroupController implements ResponseMapper {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GroupRepository groupRepository;

    @PostMapping("/create")
    public ResponseObject createGroup(@RequestBody Input.CreateGroup input){
        //todo validate each input and add limit to each input
        //todo check if group exist with same name
        Group group = new Group();
        group.setGroupName(input.groupName());
        group.setGroupDescription(input.groupDescription());
        group.setCreatedBy(input.createdBy());
        group.setCreatedTime(new Date());
        var user = userRepository.findByUsername(input.createdBy());
        group.setGroupMembers(List.of(user.get()));
        return successResponse("Created Group: "+ input.groupName());
    }

    @PutMapping("/update-description")
    public ResponseObject updateDescription(@RequestBody Input.GroupDescription input){
        //todo validate each input and add limit to each input
        //todo only create should be able to edit
        var group = groupRepository.findById(input.groupId());
        if(!group.isPresent()){
            return errorResponse(new CustomException("Group not Found"));
        }
        group.get().setGroupDescription(input.groupDescription());
        groupRepository.save(group.get());
        return successResponse("Updated Description: "+ group.get().getGroupName());
    }

    @PutMapping("/add-user")
    public ResponseObject addUser(@RequestBody Input.UserGroup input){
        //todo validate each input and add limit to each input
        //todo only create should be able to edit
        var group = groupRepository.findById(input.groupId());
        if(!group.isPresent()){
            return errorResponse(new CustomException("Group not Found"));
        }
        var user = userRepository.findById(input.userId());
        group.get().getGroupMembers().add(user.get());
        groupRepository.save(group.get());
        return successResponse("Added User to group");
    }

    @PutMapping("/remove-user")
    public ResponseObject removeUser(@RequestBody Input.UserGroup input){
        //todo validate each input and add limit to each input
        //todo only create should be able to edit
        var group = groupRepository.findById(input.groupId());
        if(!group.isPresent()){
            return errorResponse(new CustomException("Group not Found"));
        }
        var user = userRepository.findById(input.userId());
        group.get().getGroupMembers().remove(user.get());
        groupRepository.save(group.get());
        return successResponse("Removed User from group");
    }

    @PutMapping("/update-limit")
    public ResponseObject updateLimit(@RequestBody Input.UserGroup input){
        //todo validate each input and add limit to each input
        //todo only create should be able to edit
        var group = groupRepository.findById(input.groupId());
        if(!group.isPresent()){
            return errorResponse(new CustomException("Group not Found"));
        }
        group.get().setUploadLimit(300000L); // 300 MB
        groupRepository.save(group.get());
        return successResponse("Removed User from group");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseObject deleteGroup(@PathVariable(name ="id") String groupId){
        //todo validate userId
        //todo remove all group posts
        groupRepository.deleteById(groupId);
        return successResponse("Successfully deleted the group");

    }

    @GetMapping("/all")
    public ResponseObject allGroups(){
        //todo validate admin
        return successResponse(groupRepository.findAll().stream()
                .map(x-> new Output.GroupsList(x.getId(),x.getGroupName(),x.getGroupDescription())));
    }

    @GetMapping("/user-all/{id}")
    public ResponseObject allUserGroups(@PathVariable(name ="id") String userId){
        //todo validate admin
        return successResponse(groupRepository.findUserGroups(userId).stream()
                .map(x-> new Output.GroupsList(x.getId(),x.getGroupName(),x.getGroupDescription())));
    }

    @GetMapping("/explore/{id}")
    public ResponseObject exploreGroups(@PathVariable(name ="id") String userId){
        //todo validate admin
        var userGroups = groupRepository.findUserGroups(userId).stream().map(Group::getId).toList();
        return successResponse(groupRepository.findAll().stream()
                .filter(x->!userGroups.contains(x.getId()))
                .map(x-> new Output.GroupsList(x.getId(),x.getGroupName(),x.getGroupDescription())));
    }
}
