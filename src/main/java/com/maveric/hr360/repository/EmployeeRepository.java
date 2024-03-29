package com.maveric.hr360.repository;





import com.maveric.hr360.entity.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends MongoRepository<Employee, Long> {

    Employee findByEmployeeId(String employeeId);




}
