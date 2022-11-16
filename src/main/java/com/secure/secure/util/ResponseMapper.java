package com.secure.secure.util;

public interface ResponseMapper {

    default ResponseObject errorResponse(CustomException ce){
        var response = new ResponseObject();
        response.setStatus("ERROR");
        response.setResponse(null);
        return response;
    }

    default ResponseObject successResponse(Object o){
        var response = new ResponseObject();
        response.setStatus("SUCCESS");
        response.setResponse(o);
        return response;
    }

}
