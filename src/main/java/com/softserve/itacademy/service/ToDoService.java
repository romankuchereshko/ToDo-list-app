package com.softserve.itacademy.service;

import com.softserve.itacademy.model.ToDo;

import java.util.List;

public interface ToDoService {
    ToDo create(ToDo todo);
    ToDo readById(long id);
    ToDo update(ToDo todo);
    void delete(long id);
    boolean userOwnsToDo(String email, Long todoId);
    boolean userCollabsToDo(String email, Long todoId);
    List<ToDo> getAll();
    List<ToDo> getByUserId(long userId);
}
