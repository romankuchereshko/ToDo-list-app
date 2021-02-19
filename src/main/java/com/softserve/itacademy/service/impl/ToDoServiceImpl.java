package com.softserve.itacademy.service.impl;

import com.softserve.itacademy.exception.NullEntityReferenceException;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.repository.ToDoRepository;
import com.softserve.itacademy.service.ToDoService;
import com.softserve.itacademy.service.UserService;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ToDoServiceImpl implements ToDoService {

    private ToDoRepository todoRepository;
    private UserService userService;

    public ToDoServiceImpl(ToDoRepository todoRepository,  UserService userService) {
        this.todoRepository = todoRepository;
        this.userService = userService;
    }

    @Override
    public ToDo create(ToDo role) {
        if (role != null) {
            return todoRepository.save(role);
        }
        throw new NullEntityReferenceException("ToDo cannot be 'null'");
    }

    @Override
    public ToDo readById(long id) {
        return todoRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("ToDo with id " + id + " not found"));
    }

    @Override
    public ToDo update(ToDo role) {
        if (role != null) {
            readById(role.getId());
            return todoRepository.save(role);
        }
        throw new NullEntityReferenceException("ToDo cannot be 'null'");
    }

    @Override
    public void delete(long id) {
        todoRepository.delete(readById(id));
    }

    @Override
    public List<ToDo> getAll() {
        List<ToDo> todos = todoRepository.findAll();
        return todos.isEmpty() ? new ArrayList<>() : todos;
    }

    @Override
    public List<ToDo> getByUserId(long userId) {
        List<ToDo> todos = todoRepository.getByUserId(userId);
        return todos.isEmpty() ? new ArrayList<>() : todos;
    }

    public boolean userOwnsToDo(String userEmail, Long todoId) {
        if(userEmail == null || userEmail.isEmpty() || userEmail.isBlank())
            throw new EntityNotFoundException("User with given email does not exist");
        if (todoId == null) {

        }
        User user = userService.readByEmail(userEmail);
        ToDo todo = readById(todoId);
        if (user != null && todo != null) {
            return user.getMyTodos().contains(todo);
        }
        throw new EntityNotFoundException("User with given email or ToDo with given id does not exist");
    }

    public boolean userCollabsToDo(String userEmail, Long todoId) {
        if(userEmail == null || userEmail.isEmpty() || userEmail.isBlank())
            throw new EntityNotFoundException("User with given email does not exist");
        if (todoId == null) {

        }
        User user = userService.readByEmail(userEmail);
        ToDo todo = readById(todoId);
        if (user != null && todo != null) {
            return user.getOtherTodos().contains(todo);
        }
        throw new EntityNotFoundException("User with given email or ToDo with given id does not exist");
    }
}
