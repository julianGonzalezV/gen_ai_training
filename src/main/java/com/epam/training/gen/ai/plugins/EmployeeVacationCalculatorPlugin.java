package com.epam.training.gen.ai.plugins;

import com.epam.training.gen.ai.dto.EmployeeDto;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class EmployeeVacationCalculatorPlugin {
    // Mock data for the employees
    private final Map<Integer, EmployeeDto> employees = new HashMap<>();

    public EmployeeVacationCalculatorPlugin() {
        employees.put(1234, new EmployeeDto(1234, "Mr", "Julian Doe", Date.from(LocalDate.of(2019, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC))));
        employees.put(567, new EmployeeDto(567, "Mrs", "Pepito Perez", Date.from(LocalDate.of(2020, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC))));
        employees.put(890, new EmployeeDto(890, "Mr", "John Smith", Date.from(LocalDate.of(2021, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC))));
        employees.put(4, new EmployeeDto(4, "Mrs", "Evelio Jaramillo", Date.from(LocalDate.of(2022, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC))));
        employees.put(5, new EmployeeDto(5, "Mr", "Cesario", Date.from(LocalDate.of(2023, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC))));
        employees.put(6, new EmployeeDto(6, "Mrs", "Paola Cruz", Date.from(LocalDate.of(2024, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC))));
        employees.put(7, new EmployeeDto(7, "Mr", "Juliano Brown", Date.from(LocalDate.of(2018, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC))));
        employees.put(8, new EmployeeDto(8, "Mrs", "Jane Marin", Date.from(LocalDate.of(2017, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC))));
        employees.put(9, new EmployeeDto(9, "Mr", "Goku", Date.from(LocalDate.of(2016, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC))));
        employees.put(10, new EmployeeDto(10, "Mrs", "Gohan", Date.from(LocalDate.of(2015, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC))));

    }

    /**
     * This method returns a string with the number of vacation days an employee has based on their joining date
     * @param id
     * @return String
     */
    @DefineKernelFunction(name = "calculate_vacations", description = "Calculates employee vacations days based on joining date and current date")
    public String calculateVacations(@KernelFunctionParameter(name = "id", description = "The id of the employee asking for vacations days") int id
    ) {
        log.info("Calculating vacation days for employee: {}", id);
        if (!employees.containsKey(id)) {
            throw new IllegalArgumentException("Employee not found");
        }
        double yearsInCompany = (LocalDate.now().getYear() - employees.get(id).getJoiningDate().toInstant().atZone(ZoneOffset.UTC).toLocalDate().getYear());
        double vacationDays = yearsInCompany * 15;
        return "Employee " + employees.get(id).getName() + " has " + vacationDays + " vacation days";
    }
}
