package com.secure.secure.util;

public class CustomException extends Exception{
    String message;
    public CustomException(String message){
        this.message = message;
    }
    public String getMessage(){
        return message;
    }
}
