package com.onedayoffer.taskdistribution.services;

import com.onedayoffer.taskdistribution.DTO.EmployeeDTO;
import com.onedayoffer.taskdistribution.DTO.TaskDTO;
import com.onedayoffer.taskdistribution.DTO.TaskStatus;
import com.onedayoffer.taskdistribution.exceptions.TaskNotFoundException;
import com.onedayoffer.taskdistribution.exceptions.UserNotFoundException;
import com.onedayoffer.taskdistribution.repositories.EmployeeRepository;
import com.onedayoffer.taskdistribution.repositories.TaskRepository;
import com.onedayoffer.taskdistribution.repositories.entities.Employee;
import com.onedayoffer.taskdistribution.repositories.entities.Task;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;

    public List<EmployeeDTO> getEmployees(@Nullable String sortDirection) {
        Type listType = new TypeToken<List<EmployeeDTO>>() {}.getType();
        List<Employee> employees;
        if (Optional.ofNullable(sortDirection).isPresent()) {
            Sort.Direction direction = Sort.Direction.valueOf(sortDirection);
            employees = employeeRepository.findAllAndSort(Sort.by(direction, "fio"));
            return modelMapper.map(employees, listType);
        } else {
            employees = employeeRepository.findAll();
            return modelMapper.map(employees, listType);
        }
    }

    @Transactional
    public EmployeeDTO getOneEmployee(Integer id) {
        Employee employee = getEmployeeByIdIfPresent(id);
        return modelMapper.map(employee, EmployeeDTO.class);
    }

    public List<TaskDTO> getTasksByEmployeeId(Integer id) {
        Employee employee = getEmployeeByIdIfPresent(id);
        return employee.getTasks().stream().map(task -> modelMapper.map(task, TaskDTO.class)).collect(Collectors.toList());
    }

    @Transactional
    public void changeTaskStatus(Integer taskId, TaskStatus status) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new UserNotFoundException("Task with id " + taskId + " not found"));
        task.setStatus(status);
    }

    @Transactional
    public void postNewTask(Integer employeeId, TaskDTO newTask) {
        Employee employee = getEmployeeByIdIfPresent(employeeId);
        Task task = modelMapper.map(newTask, Task.class);
        employee.addTask(task);
    }

    private Employee getEmployeeByIdIfPresent(Integer employeeId) {
        return employeeRepository.findById(employeeId).orElseThrow(() -> new UserNotFoundException("Employee with id " + employeeId + " not found"));
    }
}
