package com.secure.secure.controller;

import com.secure.secure.dto.Input;
import com.secure.secure.dto.Output;
import com.secure.secure.entity.Group;
import com.secure.secure.entity.GroupJoinRequest;
import com.secure.secure.entity.User;
import com.secure.secure.repository.GroupJoinRequestRepository;
import com.secure.secure.repository.GroupRepository;
import com.secure.secure.repository.PostRepository;
import com.secure.secure.repository.UserRepository;
import com.secure.secure.util.CustomException;
import com.secure.secure.util.ResponseMapper;
import com.secure.secure.util.ResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/group")
public class GroupController implements ResponseMapper {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private GroupJoinRequestRepository groupJoinRequestRepository;

    public String getUsername(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String username ="";
        if (principal instanceof UserDetails) {
            username = ((UserDetails)principal).getUsername();
        } else {
            username = principal.toString();
        }
        return username;
    }

    @PostMapping("/create")
    public ResponseObject createGroup(@RequestBody Input.CreateGroup input){
        try{
            if(input.groupName().length()>68){
                return errorResponse(new CustomException("A Group name can have maximum of 68 characters..."));
            }
            if(input.groupDescription().length()>500){
                return errorResponse(new CustomException("A Group Description can have maximum of 500 characters..."));
            }
            Pattern p1 = Pattern.compile("[^<>:{}'',~!@#$%^&*(+){}?; ]", Pattern.CASE_INSENSITIVE);
            Matcher m1 = p1.matcher(input.groupName());
            if(!m1.find()){
                return errorResponse(new CustomException("Group Name does not allow Special Characters  <>:{}'',~!@#$%^&*(){+}?;"));
            }

            Pattern p2 = Pattern.compile("[^<>*+?:;{}'' ]", Pattern.CASE_INSENSITIVE);
            Matcher m2 = p2.matcher(input.groupDescription());
            if(!m2.find()){
                return errorResponse(new CustomException("Group Description does not allow Special Characters  <>*?:;{+}''"));
            }

            var user = userRepository.findByUsername(getUsername());
            Group group = new Group();
            group.setGroupName(input.groupName());
            group.setGroupDescription(input.groupDescription());
            group.setCreatedBy(user.get().getUsername());
            group.setCreatedTime(new Date());
            group.setGroupMembers(List.of(user.get()));
            group.setUploadLimit(307200L); //300 MB
            group.setTotalUploadedSize(0L);
            var saved = groupRepository.save(group);
            return successResponse("Created Group: "+ saved.getGroupName());
        }
        catch (Exception e ){
            return errorResponse(new CustomException("Server Error..."));
        }
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
/*
    @PutMapping("/add-user")
    public ResponseObject addUser(@RequestBody Input.UserGroup input){
        var group = groupRepository.findById(input.groupId());
        var user = userRepository.findByUsername(getUsername());
        if(!user.get().getUsername().equals("defaultAdmin") ||
                !group.get().getCreatedBy().equals(getUsername())){
            return errorResponse(new CustomException("Not Authorized"));
        }
        group.get().getGroupMembers().add(user.get());
        //delete from group request`

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
    }*/

    @PutMapping("/update-limit")
    public ResponseObject updateLimit(@RequestBody Input.UploadRequest input){
        //todo validate each input and add limit to each input
        //todo only create should be able to edit
        var group = groupRepository.findById(input.id());
        if(!group.isPresent()){
            return errorResponse(new CustomException("Group not Found"));
        }
        if(group.get().getUploadLimit()< input.updateSize()*1024){
            return errorResponse(new CustomException("Updating limit is lessthan current limit"));
        }
        if(input.updateSize()*1024 > 500*1024){
            return errorResponse(new CustomException("Maximum Upload size for a group is 500MB"));
        }
        group.get().setUploadLimit(input.updateSize()*1024);
        groupRepository.save(group.get());
        return successResponse("Now group upload limit is increased to "+ input.updateSize() + " MB");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseObject deleteGroup(@PathVariable(name ="id") String groupId){
        var group = groupRepository.findById(groupId);
        var user = userRepository.findByUsername(getUsername());
        if(!user.get().getUsername().equals("Manideep223")
//                || !group.get().getCreatedBy().equals(getUsername())){
        ) {
            return errorResponse(new CustomException("Not Authorized"));
        }
        groupJoinRequestRepository.deleteAll(groupJoinRequestRepository.getRequest(groupId));
        var posts = postRepository.getAllGroupPosts(groupId);
        if(!posts.isEmpty()){
            List<User> usersToUpdate = new ArrayList<>();
            posts.forEach(x->{
                if(!x.getFileName().equals("")){
                    var usertoedit = x.getCreator();
                    usertoedit.setTotalUploadedSize(usertoedit.getTotalUploadedSize()-x.getFileSize());
                    usersToUpdate.add(usertoedit);
                }
            });
            userRepository.saveAll(usersToUpdate);
            postRepository.deleteAll(posts);
        }

        groupRepository.deleteById(groupId);
        return successResponse("Successfully deleted the group");

    }

    @GetMapping("/all")
    public ResponseObject allGroups(){
        var user = userRepository.findByUsername(getUsername());
        if(!user.get().getUsername().equals("Manideep223")){
            return errorResponse(new CustomException("Not Authorized"));
        }
        return successResponse(groupRepository.findAll().stream()
                .map(x-> new Output.GroupsListAll(x.getId(),x.getGroupName(),x.getGroupDescription(),
                x.getTotalUploadedSize()/1024 + " MB",
                x.getUploadLimit()/1024 + " MB")));
    }

    @GetMapping("/currentUser")
    public ResponseObject allUserGroups(){
        return successResponse(groupRepository.findUserGroups1(getUsername()).stream()
                .map(x-> new Output.GroupsList(x.getId(),x.getGroupName(),x.getGroupDescription())));
    }

    @GetMapping("/explore")
    public ResponseObject exploreGroups(){
        var userGroups = groupRepository.findUserGroups1(getUsername()).stream().map(Group::getId).toList();
        var userRequestedGroups = groupJoinRequestRepository.findUserGroupsRequests(getUsername())
                .stream().map(x->x.getGroup().getId()).toList();
        var exploreList = groupRepository.findAll().stream()
                .filter(x->!userGroups.contains(x.getId()))
                .map(x-> new Output.ExploreList(x.getId(),x.getGroupName(),x.getGroupDescription(),
                        userRequestedGroups.contains(x.getId() )));
        return successResponse(exploreList);
    }

    @PostMapping("/request/{id}")
    public ResponseObject requestGroup(@PathVariable(name ="id") String groupId){
        try {
            var user = userRepository.findByUsername(getUsername());
            GroupJoinRequest groupJoinRequest = new GroupJoinRequest();
            groupJoinRequest.setGroup(groupRepository.findById(groupId).get());
            groupJoinRequest.setUser(user.get());
            groupJoinRequestRepository.save(groupJoinRequest);
            return successResponse("Request Submitted");
        }
        catch (Exception e){
            return errorResponse(new CustomException("Server Error!"));
        }
    }


}
