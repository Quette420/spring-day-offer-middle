package com.onedayoffer.taskdistribution.services;

import com.onedayoffer.taskdistribution.DTO.EmployeeDTO;
import com.onedayoffer.taskdistribution.DTO.TaskDTO;
import com.onedayoffer.taskdistribution.DTO.TaskStatus;
import com.onedayoffer.taskdistribution.exceptions.TaskNotFoundException;
import com.onedayoffer.taskdistribution.exceptions.UserNotFoundException;
import com.onedayoffer.taskdistribution.repositories.EmployeeRepository;
import com.onedayoffer.taskdistribution.repositories.entities.Employee;
import com.onedayoffer.taskdistribution.repositories.entities.Task;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;

    public List<EmployeeDTO> getEmployees(@Nullable String sortDirection) {
        log.info("getEmployees({})", sortDirection);
        List<Employee> employees;
        if (Optional.ofNullable(sortDirection).isPresent()) {
            Sort.Direction direction = Sort.Direction.valueOf(sortDirection);
            employees = employeeRepository.findAllAndSort(Sort.by(direction, "fio"));
        } else {
            employees = employeeRepository.findAll();
        }
        return modelMapper.map(employees, new TypeToken<List<EmployeeDTO>>() {}.getType());
    }

    @Transactional
    public EmployeeDTO getOneEmployee(Integer id) {
        log.info("getOneEmployee({})", id);
        var employee = getEmployeeByIdIfPresent(id);
        return modelMapper.map(employee, EmployeeDTO.class);
    }

    public List<TaskDTO> getTasksByEmployeeId(Integer id) {
        log.info("getTasksByEmployeeId({})", id);
        var employee = getEmployeeByIdIfPresent(id);
        return employee.getTasks().stream().map(task -> modelMapper.map(task, TaskDTO.class)).collect(Collectors.toList());
    }

    @Transactional
    public void changeTaskStatus(Integer employeeId, Integer taskId, TaskStatus status) {
        log.info("changeTaskStatus(employeeId = {}, taskId = {}, status = {})", employeeId, taskId, status);
        var employee = getEmployeeByIdIfPresent(employeeId);
        var task = employee.getTasks().stream().filter(task1 -> task1.getId().equals(taskId)).findFirst().orElseThrow(() -> new TaskNotFoundException("Task with id " + taskId + " not found"));
        task.setStatus(status);
    }

    @Transactional
    public void postNewTask(Integer employeeId, TaskDTO newTask) {
        log.info("changeTaskStatus(employeeId = {}, newTask = {})", employeeId, newTask);
        var employee = getEmployeeByIdIfPresent(employeeId);
        var task = modelMapper.map(newTask, Task.class);
        employee.addTask(task);
    }

    private Employee getEmployeeByIdIfPresent(Integer employeeId) {
        return employeeRepository.findById(employeeId).orElseThrow(() -> new UserNotFoundException("Employee with id " + employeeId + " not found"));
    }
}
