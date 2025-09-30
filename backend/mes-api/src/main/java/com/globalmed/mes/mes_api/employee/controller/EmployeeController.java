package com.globalmed.mes.mes_api.employee.controller;


import com.globalmed.mes.mes_api.employee.dto.EmployeeAssignmentDto;
import com.globalmed.mes.mes_api.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<Page<EmployeeAssignmentDto>> getEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "employeeName,asc") String sort,
            @RequestParam(required = false) String name
    ) {
        Sort s = Sort.by(sort.split(",")[0]);
        if (sort.split(",").length > 1 && sort.split(",")[1].equalsIgnoreCase("desc")) {
            s = s.descending();
        }
        PageRequest pageable = PageRequest.of(page, size, s);

        Page<EmployeeAssignmentDto> result = employeeService.getEmployeesWithAssignments(name, pageable);
        return ResponseEntity.ok(result);
    }

}
