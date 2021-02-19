package com.softserve.itacademy.controller;

import com.softserve.itacademy.model.Task;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.service.TaskService;
import com.softserve.itacademy.service.ToDoService;
import com.softserve.itacademy.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/todos")
public class ToDoController {

    static int counter = 0;

    private final ToDoService todoService;
    private final TaskService taskService;
    private final UserService userService;

    public ToDoController(ToDoService todoService, TaskService taskService, UserService userService) {
        this.todoService = todoService;
        this.taskService = taskService;
        this.userService = userService;
    }

    @GetMapping("/create/users/{owner_id}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public String create(@PathVariable("owner_id") long ownerId, Model model) {

        model.addAttribute("todo", new ToDo());
        model.addAttribute("ownerId", ownerId);
        return "create-todo";
    }

    @PostMapping("/create/users/{owner_id}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public String create(@PathVariable("owner_id") long ownerId, @Validated @ModelAttribute("todo") ToDo todo, BindingResult result) {
        if (result.hasErrors()) {
            return "create-todo";
        }
        todo.setCreatedAt(LocalDateTime.now());
        todo.setOwner(userService.readById(ownerId));
        todoService.create(todo);
        return "redirect:/todos/all/users/" + ownerId;
    }

    @GetMapping("/{todo_id}/tasks")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public String read(@PathVariable long todo_id, Model model, Authentication auth) {
        if(authenticationContainsAuthority(auth, "USER")
                && !todoService.userCollabsToDo(auth.getName(), todo_id))
            throw new AccessDeniedException("User can't view todo that doesn't belong him");
        ToDo todo = todoService.readById(todo_id);
        List<Task> tasks = taskService.getByTodoId(todo_id);
        List<User> users = userService.getAll().stream()
                .filter(user -> user.getId() != todo.getOwner().getId()).collect(Collectors.toList());
        model.addAttribute("todo", todo);
        model.addAttribute("tasks", tasks);
        model.addAttribute("users", users);
        return "todo-tasks";
    }

    @GetMapping("/{todo_id}/update/users/{owner_id}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public String update(@PathVariable("todo_id") long todoId, @PathVariable("owner_id") long ownerId, Model model, Authentication auth) {
        System.out.println("AUTH  CONTAINS AUTHORITY USER : " + authenticationContainsAuthority(auth, "USER"));
        if(authenticationContainsAuthority(auth, "USER")
                && !todoService.userOwnsToDo(auth.getName(), todoId))
            throw new AccessDeniedException("User can't edit todo that doesn't belong him");

        ToDo todo = todoService.readById(todoId);
        model.addAttribute("todo", todo);
        return "update-todo";
    }

    @PostMapping("/{todo_id}/update/users/{owner_id}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public String update(@PathVariable("todo_id") long todoId, @PathVariable("owner_id") long ownerId,
                         @Validated @ModelAttribute("todo") ToDo todo, BindingResult result) {
        if (result.hasErrors()) {
            todo.setOwner(userService.readById(ownerId));
            return "update-todo";
        }
        ToDo oldTodo = todoService.readById(todoId);
        todo.setOwner(oldTodo.getOwner());
        todo.setCollaborators(oldTodo.getCollaborators());
        todoService.update(todo);
        return "redirect:/todos/all/users/" + ownerId;
    }

    @GetMapping("/{todo_id}/delete/users/{owner_id}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public String delete(@PathVariable("todo_id") long todoId, @PathVariable("owner_id") long ownerId, Authentication auth) {
        if(!authenticationContainsAuthority(auth, "ADMIN")
                && !todoService.userOwnsToDo(auth.getName(), todoId))
            throw new AccessDeniedException("User can't delete todo that doesn't belong him");
        todoService.delete(todoId);
        return "redirect:/todos/all/users/" + ownerId;
    }

    @GetMapping("/all/users/{user_id}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public String getAll(@PathVariable("user_id") long userId, Model model) {
        List<ToDo> todos = todoService.getByUserId(userId);
        model.addAttribute("todos", todos);
        model.addAttribute("user", userService.readById(userId));
        return "todos-user";
    }

    @GetMapping("/{todo_id}/add")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public String addCollaborator(@PathVariable long todo_id, @RequestParam("user_id") long userId, Authentication auth) {
        if(authenticationContainsAuthority(auth, "USER")
                && !todoService.userOwnsToDo(auth.getName(), todo_id))
            throw new AccessDeniedException("User can't add collaborators into todo that doesn't belong him");
        ToDo todo = todoService.readById(todo_id);
        List<User> collaborators = todo.getCollaborators();
        collaborators.add(userService.readById(userId));
        todo.setCollaborators(collaborators);
        todoService.update(todo);
        return "redirect:/todos/" + todo_id + "/tasks";
    }


    @GetMapping("/{todo_id}/remove")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public String removeCollaborator(@PathVariable long todo_id, @RequestParam("user_id") long userId, Authentication auth) {
        if(authenticationContainsAuthority(auth, "USER")
                && !todoService.userOwnsToDo(auth.getName(), todo_id))
            throw new AccessDeniedException("User can't remove collaborators from todo that doesn't belong him");
        ToDo todo = todoService.readById(todo_id);
        List<User> collaborators = todo.getCollaborators();
        collaborators.remove(userService.readById(userId));
        todo.setCollaborators(collaborators);
        todoService.update(todo);
        return "redirect:/todos/" + todo_id + "/tasks";
    }

    private boolean authenticationContainsAuthority(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream().filter(auth -> auth.getAuthority().equalsIgnoreCase(authority)).findAny().orElse(null) != null;
    }

    /*private String getLoggedInUsernameIfUSER() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if(principal instanceof UserDetails)
            username = ((UserDetails) principal).getUsername();
        else
            username = principal.toString();
        return username;
    }*/
}
